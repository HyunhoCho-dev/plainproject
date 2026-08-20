package com.plain.backend.user;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

// =========================================================================================
// [User Repository]
// User 엔티티에 대한 데이터베이스 접근 및 영속성 관리(Persistence Management)를 담당하는
// Data Access Object (DAO) 계층입니다.
// =========================================================================================

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA가 구동 시점에 JpaRepository 상속 인터페이스의 프록시 구현체를 동적으로 생성하여 Bean으로 등록합니다.
    // 식별자(Long) 기반의 기본 CRUD 연산이 자동 제공되며, 필요한 경우 메서드 네이밍 컨벤션을 이용해 커스텀 쿼리 메서드를 정의할 수 있습니다.
}
