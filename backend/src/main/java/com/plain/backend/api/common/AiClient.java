package com.plain.backend.api.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Node로 만든 AI 서버(ai 폴더)를 호출하는 유일한 통로입니다.
 *
 * 브라우저는 AI 서버를 직접 부르지 않습니다. 항상 Spring을 거칩니다.
 * - OpenRouter API 키가 브라우저에 노출되지 않습니다
 * - AI 결과를 그대로 DB에 저장할 수 있습니다
 * - 화면이 알아야 할 주소가 8080 하나로 줄어듭니다
 *
 * API 키는 이 프로젝트가 다루지 않습니다. ai 폴더의 .env가 갖고 있습니다.
 */
@Component
public class AiClient {

    private final RestClient restClient;

    public AiClient(
            @Value("${plain.ai.base-url:http://localhost:3000}") String baseUrl,
            @Value("${plain.ai.timeout-seconds:60}") int timeoutSeconds) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        // AI 계획 생성은 20초 이상 걸릴 수 있어 읽기 제한 시간을 넉넉히 둡니다.
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * AI 서버의 {"ok":true,"data":...} 응답에서 data만 꺼내 돌려줍니다.
     * 실패하면 AiUnavailableException으로 바꿔 던져, 화면이
     * "계획을 만드는 데 문제가 생겼어요"를 띄울 수 있게 합니다.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> post(String path, Map<String, Object> body) {
        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    // 오류 응답에도 이유가 담겨 있다. 여기서 예외를 던지면 그 이유를 잃어버리므로
                    // 상태 코드와 관계없이 본문을 읽고, 아래에서 ok 값을 보고 판단한다.
                    .onStatus(status -> status.isError(), (request, errorResponse) -> { })
                    .body(Map.class);
        } catch (Exception exception) {
            throw new AiUnavailableException(
                    "AI 서버와 통신하지 못했습니다. ai 폴더의 서버가 켜져 있는지 확인하세요.", exception);
        }

        if (response == null) {
            throw new AiUnavailableException("AI 서버가 빈 응답을 보냈습니다.");
        }
        if (!Boolean.TRUE.equals(response.get("ok"))) {
            Object error = response.get("error");
            String message = error instanceof Map<?, ?> map && map.get("message") != null
                    ? String.valueOf(map.get("message"))
                    : "AI 처리에 실패했습니다.";
            throw new AiUnavailableException(message);
        }

        Object data = response.get("data");
        if (!(data instanceof Map)) {
            throw new AiUnavailableException("AI 응답 형식이 올바르지 않습니다.");
        }
        return (Map<String, Object>) data;
    }
}
