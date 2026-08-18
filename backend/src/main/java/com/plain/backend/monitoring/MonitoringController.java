package com.plain.backend.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping("/distraction")
    public ResponseEntity<Distraction> logDistraction(
            @RequestParam Long sessionId,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam String aiSeverity) {
        
        Distraction distraction = monitoringService.logDistraction(sessionId, type, description, aiSeverity);
        return ResponseEntity.ok(distraction);
    }

    @PostMapping("/notification")
    public ResponseEntity<Notification> logNotification(
            @RequestParam Long sessionId,
            @RequestParam String appName,
            @RequestParam String content,
            @RequestParam String aiImportance) {
        
        Notification notification = monitoringService.logNotification(sessionId, appName, content, aiImportance);
        return ResponseEntity.ok(notification);
    }
}
