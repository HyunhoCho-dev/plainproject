package com.plain.backend.monitoring;

import com.plain.backend.session.FocusSession; 
import jakarta.persistence.*; 
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; 
import java.time.LocalDateTime; 

// =========================================================================================
// [Notification Entity]
// 세션 도중 발생한 각종 애플리케이션 알림(Push Notification) 데이터를 관리하는 도메인 엔티티입니다.
// =========================================================================================

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    // 기본 키(PK). 데이터베이스 측의 Auto Increment 전략을 따릅니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 FocusSession 내에 다수의 Notification이 발생할 수 있으므로 다대일(N:1) 매핑을 적용합니다.
    // FetchType.LAZY를 적용하여 N+1 쿼리 문제를 방지하고 엔티티 로딩 성능을 최적화합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FocusSession focusSession;

    // 알림을 발생시킨 애플리케이션 명 (예: KakaoTalk, Messages)
    @Column(name = "app_name", nullable = false)
    private String appName;

    // 알림의 상세 텍스트 내용. 긴 내용이 포함될 수 있으므로 최대 1000자로 제한합니다.
    @Column(length = 1000)
    private String content;

    // 인공지능(AI)이 분석한 해당 알림의 중요도 수준 (예: "HIGH", "LOW")
    @Column(name = "ai_importance")
    private String aiImportance;

    // 사용자 화면에서 해당 알림을 차단(Block) 처리했는지 여부를 나타내는 플래그(Flag)입니다.
    @Column(name = "is_blocked")
    private Boolean isBlocked;

    // 이벤트가 발생한 정확한 시각을 나타내는 타임스탬프 (필수 값)
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
