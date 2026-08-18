package com.plain.backend.api.account;

import com.plain.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 기존 UserRepository를 고치지 않기 위해 따로 둔 조회용 저장소입니다.
 * 같은 User 엔티티를 보며, 로그인에 필요한 조회 메서드만 추가로 갖고 있습니다.
 */
@Repository
public interface AccountRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
