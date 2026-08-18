package com.plain.backend.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 오류를 {"ok":false,"error":{...}} 한 가지 모양으로 바꿉니다.
 *
 * basePackages를 api 패키지로 한정했습니다.
 * 기존 session·monitoring 컨트롤러의 동작에는 영향을 주지 않습니다.
 */
@RestControllerAdvice(basePackages = "com.plain.backend.api")
public class ApiExceptionHandler {

    /** 입력값이 잘못된 경우입니다. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(exception.getMessage()));
    }

    /** 보낸 JSON을 읽지 못한 경우입니다. 보낸 쪽 잘못이므로 400으로 돌려줍니다. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("보낸 데이터를 읽지 못했습니다. JSON 형식과 UTF-8 인코딩을 확인해주세요."));
    }

    /** AI 서버 쪽 문제입니다. 이유를 그대로 화면에 전달합니다. */
    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiDown(AiUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버에서 예상하지 못한 오류가 발생했습니다."));
    }
}
