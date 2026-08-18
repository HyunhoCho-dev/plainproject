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
@Table(name = "distractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FocusSession focusSession;

    @Column(nullable = false)
    private String type;

    @Column(length = 500)
    private String description;

    @Column(name = "ai_severity")
    private String aiSeverity;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
