package com.plain.backend.api.focus;

import com.plain.backend.session.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 기존 FocusSessionRepository를 고치지 않기 위해 따로 둔 조회용 저장소입니다.
 * 같은 FocusSession 엔티티를 보며, 화면에 필요한 조회 메서드만 갖고 있습니다.
 */
@Repository
public interface FocusRepository extends JpaRepository<FocusSession, Long> {

    List<FocusSession> findByUserIdOrderByStartTimeDesc(Long userId);
}
