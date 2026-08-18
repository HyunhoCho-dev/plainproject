package com.plain.backend.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 연결용 API가 모두 같은 모양으로 응답하게 만드는 껍데기입니다.
 *
 * AI 서버(ai 폴더)가 이미 {"ok":true,"data":...} 형식을 쓰고 있어서 같은 형식을 맞췄습니다.
 * 화면은 응답 모양 하나만 알면 백엔드와 AI 결과를 모두 처리할 수 있습니다.
 *
 * 이 클래스는 새로 추가한 api 패키지 안에서만 씁니다.
 * 기존 session·monitoring 컨트롤러의 응답 형식은 건드리지 않았습니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean ok, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, new ApiError(message));
    }

    public record ApiError(String message) {}
}
