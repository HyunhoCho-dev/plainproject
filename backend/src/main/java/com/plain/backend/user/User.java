package com.plain.backend.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entity (엔티티): 데이터베이스의 테이블과 1:1로 매칭되는 자바 클래스입니다.
 */
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
    private Long id; // 식별자

    @Column(nullable = false, unique = true)
    private String username; // 사용자 이름

    @Column(nullable = false, unique = true)
    private String email; // 사용자 이메일

    @Column(nullable = false)
    private String password; // 비밀번호
}
