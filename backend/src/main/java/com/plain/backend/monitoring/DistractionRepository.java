// =========================================================================================
// [1. 패키지 선언]
// =========================================================================================
package com.plain.backend.monitoring;

import org.springframework.data.jpa.repository.JpaRepository; // 만능 DB 조종 리모컨
import org.springframework.stereotype.Repository; // 창고 관리인 명찰

// =========================================================================================
// [2. 본격적인 코드 시작: Distraction(딴짓) 데이터 창고]
// =========================================================================================

// @Repository: 스프링에게 "나 딴짓(Distraction) DB를 전담해서 관리하는 창고지기야!" 라고 이마에 명찰을 붙입니다.
@Repository
// extends JpaRepository<Distraction, Long>: 만능 리모컨 기능을 그대로 물려받아, 저장/조회/삭제 기능을
// 날로 먹습니다!
public interface DistractionRepository extends JpaRepository<Distraction, Long> {

    // 이 중괄호 { } 안이 텅 비어있어도, 스프링이 부팅될 때 알아서 완벽한 DB 조종 코드를 뒤에서 다 채워줍니다.
    // 백엔드 개발자는 그저 이 껍데기(interface)만 만들어두면 됩니다.
}
