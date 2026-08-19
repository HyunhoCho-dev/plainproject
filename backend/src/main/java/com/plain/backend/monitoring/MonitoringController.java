package com.plain.backend.monitoring;

import lombok.RequiredArgsConstructor; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*; 

// =========================================================================================
// [Monitoring Controller]
// 사용자의 주의 산만(Distraction) 및 알림(Notification) 이벤트를 수집하는 REST API 컨트롤러입니다.
// 클라이언트 애플리케이션의 모니터링 이벤트 데이터를 수신하고 처리합니다.
// =========================================================================================

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    // 비즈니스 로직 위임을 위한 Service 계층 의존성 주입 (생성자 기반)
    private final MonitoringService monitoringService;

    // =========================================================================================
    // [Distraction 기록 API]
    // =========================================================================================
    // POST /api/monitoring/distraction
    // 사용자의 딴짓/주의 분산 이벤트를 데이터베이스에 기록합니다.
    @PostMapping("/distraction")
    public ResponseEntity<Distraction> logDistraction(
            @RequestParam Long sessionId,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam String aiSeverity) {
        
        // Service 레이어로 데이터 저장을 위임
        Distraction distraction = monitoringService.logDistraction(sessionId, type, description, aiSeverity);
        
        // HTTP 200 OK 상태 코드와 함께 저장된 엔티티 반환
        return ResponseEntity.ok(distraction);
    }

    // =========================================================================================
    // [Notification 기록 API]
    // =========================================================================================
    // POST /api/monitoring/notification
    // 스마트폰이나 PC에서 발생한 알림 이벤트를 데이터베이스에 기록합니다.
    @PostMapping("/notification")
    public ResponseEntity<Notification> logNotification(
            @RequestParam Long sessionId,
            @RequestParam String appName,
            @RequestParam String content,
            @RequestParam String aiImportance) {
        
        // Service 레이어로 알림 기록 저장 로직 위임
        Notification notification = monitoringService.logNotification(sessionId, appName, content, aiImportance);
        
        // HTTP 200 OK 상태 코드와 함께 저장된 알림 엔티티 반환
        return ResponseEntity.ok(notification);
    }
}
