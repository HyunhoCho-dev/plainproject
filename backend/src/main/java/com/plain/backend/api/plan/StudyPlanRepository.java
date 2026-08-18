package com.plain.backend.api.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    /** 가장 최근에 만든 계획 하나. 홈 화면이 이걸 불러 그립니다. */
    Optional<StudyPlan> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
