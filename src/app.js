import express from "express";
import cors from "cors";
import { AiService } from "./ai/ai-service.js";
import { OpenRouterClient } from "./ai/openrouter-client.js";
import { createAiRouter } from "./routes/ai-routes.js";
import { HttpError } from "./utils/http-error.js";
import { config } from "./config.js";

// 테스트에서도 같은 앱을 만들 수 있도록 서버 실행과 Express 설정을 분리했습니다.
export function createApp(options = {}) {
  const app = express();
  const client = options.client ?? new OpenRouterClient();
  const aiService = options.aiService ?? new AiService(client);
  app.disable("x-powered-by");

  // 허용 목록에 있는 프론트엔드 주소만 브라우저에서 API를 호출할 수 있습니다.
  app.use(cors({
    origin(origin, callback) {
      if (!origin || config.corsOrigins.includes(origin)) return callback(null, true);
      return callback(new HttpError(403, "허용되지 않은 프론트엔드 주소입니다."));
    },
  }));

  // 너무 큰 JSON 요청이 서버 메모리와 AI 비용을 낭비하지 못하게 제한합니다.
  app.use(express.json({ limit: "200kb" }));
  app.get("/health", (_request, response) => response.json({ ok: true, service: "plainproject-ai" }));
  app.use("/api/ai", createAiRouter(aiService));
  app.use((_request, _response, next) => next(new HttpError(404, "요청한 API를 찾을 수 없습니다.")));
  // 모든 오류 응답의 모양을 동일하게 만들어 프론트엔드가 쉽게 처리하게 합니다.
  app.use((error, _request, response, _next) => {
    const status = Number.isInteger(error.status) ? error.status : 500;
    const message = status >= 500 && !(error instanceof HttpError)
      ? "서버에서 예상하지 못한 오류가 발생했습니다."
      : error.message;
    if (status >= 500) console.error(error);
    response.status(status).json({ ok: false, error: { message, ...(error.details ? { details: error.details } : {}) } });
  });
  return app;
}
