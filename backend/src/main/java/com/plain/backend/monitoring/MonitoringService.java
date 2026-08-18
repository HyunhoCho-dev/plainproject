package com.plain.backend.monitoring;

import com.plain.backend.session.FocusSession;
import com.plain.backend.session.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final FocusSessionRepository focusSessionRepository;
    private final DistractionRepository distractionRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Distraction logDistraction(Long sessionId, String type, String description, String aiSeverity) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        Distraction distraction = Distraction.builder()
                .focusSession(session)
                .type(type)
                .description(description)
                .aiSeverity(aiSeverity)
                .timestamp(LocalDateTime.now())
                .build();

        return distractionRepository.save(distraction);
    }

    @Transactional
    public Notification logNotification(Long sessionId, String appName, String content, String aiImportance) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

        boolean isBlocked = "LOW".equalsIgnoreCase(aiImportance);

        Notification notification = Notification.builder()
                .focusSession(session)
                .appName(appName)
                .content(content)
                .aiImportance(aiImportance)
                .isBlocked(isBlocked)
                .timestamp(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }
}
