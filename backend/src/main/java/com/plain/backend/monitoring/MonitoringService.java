package com.plain.backend.monitoring;

import com.plain.backend.session.FocusSession; 
import com.plain.backend.session.FocusSessionRepository; 
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
import java.time.LocalDateTime; 

// =========================================================================================
// [Monitoring Service]
// 사용자의 주의 분산(Distraction) 및 알림(Notification) 이벤트를 처리하고 영속화하는 비즈니스 로직 계층입니다.
// =========================================================================================

@Service
@RequiredArgsConstructor
public class MonitoringService {

    // Repository 의존성 주입. 데이터베이스와의 통신을 추상화합니다.
    private final FocusSessionRepository focusSessionRepository;
    private final DistractionRepository distractionRepository;
    private final NotificationRepository notificationRepository;

    // =========================================================================================
    // [Distraction 기록 로직]
    // =========================================================================================
    // 데이터의 일관성을 보장하기 위해 단일 트랜잭션 범위 내에서 로직을 실행합니다.
    @Transactional
    public Distraction logDistraction(Long sessionId, String type, String description, String aiSeverity) {
        
        // 1. 연관 관계 맵핑을 위해 부모 엔티티인 FocusSession 조회
        // 식별자에 해당하는 세션이 없을 경우 IllegalArgumentException 예외를 발생시킵니다.
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        // 2. Builder 패턴을 활용하여 새로운 Distraction 엔티티 인스턴스 생성
        Distraction distraction = Distraction.builder()
                .focusSession(session) 
                .type(type) 
                .description(description) 
                .aiSeverity(aiSeverity) 
                .timestamp(LocalDateTime.now()) // 이벤트 발생 시간을 서버의 현재 시간으로 기록
                .build();

        // 3. 엔티티를 영속성 컨텍스트에 저장(Persist)하고 생성된 객체를 반환
        return distractionRepository.save(distraction);
    }

    // =========================================================================================
    // [Notification 기록 로직]
    // =========================================================================================
    @Transactional
    public Notification logNotification(Long sessionId, String appName, String content, String aiImportance) {
        
        // 1. 연관된 FocusSession 엔티티 조회 유효성 검증
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        // 2. 비즈니스 로직: AI가 판단한 중요도(aiImportance)가 "LOW"인 경우 해당 알림을 차단된 것으로 간주
        // 문자열 비교 시 대소문자를 무시하여 안정성을 높임
        boolean isBlocked = "LOW".equalsIgnoreCase(aiImportance);

        // 3. Notification 엔티티 생성
        Notification notification = Notification.builder()
                .focusSession(session)
                .appName(appName)
                .content(content)
                .aiImportance(aiImportance)
                .isBlocked(isBlocked) // 산출된 차단 여부 플래그 세팅
                .timestamp(LocalDateTime.now()) 
                .build();

        // 4. 생성된 엔티티 저장 및 반환
        return notificationRepository.save(notification);
    }
}
