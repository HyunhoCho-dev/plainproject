import { Router } from "express";

// Express는 async 함수의 오류를 자동으로 처리하지 못할 수 있어 next로 전달합니다.
function asyncRoute(handler) {
  return async (request, response, next) => {
    try {
      const result = await handler(request.body);
      response.json({ ok: true, data: result });
    } catch (error) {
      next(error);
    }
  };
}

export function createAiRouter(aiService) {
  const router = Router();

  // 각 주소는 복잡한 AI 처리 대신 AiService의 한 기능만 호출합니다.
  router.post("/plans/generate", asyncRoute((body) => aiService.createPlan(body)));
  router.post("/distractions/analyze", asyncRoute((body) => aiService.analyzeDistractions(body)));
  router.post("/patterns/analyze", asyncRoute((body) => aiService.analyzePattern(body)));
  router.post("/notifications/judge", asyncRoute((body) => aiService.judgeNotification(body)));
  return router;
}
