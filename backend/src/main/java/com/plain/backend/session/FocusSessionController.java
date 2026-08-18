package com.plain.backend.session;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    @PostMapping("/start")
    public ResponseEntity<FocusSession> startSession(
            @RequestParam Long userId,
            @RequestParam String goal,
            @RequestParam Integer targetMinutes) {
        
        FocusSession session = focusSessionService.startSession(userId, goal, targetMinutes);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/end")
    public ResponseEntity<FocusSession> endSession(@RequestParam Long sessionId) {
        FocusSession session = focusSessionService.endSession(sessionId);
        return ResponseEntity.ok(session);
    }
}
