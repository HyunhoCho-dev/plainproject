package com.plain.backend.monitoring;

import com.plain.backend.session.FocusSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FocusSession focusSession;

    @Column(name = "app_name", nullable = false)
    private String appName;

    @Column(length = 1000)
    private String content;

    @Column(name = "ai_importance")
    private String aiImportance;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
