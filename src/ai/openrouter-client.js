import { config } from "../config.js";
import { HttpError } from "../utils/http-error.js";

const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

// 재시도 사이에 잠깐 기다리기 위한 작은 도우미입니다.
function wait(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

function parseJson(text) {
  // 일부 모델은 JSON을 코드 블록으로 감싸므로 바깥 표시를 제거합니다.
  const cleaned = text.trim().replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, "");
  try {
    return JSON.parse(cleaned);
  } catch {
    throw new HttpError(502, "AI가 올바른 JSON 형식으로 응답하지 않았습니다.");
  }
}

export class OpenRouterClient {
  constructor(options = {}) {
    this.apiKey = options.apiKey ?? config.openRouterApiKey;
    this.model = options.model ?? config.openRouterModel;
    this.fetch = options.fetch ?? globalThis.fetch;
    this.timeoutMs = options.timeoutMs ?? 30_000;
    this.maxRetries = options.maxRetries ?? 2;
  }

  async generateJson({ system, user, schemaName }) {
    if (!this.apiKey) {
      throw new HttpError(503, "OPENROUTER_API_KEY가 설정되지 않았습니다.");
    }

    // 일시적인 서버 오류나 사용량 제한은 최대 maxRetries만큼 다시 시도합니다.
    for (let attempt = 0; attempt <= this.maxRetries; attempt += 1) {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), this.timeoutMs);

      try {
        // API 키는 브라우저가 아니라 이 백엔드에서만 OpenRouter로 전송합니다.
        const response = await this.fetch(OPENROUTER_URL, {
          method: "POST",
          signal: controller.signal,
          headers: {
            Authorization: `Bearer ${this.apiKey}`,
            "Content-Type": "application/json",
            "HTTP-Referer": config.appUrl,
            "X-Title": config.appName,
          },
          body: JSON.stringify({
            model: this.model,
            temperature: 0.2,
            response_format: { type: "json_object" },
            messages: [
              {
                role: "system",
                content: `${system}\n반드시 유효한 JSON 객체만 반환하세요. 응답 종류: ${schemaName}`,
              },
              { role: "user", content: JSON.stringify(user) },
            ],
          }),
        });

        if (!response.ok) {
          const body = await response.text();
          const retryable = response.status === 429 || response.status >= 500;
          if (retryable && attempt < this.maxRetries) {
            await wait(400 * 2 ** attempt);
            continue;
          }
          throw new HttpError(502, `OpenRouter 요청 실패 (${response.status})`, body.slice(0, 300));
        }

        const data = await response.json();
        const content = data?.choices?.[0]?.message?.content;
        if (typeof content !== "string") {
          throw new HttpError(502, "OpenRouter 응답에 AI 메시지가 없습니다.");
        }
        // 문자열 응답을 JavaScript 객체로 바꾼 뒤 각 기능의 검증기로 넘깁니다.
        return parseJson(content);
      } catch (error) {
        if (error.name === "AbortError") {
          if (attempt < this.maxRetries) continue;
          throw new HttpError(504, "AI 응답 시간이 초과되었습니다.");
        }
        throw error;
      } finally {
        clearTimeout(timer);
      }
    }

    throw new HttpError(502, "AI 요청에 실패했습니다.");
  }
}
