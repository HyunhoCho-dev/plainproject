package com.plain.backend.api.plan;

import com.plain.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI가 만들어준 학습 계획입니다.
 * 화면을 새로 고쳐도 계획이 남아 있어야 해서 AI 응답을 흘려보내지 않고 여기에 저장합니다.
 *
 * 새로 만든 테이블이라 기존 users·focus_sessions 등의 구조는 그대로입니다.
 */
@Entity
@Table(name = "study_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String goal;

    @Column(name = "current_level", length = 300)
    private String currentLevel;

    @Column(name = "daily_hours")
    private Double dailyHours;

    @Column(name = "start_date")
    private LocalDate startDate;

    /** AI가 요약한 한두 문장 */
    @Column(length = 1000)
    private String summary;

    /** 예상 목표 달성 기간(주) */
    @Column(name = "estimated_weeks")
    private Integer estimatedWeeks;

    /** AI 처방 코멘트. 줄바꿈으로 이어 붙여 저장합니다. */
    @Column(length = 2000)
    private String advice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("blockDate asc, startTime asc")
    private List<PlanBlock> blocks = new ArrayList<>();

    public void addBlock(PlanBlock block) {
        block.setStudyPlan(this);
        this.blocks.add(block);
    }
}
