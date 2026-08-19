package com.plain.backend.session;

import com.plain.backend.user.User;
import com.plain.backend.user.UserRepository;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
import java.time.LocalDateTime; 

// =========================================================================================
// [Focus Session Service]
// 집중 세션(Focus Session)의 생성 및 종료 등 핵심 비즈니스 로직을 담당하는 Service 계층입니다.
// =========================================================================================

@Service
@RequiredArgsConstructor
public class FocusSessionService {

    // Repository 의존성 주입. 데이터베이스 접근을 추상화하여 비즈니스 로직에 집중할 수 있게 합니다.
    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;

    // =========================================================================================
    // [세션 시작 로직]
    // =========================================================================================
    // 해당 메서드 내의 작업은 단일 트랜잭션으로 묶이며, 예외 발생 시 자동으로 롤백(Rollback) 처리됩니다.
    @Transactional
    public FocusSession startSession(Long userId, String goalDescription, Integer targetMinutes) {
        
        // 1. User 엔티티 조회 (JPA 영속성 컨텍스트에 캐싱 또는 DB Select)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. 입력받은 데이터와 서버 측 현재 시간을 기반으로 새로운 FocusSession 엔티티를 생성 (Builder 패턴)
        FocusSession session = FocusSession.builder()
                .user(user) 
                .startTime(LocalDateTime.now()) 
                .goalDescription(goalDescription) 
                .targetDurationMinutes(targetMinutes) 
                .build();

        // 3. 엔티티를 데이터베이스에 영속화(Persist)하고 생성된 객체를 반환
        return focusSessionRepository.save(session);
    }

    // =========================================================================================
    // [세션 종료 로직]
    // =========================================================================================
    @Transactional
    public FocusSession endSession(Long sessionId) {
        
        // 1. 기존에 진행 중이던 세션 엔티티를 식별자를 통해 조회
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        // 2. 종료 시간을 현재 시간으로 갱신
        session.setEndTime(LocalDateTime.now());

        // 3. 시작 시간과 종료 시간의 차이를 계산하여 실제 집중한 시간(분 단위) 도출
        long minutes = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        session.setActualDurationMinutes((int) minutes);

        // 4. Dirty Checking(변경 감지) 또는 명시적 save 호출을 통해 업데이트된 상태를 DB에 반영
        return focusSessionRepository.save(session);
    }
}
