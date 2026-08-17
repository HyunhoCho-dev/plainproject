import "dotenv/config";

// 환경변수를 한 곳에서 읽어 다른 파일이 process.env를 직접 다루지 않게 합니다.
export const config = {
  port: Number(process.env.PORT || 3000),
  openRouterApiKey: process.env.OPENROUTER_API_KEY || "",
  openRouterModel: process.env.OPENROUTER_MODEL || "deepseek/deepseek-v4-flash",
  appUrl: process.env.APP_URL || "http://localhost:3000",
  appName: process.env.APP_NAME || "PLAIN AI Prototype",
  corsOrigins: (process.env.CORS_ORIGINS || "http://127.0.0.1:4173,http://localhost:4173")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean),
};
