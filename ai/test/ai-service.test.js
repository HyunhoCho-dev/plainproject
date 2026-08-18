import assert from "node:assert/strict";
import test from "node:test";
import { AiService } from "../src/ai/ai-service.js";

class FakeClient {
  constructor(result) { this.result = result; this.lastRequest = null; }
  async generateJson(request) { this.lastRequest = request; return this.result; }
}

test("주간 계획을 생성하고 결과 형식을 검증한다", async () => {
  const client = new FakeClient({
    summary: "기초부터 시작하는 일주일 계획",
    estimatedWeeks: 8,
    days: [{ date: "2026-08-17", blocks: [{ start: "19:00", end: "20:00", title: "기초 학습", purpose: "개념 이해" }] }],
    advice: ["매일 복습하세요"],
  });
  const result = await new AiService(client).createPlan({ goal: "JavaScript 기초 학습", currentLevel: "초보", dailyHours: 1, startDate: "2026-08-17" });
  assert.equal(result.days[0].blocks[0].title, "기초 학습");
  assert.equal(client.lastRequest.schemaName, "weekly_plan");
});

test("14일 미만 패턴 데이터는 AI 호출 전에 거부한다", async () => {
  const service = new AiService(new FakeClient({}));
  await assert.rejects(() => service.analyzePattern({ dailyStats: [{ date: "2026-08-01" }] }), /14~90개/);
});

test("방해 앱 분석은 사용자 식별 정보 없이 집계 데이터만 전달한다", async () => {
  const client = new FakeClient({ recommendations: [{ appId: "youtube", action: "review", reason: "집중 중 4회 사용", confidence: 0.65 }], note: "사용자가 최종 선택해야 합니다." });
  await new AiService(client).analyzeDistractions({ goal: "영어 공부", apps: [{ appId: "youtube", category: "video", focusUseCount: 4, exitsAfterOpen: 2, totalMinutes: 50 }] });
  assert.equal(client.lastRequest.user.apps[0].appId, "youtube");
  assert.equal("email" in client.lastRequest.user, false);
});
