package com.plain.backend.session;

import com.plain.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

// =========================================================================================
// [FocusSession Entity]
// 집중 세션(Focus Session) 정보를 관리하는 도메인 엔티티입니다.
// JPA를 통해 데이터베이스의 'focus_sessions' 테이블과 매핑됩니다.
// =========================================================================================

@Entity
@Table(name = "focus_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusSession {

    // Primary Key (대리 키) 전략으로 IDENTITY를 사용하여 데이터베이스 측에서 자동 증가하도록 설정합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 엔티티와의 다대일(N:1) 매핑. 
    // 성능 최적화를 위해 FetchType.LAZY(지연 로딩)를 적용하여 연관 객체 초기화를 지연시킵니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 세션 시작 시간. 엔티티 생성 시 필수 값입니다.
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // 세션 종료 시간. 세션 진행 중에는 null일 수 있습니다.
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 집중 목표에 대한 상세 설명. 최대 길이를 500자로 제한합니다.
    @Column(name = "goal_description", length = 500)
    private String goalDescription;

    // 세션 카테고리 (예: 학습, 업무, 독서 등)
    @Column
    private String category;

    // 사용자가 설정한 목표 집중 시간 (분 단위)
    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    // 실제 집중한 시간 (분 단위). 세션 종료 시점에 계산되어 업데이트됩니다.
    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    // 현재 세션 상태 코드 (예: IN_PROGRESS, COMPLETED, CANCELLED 등).
    // Builder 패턴 사용 시 초기 상태를 "IN_PROGRESS"로 기본 설정합니다.
    @Column(nullable = false)
    @Builder.Default
    private String status = "IN_PROGRESS";
}
