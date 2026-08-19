package com.plain.backend.monitoring;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
import java.util.List; 

// =========================================================================================
// [Notification Repository]
// Notification 엔티티에 대한 데이터베이스 접근 및 영속성 관리(Persistence Management)를 담당하는
// Data Access Object (DAO) 계층입니다.
// =========================================================================================

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Spring Data JPA의 Query Method 기능을 활용한 커스텀 조회 메서드 선언입니다.
    // 메서드 시그니처 네이밍 규칙(findBy + 필드명)에 따라 실행 시점에 적절한 JPQL 쿼리가 자동 생성됩니다.
    // 외래키(session_id)를 기준으로 연관된 Notification 목록을 조회합니다.
    List<Notification> findByFocusSessionId(Long sessionId);
}
