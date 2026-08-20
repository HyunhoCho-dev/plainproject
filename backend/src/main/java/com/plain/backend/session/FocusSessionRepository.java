package com.plain.backend.session;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

// =========================================================================================
// [Focus Session Repository]
// FocusSession 엔티티에 대한 데이터베이스 접근 및 영속성 관리(Persistence Management)를 
// 담당하는 Data Access Object (DAO) 계층입니다.
// =========================================================================================

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    // Spring Data JPA가 JpaRepository를 상속받은 인터페이스에 대해 프록시 객체를 동적으로 생성하여 
    // 의존성을 주입합니다. 기본적으로 제공되는 CRUD 및 페이징/정렬 기능 외에 추가적인 
    // Query Method 선언이 필요한 경우 이곳에 정의할 수 있습니다.
}
