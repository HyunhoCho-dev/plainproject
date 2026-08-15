import express from "express";
import { AiService } from "./ai/ai-service.js";
import { OpenRouterClient } from "./ai/openrouter-client.js";
import { createAiRouter } from "./routes/ai-routes.js";
import { HttpError } from "./utils/http-error.js";

export function createApp(options = {}) {
  const app = express();
  const client = options.client ?? new OpenRouterClient();
  const aiService = options.aiService ?? new AiService(client);
  app.disable("x-powered-by");
  app.use(express.json({ limit: "200kb" }));
  app.get("/health", (_request, response) => response.json({ ok: true, service: "plainproject-ai" }));
  app.use("/api/ai", createAiRouter(aiService));
  app.use((_request, _response, next) => next(new HttpError(404, "요청한 API를 찾을 수 없습니다.")));
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
