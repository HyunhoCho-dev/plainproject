package com.plain.backend.monitoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistractionRepository extends JpaRepository<Distraction, Long> {
    List<Distraction> findByFocusSessionId(Long sessionId);
}
