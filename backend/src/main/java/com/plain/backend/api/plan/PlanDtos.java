package com.plain.backend.api.plan;

import java.util.List;

/**
 * 화면과 주고받는 데이터 모양입니다.
 * 엔티티를 그대로 내보내면 연결된 User까지 딸려 나가므로 따로 둡니다.
 */
public final class PlanDtos {

    private PlanDtos() {}

    /** 목표 입력 3단계(시간 → 수준 → 내용)에서 모은 값이 그대로 들어옵니다. */
    public record GenerateRequest(
            Long userId,
            String goal,
            String currentLevel,
            Double dailyHours,
            String startDate,
            String constraints) {}

    public record PlanResponse(
            Long planId,
            String goal,
            Double dailyHours,
            String summary,
            Integer estimatedWeeks,
            List<String> advice,
            List<DayResponse> days) {}

    public record DayResponse(String date, List<BlockResponse> blocks) {}

    public record BlockResponse(
            Long id,
            String start,
            String end,
            String title,
            String purpose,
            boolean done) {}
}
