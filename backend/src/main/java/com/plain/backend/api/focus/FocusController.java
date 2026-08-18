package com.plain.backend.api.focus;

import com.plain.backend.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 화면이 쓰는 세션 주소입니다.
 *
 * 기존 /api/session/* 은 그대로 두고 /api/focus/* 를 새로 열었습니다.
 * 기존 주소는 값을 쿼리 파라미터로 받고 엔티티를 그대로 반환해서 화면이 쓰기 어렵기 때문입니다.
 */
@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusController {

    private final FocusService focusService;

    /** 타이머 화면이 열릴 때 세션을 시작합니다. */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<FocusDtos.SessionResponse>> start(
            @RequestBody FocusDtos.StartRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(focusService.start(request)));
    }

    /** 종료를 누르면 실제 집중 시간이 계산되어 저장됩니다. */
    @PostMapping("/end")
    public ResponseEntity<ApiResponse<FocusDtos.SessionResponse>> end(
            @RequestBody FocusDtos.EndRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(focusService.end(request.sessionId())));
    }

    /** 타이머 화면 아래의 오늘 완료한 세션 목록입니다. */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<FocusDtos.SessionResponse>>> today(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(focusService.findTodayFinished(userId)));
    }
}
