# API 사용 예시

모든 성공 응답은 `{ "ok": true, "data": ... }`, 실패 응답은 `{ "ok": false, "error": ... }` 형식입니다.

## 계획 생성

`POST /api/ai/plans/generate`

```json
{"goal":"정보처리기사 시험 준비","currentLevel":"기초 개념을 조금 알고 있음","dailyHours":2,"startDate":"2026-08-17","constraints":"평일은 오후 7시 이후 가능"}
```

## 방해 앱 분석

`POST /api/ai/distractions/analyze`

```json
{"goal":"영어 공부","periodDays":14,"apps":[{"appId":"youtube","category":"video","focusUseCount":8,"exitsAfterOpen":3,"totalMinutes":120}]}
```

## 패턴 분석

`POST /api/ai/patterns/analyze`에 아래 구조의 `dailyStats`를 최소 14개 전달합니다.

```json
{"dailyStats":[{"date":"2026-08-01","studyMinutes":90,"plannedMinutes":120,"completionRate":0.75,"blockedCount":4}]}
```

## 알림 중요도 판정

`POST /api/ai/notifications/judge`

```json
{"appId":"com.example.calendar","appCategory":"calendar","focusGoal":"수학 문제 풀이"}
```

개인정보 보호를 위해 알림 본문은 받지 않습니다.
