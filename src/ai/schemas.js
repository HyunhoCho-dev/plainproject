import { HttpError } from "../utils/http-error.js";

function assert(condition, message) {
  if (!condition) throw new HttpError(502, `AI 응답 검증 실패: ${message}`);
}

export function validatePlan(result) {
  assert(result && typeof result === "object", "객체가 아닙니다.");
  assert(typeof result.summary === "string", "summary가 필요합니다.");
  assert(Array.isArray(result.days) && result.days.length > 0, "days 배열이 필요합니다.");

  for (const day of result.days) {
    assert(typeof day.date === "string", "각 날짜가 필요합니다.");
    assert(Array.isArray(day.blocks), "각 날짜의 blocks 배열이 필요합니다.");
    for (const block of day.blocks) {
      assert(typeof block.title === "string", "일정 제목이 필요합니다.");
      assert(/^\d{2}:\d{2}$/.test(block.start), "시작 시간은 HH:mm 형식이어야 합니다.");
      assert(/^\d{2}:\d{2}$/.test(block.end), "종료 시간은 HH:mm 형식이어야 합니다.");
    }
  }
  return result;
}

export function validateDistractions(result) {
  assert(Array.isArray(result.recommendations), "recommendations 배열이 필요합니다.");
  for (const item of result.recommendations) {
    assert(typeof item.appId === "string", "appId가 필요합니다.");
    assert(["block", "allow", "review"].includes(item.action), "action 값이 잘못되었습니다.");
    assert(typeof item.reason === "string", "추천 사유가 필요합니다.");
    assert(typeof item.confidence === "number", "confidence가 필요합니다.");
  }
  return result;
}

export function validatePattern(result) {
  assert(typeof result.insight === "string", "insight가 필요합니다.");
  assert(Array.isArray(result.actions), "actions 배열이 필요합니다.");
  return result;
}

export function validateNotification(result) {
  assert(["allow", "block", "review"].includes(result.action), "action 값이 잘못되었습니다.");
  assert(typeof result.reason === "string", "reason이 필요합니다.");
  assert(typeof result.confidence === "number", "confidence가 필요합니다.");
  return result;
}

