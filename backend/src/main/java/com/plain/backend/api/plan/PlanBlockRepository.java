package com.plain.backend.api.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanBlockRepository extends JpaRepository<PlanBlock, Long> {
}
