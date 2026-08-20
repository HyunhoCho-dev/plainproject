package com.plain.backend.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// [공부포인트] @RestControllerAdvice: 우리 프로젝트 전체에서 일어나는 에러(예외)들을 한 곳으로 모아서 처리해주는 '전역 콜센터'입니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [공부포인트] @ExceptionHandler: 이 메서드가 처리할 특정 에러 종류를 지정합니다.
    // 여기서는 아이디 중복, 비밀번호 틀림 등 우리가 직접 던진 IllegalArgumentException을 가로챕니다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        // 에러 메시지를 예쁜 JSON 상자(Map)에 담습니다.
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());

        // 프론트엔드에게 "이건 서버 잘못이 아니라, 네가 보낸 요청이 잘못됐어!" 라는 의미로 
        // 400 Bad Request 상태 코드와 함께 JSON 응답을 돌려줍니다.
        // 이렇게 하면 프론트엔드가 화면을 멈추지 않고, 이 메시지를 읽어서 유저에게 경고창을 띄워줄 수 있습니다.
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 만약 JWT 토큰 관련 에러 등 다른 예외도 처리하고 싶다면 여기에 계속 추가할 수 있습니다.
}
