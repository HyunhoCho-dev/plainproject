package com.plain.backend.api.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    /** 가장 최근에 만든 계획 하나. 홈 화면이 이걸 불러 그립니다. */
    Optional<StudyPlan> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /** 회원탈퇴에서 이 사용자의 계획을 모두 지울 때 씁니다. */
    java.util.List<StudyPlan> findByUserId(Long userId);
}
