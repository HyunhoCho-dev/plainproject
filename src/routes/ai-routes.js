import { Router } from "express";

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
  router.post("/plans/generate", asyncRoute((body) => aiService.createPlan(body)));
  router.post("/distractions/analyze", asyncRoute((body) => aiService.analyzeDistractions(body)));
  router.post("/patterns/analyze", asyncRoute((body) => aiService.analyzePattern(body)));
  router.post("/notifications/judge", asyncRoute((body) => aiService.judgeNotification(body)));
  return router;
}
