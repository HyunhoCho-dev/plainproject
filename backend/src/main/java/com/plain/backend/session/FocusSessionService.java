package com.plain.backend.session;

import com.plain.backend.user.User;
import com.plain.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public FocusSession startSession(Long userId, String goalDescription, Integer targetMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        FocusSession session = FocusSession.builder()
                .user(user)
                .startTime(LocalDateTime.now())
                .goalDescription(goalDescription)
                .targetDurationMinutes(targetMinutes)
                .build();

        return focusSessionRepository.save(session);
    }

    @Transactional
    public FocusSession endSession(Long sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        session.setEndTime(LocalDateTime.now());
        long minutes = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        session.setActualDurationMinutes((int) minutes);

        return focusSessionRepository.save(session);
    }
}
