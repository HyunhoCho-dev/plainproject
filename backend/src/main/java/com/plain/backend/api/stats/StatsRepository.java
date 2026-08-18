package com.plain.backend.api.stats;

import com.plain.backend.monitoring.Distraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 기존 DistractionRepository를 고치지 않기 위해 따로 둔 집계용 저장소입니다.
 * 한 사용자의 방해 기록 전체를 날짜별로 세는 데 씁니다.
 */
@Repository
public interface StatsRepository extends JpaRepository<Distraction, Long> {

    List<Distraction> findByFocusSessionUserId(Long userId);
}
