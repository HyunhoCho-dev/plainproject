package com.plain.backend.session;

import lombok.RequiredArgsConstructor; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*; 

// =========================================================================================
// [Focus Session Controller]
// 집중 세션(Focus Session)과 관련된 클라이언트(프론트엔드)의 HTTP 요청을 처리하는 REST API 컨트롤러입니다.
// =========================================================================================

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class FocusSessionController {

    // 비즈니스 로직 처리를 위임하기 위한 Service 계층 의존성 주입 (생성자 주입 방식)
    private final FocusSessionService focusSessionService;

    // =========================================================================================
    // [세션 시작 API]
    // =========================================================================================
    // POST /api/session/start
    // 클라이언트로부터 유저 ID, 목표 설명, 목표 집중 시간을 전달받아 새로운 세션을 생성합니다.
    @PostMapping("/start")
    public ResponseEntity<FocusSession> startSession(
            @RequestParam Long userId,
            @RequestParam String goal,
            @RequestParam Integer targetMinutes) {
        
        // Service 레이어에 트랜잭션 처리를 위임하고, 생성된 엔티티를 반환받습니다.
        FocusSession session = focusSessionService.startSession(userId, goal, targetMinutes);
        
        // HTTP 200 OK 상태 코드와 함께 생성된 세션 객체를 응답 본문에 담아 반환합니다.
        return ResponseEntity.ok(session);
    }

    // =========================================================================================
    // [세션 종료 API]
    // =========================================================================================
    // POST /api/session/end
    // 클라이언트로부터 종료할 세션 ID를 전달받아 해당 세션의 종료 시간 및 실제 집중 시간을 업데이트합니다.
    @PostMapping("/end")
    public ResponseEntity<FocusSession> endSession(
            @RequestParam Long sessionId) {
        
        // Service 레이어에서 세션 종료 로직 및 시간 계산을 수행하고 변경된 엔티티를 반환받습니다.
        FocusSession session = focusSessionService.endSession(sessionId);
        
        // HTTP 200 OK 상태 코드와 함께 업데이트된 세션 객체를 반환합니다.
        return ResponseEntity.ok(session);
    }
}
