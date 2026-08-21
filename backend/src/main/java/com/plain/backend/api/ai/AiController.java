package com.plain.backend.api.ai;

import com.plain.backend.api.common.AiClient;
import com.plain.backend.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 화면이 부르는 AI 주소입니다.
 * 주소 모양은 AI 서버(ai/docs/API.md)와 똑같이 맞췄고, 내부적으로는 Spring이 대신 호출합니다.
 *
 * 계획 생성만 결과를 저장해야 해서 PlanController가 따로 맡습니다.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiClient aiClient;

    /** 사용 기록을 바탕으로 차단을 추천받습니다. 추천일 뿐 실제 차단은 사용자가 정합니다. */
    @PostMapping("/distractions/analyze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeDistractions(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(aiClient.post("/api/ai/distractions/analyze", body)));
    }

    /** 이 앱을 왜 차단 대상으로 추천했는지 근거를 받아옵니다. 사용 기록 없이 앱 성격과 목표만 씁니다. */
    @PostMapping("/apps/judge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> judgeApp(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(aiClient.post("/api/ai/apps/judge", body)));
    }

    /** 알림 중요도를 판정합니다. 알림 본문은 보내지 않습니다. */
    @PostMapping("/notifications/judge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> judgeNotification(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(aiClient.post("/api/ai/notifications/judge", body)));
    }
}
