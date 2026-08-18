package com.plain.backend.api.focus;

import com.plain.backend.session.FocusSession;

import java.time.LocalDateTime;

/**
 * 타이머 화면과 주고받는 데이터입니다.
 *
 * 엔티티(FocusSession)를 그대로 내보내면 지연 로딩된 User까지 딸려 나가 JSON 변환에서 실패합니다.
 * 그래서 필요한 값만 담아 보냅니다.
 */
public final class FocusDtos {

    private FocusDtos() {}

    public record StartRequest(Long userId, String goal, Integer targetMinutes) {}

    public record EndRequest(Long sessionId) {}

    public record SessionResponse(
            Long id,
            String goal,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer targetMinutes,
            Integer actualMinutes) {

        public static SessionResponse from(FocusSession session) {
            return new SessionResponse(
                    session.getId(),
                    session.getGoalDescription(),
                    session.getStartTime(),
                    session.getEndTime(),
                    session.getTargetDurationMinutes(),
                    session.getActualDurationMinutes());
        }
    }
}
