package com.plain.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository (저장소): 데이터베이스에 직접 접근해서 데이터를 CRUD 하는 역할.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
