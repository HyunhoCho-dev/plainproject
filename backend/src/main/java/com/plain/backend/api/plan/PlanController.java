package com.plain.backend.api.plan;

import com.plain.backend.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** 목표 입력 3단계를 마치면 호출됩니다. Spring이 AI 서버를 부르고 결과를 저장합니다. */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PlanDtos.PlanResponse>> generate(
            @RequestBody PlanDtos.GenerateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(planService.generate(request)));
    }

    /** 홈 화면이 열릴 때 지금 쓰는 계획을 불러옵니다. 계획이 없으면 data가 없습니다. */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<PlanDtos.PlanResponse>> current(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findCurrent(userId)));
    }

    /** 일정 완료 체크 토글 */
    @PatchMapping("/blocks/{blockId}")
    public ResponseEntity<ApiResponse<PlanDtos.BlockResponse>> setDone(
            @PathVariable Long blockId,
            @RequestParam boolean done) {
        return ResponseEntity.ok(ApiResponse.ok(planService.setBlockDone(blockId, done)));
    }
}
