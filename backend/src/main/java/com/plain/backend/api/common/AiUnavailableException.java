package com.plain.backend.api.common;

/** AI 서버(ai 폴더)가 꺼져 있거나 오류를 돌려줬을 때 쓰는 예외입니다. */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
