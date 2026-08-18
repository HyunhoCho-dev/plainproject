package com.plain.backend.api.focus;

import com.plain.backend.api.account.AccountRepository;
import com.plain.backend.session.FocusSession;
import com.plain.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 타이머 화면이 쓰는 집중 세션 기록입니다.
 *
 * 기존 FocusSessionService와 하는 일이 겹치지만, 기존 파일을 고치지 않기 위해 따로 두었습니다.
 * 저장하는 테이블(focus_sessions)과 엔티티는 완전히 같습니다.
 */
@Service
@RequiredArgsConstructor
public class FocusService {

    private final FocusRepository focusRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public FocusDtos.SessionResponse start(FocusDtos.StartRequest request) {
        User user = accountRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + request.userId()));

        FocusSession session = FocusSession.builder()
                .user(user)
                .startTime(LocalDateTime.now())
                .goalDescription(request.goal())
                .targetDurationMinutes(request.targetMinutes())
                .build();

        return FocusDtos.SessionResponse.from(focusRepository.save(session));
    }

    @Transactional
    public FocusDtos.SessionResponse end(Long sessionId) {
        FocusSession session = focusRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        session.setEndTime(LocalDateTime.now());
        long minutes = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        session.setActualDurationMinutes((int) minutes);

        return FocusDtos.SessionResponse.from(focusRepository.save(session));
    }

    /** 오늘 끝난 세션만 최신순으로 돌려줍니다. 진행 중인 세션은 제외합니다. */
    @Transactional(readOnly = true)
    public List<FocusDtos.SessionResponse> findTodayFinished(Long userId) {
        LocalDate today = LocalDate.now();
        return focusRepository.findByUserIdOrderByStartTimeDesc(userId).stream()
                .filter(session -> session.getEndTime() != null)
                .filter(session -> session.getStartTime().toLocalDate().equals(today))
                .map(FocusDtos.SessionResponse::from)
                .toList();
    }
}
