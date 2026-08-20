package com.plain.backend.user;

import jakarta.persistence.*; 
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

// =========================================================================================
// [User Entity]
// 시스템을 이용하는 사용자 계정 정보를 관리하는 도메인 엔티티입니다.
// JPA를 통해 데이터베이스의 'users' 테이블과 매핑되며, 인증 및 인가 처리에 사용됩니다.
// =========================================================================================

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 username -> 사용자가 원할 경우 로그인 아이디 역할로 쓸 수 있으나, spec에 따라 phone 기반 자체 로그인이면 일단 보류하거나 phone과 별도로 아이디 역할을 할 수 있음.
    // 명세서 [DB] users에는 nickname이 있고 id는 pk. 로그인 아이디는 따로 안보이므로, phone을 아이디로 사용할 수도 있지만, "아이디+비밀번호 로그인"이라고 되어 있음.
    // username을 로그인 아이디로 둡니다.
    @Column(nullable = true, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String loginType; // "LOCAL" or "SOCIAL"

    @Column(nullable = true, unique = true)
    private String phone; // 전화번호 (LOCAL 가입시 필수)

    @Column(name = "password_hash")
    private String password; // 비밀번호 해시

    @Column(name = "social_provider")
    private String socialProvider; // "GOOGLE", "KAKAO", "NAVER" 등

    @Column(name = "social_id")
    private String socialId; // 소셜 로그인에서 제공하는 고유 ID

    @Column(name = "streak_count", nullable = false)
    @Builder.Default
    private Integer streakCount = 0; // 연속 학습일

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.streakCount == null) {
            this.streakCount = 0;
        }
    }
}
