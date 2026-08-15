package com.plain.backend.session;

import com.plain.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * FocusSession (집중 세션): 집중 단위를 기록하는 엔티티
 */
@Entity
@Table(name = "focus_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "goal_description", length = 500)
    private String goalDescription;

    @Column
    private String category;

    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;
}
