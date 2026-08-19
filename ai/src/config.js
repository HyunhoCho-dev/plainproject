import "dotenv/config";

// 환경변수를 한 곳에서 읽어 다른 파일이 process.env를 직접 다루지 않게 합니다.
export const config = {
  port: Number(process.env.PORT || 3000),
  openRouterApiKey: process.env.OPENROUTER_API_KEY || "",
  openRouterModel: process.env.OPENROUTER_MODEL || "deepseek/deepseek-v4-flash",
  // 계획처럼 긴 JSON 응답은 30초보다 오래 걸릴 수 있어 충분히 기다립니다.
  openRouterTimeoutMs: Number(process.env.OPENROUTER_TIMEOUT_MS || 120_000),
  // Spring이 180초 안에 응답을 기다리므로 120초 요청을 자동 반복하지 않습니다.
  // 실패했을 때는 화면의 "다시 시도"로 사용자가 명확하게 재요청합니다.
  openRouterMaxRetries: Number(process.env.OPENROUTER_MAX_RETRIES || 0),
  appUrl: process.env.APP_URL || "http://localhost:3000",
  appName: process.env.APP_NAME || "PLAIN AI Prototype",
  corsOrigins: (process.env.CORS_ORIGINS || "http://127.0.0.1:4173,http://localhost:4173")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean),
};
