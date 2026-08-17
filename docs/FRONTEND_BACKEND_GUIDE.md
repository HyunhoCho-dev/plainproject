# 프론트엔드와 AI 백엔드 연결 가이드

이 문서는 백엔드가 처음인 팀원도 PLAIN 프론트엔드에서 AI API를 호출할 수 있도록 작성했습니다.

## 1. 전체 흐름

```text
사용자가 버튼 클릭
  → 프론트엔드 JavaScript가 입력값 수집
  → fetch로 AI 백엔드에 JSON 전송
  → 백엔드가 입력 검증
  → OpenRouter를 통해 DeepSeek V4 Flash 호출
  → 백엔드가 AI 결과 검증
  → 프론트엔드가 결과를 화면에 표시
```

OpenRouter API 키는 반드시 백엔드에만 둡니다. 프론트엔드 HTML이나 JavaScript에 키를 넣으면 방문자가 키를 볼 수 있습니다.

## 2. 백엔드 실행

```bash
npm install
copy .env.example .env
npm run dev
```

`.env`에 실제 키를 입력합니다.

```env
OPENROUTER_API_KEY=본인의_키
OPENROUTER_MODEL=deepseek/deepseek-v4-flash
PORT=3000
CORS_ORIGINS=http://127.0.0.1:4173,http://localhost:4173
```

브라우저에서 `http://localhost:3000/health`를 열어 아래 응답이 나오면 준비가 끝난 것입니다.

```json
{"ok":true,"service":"plainproject-ai"}
```

## 3. 프론트엔드 공통 API 함수

프론트엔드의 `shared/ai-api.js` 같은 공통 파일에 아래 코드를 둡니다.

```js
const AI_API_URL = "http://localhost:3000/api/ai";

export async function callAiApi(path, body) {
  const response = await fetch(`${AI_API_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const result = await response.json();

  if (!response.ok) {
    throw new Error(result.error?.message || "AI 요청에 실패했습니다.");
  }

  return result.data;
}
```

이 함수를 사용하면 모든 화면에서 `fetch`, 오류 확인, JSON 변환을 반복하지 않아도 됩니다.

## 4. 계획 생성 화면 연결

```js
import { callAiApi } from "../shared/ai-api.js";

async function makePlan() {
  const button = document.querySelector("#make-plan-button");
  const errorBox = document.querySelector("#error-message");

  button.disabled = true;
  button.textContent = "계획을 만들고 있어요...";
  errorBox.textContent = "";

  try {
    const plan = await callAiApi("/plans/generate", {
      goal: document.querySelector("#goal").value,
      currentLevel: document.querySelector("#current-level").value,
      dailyHours: Number(document.querySelector("#daily-hours").value),
      startDate: new Date().toISOString().slice(0, 10),
      constraints: document.querySelector("#constraints").value,
    });

    // 프로토타입에서는 확인을 위해 저장합니다.
    // 실제 서비스에서는 백엔드 데이터베이스에 저장해야 합니다.
    sessionStorage.setItem("generatedPlan", JSON.stringify(plan));
    location.href = "plan-result.html";
  } catch (error) {
    errorBox.textContent = error.message;
  } finally {
    button.disabled = false;
    button.textContent = "AI 계획 만들기";
  }
}
```

## 5. 다른 AI 기능 호출

### 방해 앱 분석

```js
const result = await callAiApi("/distractions/analyze", {
  goal: "영어 공부",
  periodDays: 14,
  apps: collectedAppStats,
});

renderRecommendedApps(result.recommendations);
```

### 패턴 분석

```js
const result = await callAiApi("/patterns/analyze", {
  dailyStats: last14Days,
});

document.querySelector("#ai-insight").textContent = result.insight;
```

`dailyStats`는 최소 14개가 필요합니다. 그보다 적으면 프론트엔드에서 “아직 데이터가 부족해요”를 표시합니다.

### 알림 중요도 판단

```js
const result = await callAiApi("/notifications/judge", {
  appId: "com.example.calendar",
  appCategory: "calendar",
  focusGoal: "수학 문제 풀이",
});

if (result.action === "allow") showNotification();
if (result.action === "block") saveBlockedNotification();
if (result.action === "review") askUserToChoose();
```

AI 결과만으로 알림이나 앱을 강제로 차단하지 않고 사용자의 설정과 선택을 우선해야 합니다.

## 6. 화면에서 반드시 처리할 상태

각 AI 화면은 다음 네 가지 상태를 보여줘야 합니다.

1. 입력 전: 버튼을 누를 수 있는 기본 화면
2. 로딩 중: 중복 요청 방지를 위해 버튼 비활성화
3. 성공: AI 결과 표시
4. 실패: `계획을 만드는 데 문제가 생겼어요`와 다시 시도 버튼 표시

네트워크가 끊기면 상단에 `오프라인 상태예요, 일부 기능이 제한돼요` 배너를 표시합니다.

## 7. 자주 발생하는 문제

### CORS 오류

프론트엔드 주소가 `.env`의 `CORS_ORIGINS`에 포함되어 있는지 확인하고 백엔드를 다시 시작합니다.

### 503과 API 키 오류

백엔드 `.env`에 `OPENROUTER_API_KEY`가 있는지 확인합니다. 키를 프론트엔드로 복사하면 안 됩니다.

### 400 오류

프론트엔드에서 보낸 필드 이름과 자료형이 API 예시와 같은지 확인합니다. 특히 `dailyHours`는 문자열이 아닌 숫자여야 합니다.

### AI JSON 오류

AI가 형식을 지키지 못하면 백엔드가 502 오류를 반환합니다. 프론트엔드는 사용자에게 다시 시도 버튼을 보여줍니다.

## 8. 팀 작업 권장 순서

1. AI 백엔드를 포트 3000에서 실행
2. 프론트엔드를 포트 4173에서 실행
3. 공통 `callAiApi` 함수 작성
4. 목표 입력 화면과 계획 생성 API 연결
5. 계획 결과 화면에 응답 표시
6. 방해 앱·통계·알림 화면을 차례로 연결
7. 실제 서버와 데이터베이스가 준비되면 `sessionStorage`를 API 저장 방식으로 교체

