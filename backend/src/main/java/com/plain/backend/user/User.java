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

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // 기본 키(Primary Key). 데이터베이스의 Auto Increment 기능(IDENTITY)을 위임받아 식별자를 생성합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    // 사용자 식별 아이디 (유저 네임). 고유해야 하며(unique), 필수 값(nullable = false)입니다.
    @Column(nullable = false, unique = true)
    private String username; 

    // 사용자 이메일 주소. 고유해야 하며, 필수 값입니다.
    @Column(nullable = false, unique = true)
    private String email; 

    // 암호화된 사용자 비밀번호 해시.
    @Column(nullable = false)
    private String password; 
}
