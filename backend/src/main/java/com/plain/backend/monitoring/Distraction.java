package com.plain.backend.monitoring;

// 연관된 세션 엔티티 참조를 위해 동일 프로젝트 내의 FocusSession 클래스를 import 합니다.
import com.plain.backend.session.FocusSession; 

// JPA(Java Persistence API)의 핵심 어노테이션(@Entity, @Table, @Id 등)들을 사용하기 위한 패키지입니다.
import jakarta.persistence.*; 

// Lombok 라이브러리: 컴파일 시점에 Getter/Setter, 생성자, 빌더 패턴 코드를 자동 생성하여 보일러플레이트를 줄여줍니다.
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; 

// Java 8부터 도입된 불변(Immutable) 날짜/시간 API로, 스레드 세이프(Thread-safe)한 시간 기록을 위해 사용됩니다.
import java.time.LocalDateTime; 

// =========================================================================================
// [Distraction Entity]
// 사용자의 집중을 방해하는 요소(예: 웹서핑, 다른 앱 사용 등)의 발생 기록을 
// 관리하는 도메인 엔티티입니다.
// =========================================================================================

@Entity
@Table(name = "distractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distraction {

    // 기본 키(PK). 데이터베이스 측의 Auto Increment 전략을 따릅니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 FocusSession 내에 다수의 Distraction이 발생할 수 있으므로 다대일(N:1) 매핑을 적용합니다.
    // FetchType.LAZY를 적용하여 N+1 쿼리 문제를 방지하고 엔티티 로딩 성능을 최적화합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FocusSession focusSession;

    // 방해 요소의 카테고리 또는 유형 (예: "WEB_BROWSING", "APP_USAGE")
    @Column(nullable = false)
    private String type;

    // 방해 요소에 대한 상세 텍스트 설명 (최대 500자 제한)
    @Column(length = 500)
    private String description;

    // 인공지능(AI)이 분석한 해당 방해 요소의 심각도 수준 (예: "HIGH", "LOW")
    @Column(name = "ai_severity")
    private String aiSeverity;

    // 이벤트가 발생한 정확한 시각을 나타내는 타임스탬프 (필수 값)
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
