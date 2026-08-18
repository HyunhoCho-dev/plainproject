package com.plain.backend.api.stats;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatsDtos {

    private StatsDtos() {}

    /** 하루치 기록. 화면 그래프와 AI 패턴 분석이 같은 값을 씁니다. */
    public record DailyStat(
            String date,
            int studyMinutes,
            int plannedMinutes,
            double completionRate,
            int blockedCount) {

        /** AI 서버(ai/docs/API.md)가 요구하는 필드 이름 그대로 변환합니다. */
        public Map<String, Object> toAiInput() {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date);
            item.put("studyMinutes", studyMinutes);
            item.put("plannedMinutes", plannedMinutes);
            item.put("completionRate", completionRate);
            item.put("blockedCount", blockedCount);
            return item;
        }
    }

    public record SummaryResponse(
            int todayMinutes,
            int weekMinutes,
            double averageCompletionRate,
            int blockedCount,
            int daysCollected,
            int needMoreDays,
            List<DailyStat> dailyStats) {}

    /**
     * enough=false면 AI를 부르지 않았다는 뜻이고, needMoreDays가 며칠 더 필요한지 알려줍니다.
     * 화면은 이 값으로 "앞으로 D일 더 필요" 문구를 채웁니다.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PatternResponse(boolean enough, int needMoreDays, Map<String, Object> insight) {}
}
