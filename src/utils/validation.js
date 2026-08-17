import { HttpError } from "./http-error.js";

// 아래 함수들은 OpenRouter에 보내기 전에 요청 데이터의 형식과 크기를 검사합니다.
export function requireObject(value, fieldName = "요청 본문") {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    throw new HttpError(400, `${fieldName}은 객체여야 합니다.`);
  }
  return value;
}

export function requireString(value, fieldName, options = {}) {
  const { min = 1, max = 500 } = options;
  if (typeof value !== "string") {
    throw new HttpError(400, `${fieldName}은 문자열이어야 합니다.`);
  }

  const cleaned = value.trim();
  if (cleaned.length < min || cleaned.length > max) {
    throw new HttpError(400, `${fieldName}은 ${min}~${max}자여야 합니다.`);
  }
  return cleaned;
}

export function requireNumber(value, fieldName, options = {}) {
  const { min = 0, max = Number.MAX_SAFE_INTEGER } = options;
  if (typeof value !== "number" || !Number.isFinite(value)) {
    throw new HttpError(400, `${fieldName}은 숫자여야 합니다.`);
  }
  if (value < min || value > max) {
    throw new HttpError(400, `${fieldName}은 ${min}~${max} 범위여야 합니다.`);
  }
  return value;
}

export function requireArray(value, fieldName, options = {}) {
  const { min = 1, max = 100 } = options;
  if (!Array.isArray(value) || value.length < min || value.length > max) {
    throw new HttpError(400, `${fieldName}은 ${min}~${max}개 항목의 배열이어야 합니다.`);
  }
  return value;
}

export function optionalString(value, fieldName, max = 500) {
  if (value === undefined || value === null || value === "") return undefined;
  return requireString(value, fieldName, { max });
}
