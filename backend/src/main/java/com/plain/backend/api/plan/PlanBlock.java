package com.plain.backend.api.plan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 계획 안의 일정 한 칸입니다. 홈 화면의 plan-block 컴포넌트 하나와 1:1로 대응합니다. */
@Entity
@Table(name = "plan_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyPlan studyPlan;

    @Column(name = "block_date", nullable = false)
    private LocalDate blockDate;

    /** "HH:mm" 형식. AI 응답 형식을 그대로 쓰고 화면에서도 그대로 보여줍니다. */
    @Column(name = "start_time", length = 5)
    private String startTime;

    @Column(name = "end_time", length = 5)
    private String endTime;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 1000)
    private String purpose;

    @Builder.Default
    @Column(nullable = false)
    private Boolean done = false;
}
