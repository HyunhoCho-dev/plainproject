package com.plain.backend.api.stats;

import com.plain.backend.api.common.AiClient;
import com.plain.backend.api.focus.FocusRepository;
import com.plain.backend.api.plan.PlanBlock;
import com.plain.backend.api.plan.StudyPlanRepository;
import com.plain.backend.api.plan.StudyPlan;
import com.plain.backend.session.FocusSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 화면이 쓰는 집계입니다.
 *
 * 여기가 세 폴더가 실제로 만나는 지점입니다.
 * 타이머가 남긴 세션 + 계획 블록 + 방해 기록(backend)을 하루 단위로 묶어
 * AI 서버(ai)의 패턴 분석 입력 형식으로 변환해 넘깁니다.
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    /** AI 패턴 분석이 요구하는 최소 일수입니다. (ai/src/ai/ai-service.js의 검증과 같은 값) */
    private static final int MIN_DAYS_FOR_PATTERN = 14;

    private final FocusRepository focusRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StatsRepository statsRepository;
    private final AiClient aiClient;

    /** 통계 화면 상단의 숫자들과, 아래 그래프용 일별 기록을 한 번에 돌려줍니다. */
    @Transactional(readOnly = true)
    public StatsDtos.SummaryResponse summary(Long userId) {
        List<StatsDtos.DailyStat> dailyStats = buildDailyStats(userId);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        int todayMinutes = dailyStats.stream()
                .filter(day -> day.date().equals(today.toString()))
                .mapToInt(StatsDtos.DailyStat::studyMinutes).sum();

        int weekMinutes = dailyStats.stream()
                .filter(day -> !LocalDate.parse(day.date()).isBefore(weekStart))
                .mapToInt(StatsDtos.DailyStat::studyMinutes).sum();

        double averageCompletion = dailyStats.isEmpty() ? 0
                : dailyStats.stream().mapToDouble(StatsDtos.DailyStat::completionRate).average().orElse(0);

        int blockedCount = dailyStats.stream().mapToInt(StatsDtos.DailyStat::blockedCount).sum();
        int needMoreDays = Math.max(0, MIN_DAYS_FOR_PATTERN - dailyStats.size());

        return new StatsDtos.SummaryResponse(
                todayMinutes, weekMinutes, round2(averageCompletion), blockedCount,
                dailyStats.size(), needMoreDays, dailyStats);
    }

    /**
     * 쌓인 기록으로 AI 패턴 분석을 요청합니다.
     * 14일이 안 되면 AI를 부르지 않고 며칠이 더 필요한지 알려줍니다.
     */
    @Transactional(readOnly = true)
    public StatsDtos.PatternResponse analyzePatterns(Long userId) {
        List<StatsDtos.DailyStat> dailyStats = buildDailyStats(userId);

        if (dailyStats.size() < MIN_DAYS_FOR_PATTERN) {
            return new StatsDtos.PatternResponse(false, MIN_DAYS_FOR_PATTERN - dailyStats.size(), null);
        }

        // AI는 최대 90일까지 받습니다. 최근 기록이 더 의미 있으므로 뒤에서 잘라 보냅니다.
        List<StatsDtos.DailyStat> recent = dailyStats.size() > 90
                ? dailyStats.subList(dailyStats.size() - 90, dailyStats.size())
                : dailyStats;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dailyStats", recent.stream().map(StatsDtos.DailyStat::toAiInput).toList());

        return new StatsDtos.PatternResponse(true, 0, aiClient.post("/api/ai/patterns/analyze", payload));
    }

    /* ── 집계 ────────────────────────────────────────── */

    private List<StatsDtos.DailyStat> buildDailyStats(Long userId) {
        // 1) 끝난 세션을 날짜별 학습 시간으로 묶습니다.
        Map<LocalDate, Integer> studyMinutes = new HashMap<>();
        for (FocusSession session : focusRepository.findByUserIdOrderByStartTimeDesc(userId)) {
            if (session.getEndTime() == null || session.getActualDurationMinutes() == null) continue;
            studyMinutes.merge(session.getStartTime().toLocalDate(), session.getActualDurationMinutes(), Integer::sum);
        }

        // 2) 계획 블록에서 날짜별 계획 시간을 계산합니다.
        Map<LocalDate, Integer> plannedMinutes = new HashMap<>();
        studyPlanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(StudyPlan::getBlocks)
                .orElse(List.of())
                .forEach(block -> plannedMinutes.merge(block.getBlockDate(), minutesOf(block), Integer::sum));

        // 3) 방해 기록 수를 날짜별로 셉니다.
        Map<LocalDate, Integer> blockedCount = new HashMap<>();
        statsRepository.findByFocusSessionUserId(userId)
                .forEach(distraction -> blockedCount.merge(distraction.getTimestamp().toLocalDate(), 1, Integer::sum));

        // 실제로 공부한 날만 통계에 넣습니다. 계획만 있고 기록이 없는 날은 아직 지나지 않은 날일 수 있습니다.
        List<LocalDate> dates = new ArrayList<>(studyMinutes.keySet());
        dates.sort(LocalDate::compareTo);

        List<StatsDtos.DailyStat> result = new ArrayList<>();
        for (LocalDate date : dates) {
            int study = Math.min(1440, studyMinutes.getOrDefault(date, 0));
            int planned = Math.min(1440, plannedMinutes.getOrDefault(date, 0));
            double completion = planned > 0
                    ? Math.min(1.0, (double) study / planned)
                    : (study > 0 ? 1.0 : 0.0);
            result.add(new StatsDtos.DailyStat(
                    date.toString(), study, planned, round2(completion), blockedCount.getOrDefault(date, 0)));
        }
        return result;
    }

    /** "09:00" ~ "10:30" 같은 문자열에서 분을 계산합니다. 형식이 이상하면 0분으로 봅니다. */
    private int minutesOf(PlanBlock block) {
        try {
            long minutes = ChronoUnit.MINUTES.between(
                    LocalTime.parse(block.getStartTime()), LocalTime.parse(block.getEndTime()));
            return minutes > 0 ? (int) minutes : 0;
        } catch (Exception exception) {
            return 0;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
