package com.plain.backend.api.stats;

import com.plain.backend.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 통계 화면이 열릴 때 부르는 숫자 모음입니다. */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatsDtos.SummaryResponse>> summary(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.summary(userId)));
    }

    /** 쌓인 기록으로 AI 패턴 분석을 요청합니다. 14일이 안 되면 AI를 부르지 않습니다. */
    @PostMapping("/patterns")
    public ResponseEntity<ApiResponse<StatsDtos.PatternResponse>> patterns(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.analyzePatterns(userId)));
    }
}
