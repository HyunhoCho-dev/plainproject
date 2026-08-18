package com.plain.backend.api.plan;

import com.plain.backend.api.account.AccountRepository;
import com.plain.backend.api.common.AiClient;
import com.plain.backend.api.common.AiUnavailableException;
import com.plain.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 계획 생성의 전체 흐름입니다.
 * 화면 → Spring → AI 서버 → DB 저장 → 화면 순서로 한 번에 이어집니다.
 */
@Service
@RequiredArgsConstructor
public class PlanService {

    private final AiClient aiClient;
    private final StudyPlanRepository studyPlanRepository;
    private final PlanBlockRepository planBlockRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public PlanDtos.PlanResponse generate(PlanDtos.GenerateRequest request) {
        User user = accountRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + request.userId()));

        LocalDate startDate = request.startDate() == null || request.startDate().isBlank()
                ? LocalDate.now()
                : LocalDate.parse(request.startDate());

        // AI 서버가 요구하는 입력 형식(ai/docs/API.md)에 맞춰 그대로 넘깁니다.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("goal", request.goal());
        payload.put("currentLevel", request.currentLevel());
        payload.put("dailyHours", request.dailyHours());
        payload.put("startDate", startDate.toString());
        if (request.constraints() != null && !request.constraints().isBlank()) {
            payload.put("constraints", request.constraints());
        }

        Map<String, Object> result = aiClient.post("/api/ai/plans/generate", payload);

        StudyPlan plan = StudyPlan.builder()
                .user(user)
                .goal(request.goal())
                .currentLevel(request.currentLevel())
                .dailyHours(request.dailyHours())
                .startDate(startDate)
                .summary(text(result.get("summary")))
                .estimatedWeeks(integer(result.get("estimatedWeeks")))
                .advice(joinAdvice(result.get("advice")))
                .createdAt(LocalDateTime.now())
                .build();

        for (Map<String, Object> day : list(result.get("days"))) {
            LocalDate blockDate = parseDate(day.get("date"), startDate);
            for (Map<String, Object> block : list(day.get("blocks"))) {
                plan.addBlock(PlanBlock.builder()
                        .blockDate(blockDate)
                        .startTime(text(block.get("start")))
                        .endTime(text(block.get("end")))
                        .title(text(block.get("title")))
                        .purpose(text(block.get("purpose")))
                        .done(false)
                        .build());
            }
        }

        if (plan.getBlocks().isEmpty()) {
            throw new AiUnavailableException("AI가 일정이 없는 계획을 만들었습니다. 다시 시도해주세요.");
        }

        return toResponse(studyPlanRepository.save(plan));
    }

    /** 홈 화면이 부르는 지금 쓰는 계획. 없으면 null을 돌려 빈 상태 화면을 띄우게 합니다. */
    @Transactional(readOnly = true)
    public PlanDtos.PlanResponse findCurrent(Long userId) {
        return studyPlanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    /** 계획 블록의 완료 토글. 화면에서 체크를 누르면 바로 이 값이 바뀝니다. */
    @Transactional
    public PlanDtos.BlockResponse setBlockDone(Long blockId, boolean done) {
        PlanBlock block = planBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다. ID: " + blockId));
        block.setDone(done);
        return toBlockResponse(planBlockRepository.save(block));
    }

    private PlanDtos.PlanResponse toResponse(StudyPlan plan) {
        Map<LocalDate, List<PlanDtos.BlockResponse>> grouped = new LinkedHashMap<>();
        plan.getBlocks().stream()
                .sorted(Comparator.comparing(PlanBlock::getBlockDate)
                        .thenComparing(block -> block.getStartTime() == null ? "" : block.getStartTime()))
                .forEach(block -> grouped
                        .computeIfAbsent(block.getBlockDate(), key -> new ArrayList<>())
                        .add(toBlockResponse(block)));

        List<PlanDtos.DayResponse> days = grouped.entrySet().stream()
                .map(entry -> new PlanDtos.DayResponse(entry.getKey().toString(), entry.getValue()))
                .toList();

        List<String> advice = plan.getAdvice() == null || plan.getAdvice().isBlank()
                ? List.of()
                : Arrays.asList(plan.getAdvice().split("\n"));

        return new PlanDtos.PlanResponse(
                plan.getId(), plan.getGoal(), plan.getDailyHours(),
                plan.getSummary(), plan.getEstimatedWeeks(), advice, days);
    }

    private PlanDtos.BlockResponse toBlockResponse(PlanBlock block) {
        return new PlanDtos.BlockResponse(
                block.getId(), block.getStartTime(), block.getEndTime(),
                block.getTitle(), block.getPurpose(), Boolean.TRUE.equals(block.getDone()));
    }

    /* ── AI 응답(Map)에서 값을 꺼내는 작은 도구들 ─────────────── */

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        if (!(value instanceof List<?> items)) return List.of();
        return items.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private LocalDate parseDate(Object value, LocalDate fallback) {
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String joinAdvice(Object value) {
        if (!(value instanceof List<?> items) || items.isEmpty()) return null;
        return String.join("\n", items.stream().map(String::valueOf).toList());
    }
}
