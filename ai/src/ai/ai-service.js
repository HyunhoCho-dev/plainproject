import {
  optionalString,
  requireArray,
  requireNumber,
  requireObject,
  requireString,
} from "../utils/validation.js";
import {
  validateAppBlock,
  validateDistractions,
  validateNotification,
  validatePattern,
  validatePlan,
} from "./schemas.js";

const PRIVACY_RULE = "이름, 이메일, 알림 본문 등 불필요한 개인정보를 요구하거나 추측하지 마세요.";

/** 계획 한 번에 만드는 일수. 홈 화면의 주간 캘린더 칸 수와 같습니다. */
const PLAN_DAYS = 7;

/** 시작일부터 연속된 날짜 목록. 시차 영향을 받지 않도록 UTC로 계산합니다. */
function planDates(startDate, count) {
  const start = new Date(`${startDate}T00:00:00Z`);
  if (Number.isNaN(start.getTime())) return [];
  return Array.from({ length: count }, (_, index) =>
    new Date(start.getTime() + index * 86_400_000).toISOString().slice(0, 10));
}

// AiService는 입력 검증 → 프롬프트 구성 → 결과 검증 순서를 담당합니다.
// HTTP나 데이터베이스 코드를 넣지 않아 각 AI 기능을 독립적으로 테스트할 수 있습니다.
export class AiService {
  constructor(client) {
    this.client = client;
  }

  async createPlan(input) {
    // 사용자의 목표와 가능한 시간을 정리해 주간·일일 계획을 요청합니다.
    requireObject(input);
    const goal = requireString(input.goal, "goal", { max: 500 });
    const currentLevel = requireString(input.currentLevel, "currentLevel", { max: 300 });
    const dailyHours = requireNumber(input.dailyHours, "dailyHours", { min: 0.5, max: 16 });
    const startDate = requireString(input.startDate, "startDate", { max: 10 });
    const constraints = optionalString(input.constraints, "constraints", 500);

    // "7일치를 만드세요"라고만 하면 모델이 3~4일만 주고 끝내는 일이 있습니다.
    // 그래서 날짜를 직접 계산해 목록으로 넘기고, 그 날짜를 모두 채우게 합니다.
    const dates = planDates(startDate, PLAN_DAYS);

    const result = await this.client.generateJson({
      schemaName: "weekly_plan",
      system: `당신은 현실적인 학습·업무 계획을 만드는 코치입니다. ${PRIVACY_RULE}
출력 형식: {"summary":"...","estimatedWeeks":숫자,"days":[{"date":"YYYY-MM-DD","blocks":[{"start":"HH:mm","end":"HH:mm","title":"...","purpose":"..."}]}],"advice":["..."]}.
휴식 시간을 포함하고 하루 총 계획 시간이 dailyHours를 넘지 않게 하세요.
days 배열은 입력의 dates에 있는 ${PLAN_DAYS}개 날짜를 그 순서대로 모두 포함해야 합니다.
날짜를 빠뜨리거나 합치지 마세요. days의 길이는 정확히 ${PLAN_DAYS}이어야 합니다.
title과 purpose는 goal에 적힌 활동에서 직접 나와야 합니다.
goal이 시험 준비가 아니면 '기출', '모의고사', '오답 노트' 같은 시험 용어를 쓰지 마세요.
예: goal이 요리라면 재료 손질·조리 실습처럼 그 분야에서 실제로 하는 일을 적습니다.
날마다 내용이 달라야 합니다. 같은 제목을 반복하지 말고 단계가 진행되게 배치하세요.`,
      user: { goal, currentLevel, dailyHours, startDate, dates, constraints },
    });
    return validatePlan(result);
  }

  async analyzeDistractions(input) {
    // 앱 이름 대신 집계 수치를 보내 개인정보 노출을 줄입니다.
    requireObject(input);
    const goal = requireString(input.goal, "goal", { max: 500 });
    const apps = requireArray(input.apps, "apps", { max: 100 }).map((app, index) => {
      requireObject(app, `apps[${index}]`);
      return {
        appId: requireString(app.appId, `apps[${index}].appId`, { max: 100 }),
        category: optionalString(app.category, `apps[${index}].category`, 100),
        focusUseCount: requireNumber(app.focusUseCount, `apps[${index}].focusUseCount`, { max: 100000 }),
        exitsAfterOpen: requireNumber(app.exitsAfterOpen, `apps[${index}].exitsAfterOpen`, { max: 100000 }),
        totalMinutes: requireNumber(app.totalMinutes, `apps[${index}].totalMinutes`, { max: 1_000_000 }),
      };
    });

    const result = await this.client.generateJson({
      schemaName: "distraction_recommendations",
      system: `당신은 집중 방해 앱을 추천하는 분석기입니다. ${PRIVACY_RULE}
AI는 강제 차단을 결정하지 않고 추천만 합니다.
출력 형식: {"recommendations":[{"appId":"...","action":"block|allow|review","category":"...","reason":"정량적 근거","confidence":0.0}],"note":"..."}.
confidence는 0~1 사이 숫자이며 근거가 부족하면 review를 선택하세요.`,
      user: { goal, periodDays: input.periodDays ?? 14, apps },
    });
    return validateDistractions(result);
  }

  async analyzePattern(input) {
    // 의미 있는 패턴을 만들 수 있도록 최소 14일치 통계만 허용합니다.
    requireObject(input);
    const dailyStats = requireArray(input.dailyStats, "dailyStats", { min: 14, max: 90 });
    const safeStats = dailyStats.map((day, index) => {
      requireObject(day, `dailyStats[${index}]`);
      return {
        date: requireString(day.date, `dailyStats[${index}].date`, { max: 10 }),
        studyMinutes: requireNumber(day.studyMinutes, `dailyStats[${index}].studyMinutes`, { max: 1440 }),
        plannedMinutes: requireNumber(day.plannedMinutes, `dailyStats[${index}].plannedMinutes`, { max: 1440 }),
        completionRate: requireNumber(day.completionRate, `dailyStats[${index}].completionRate`, { min: 0, max: 1 }),
        blockedCount: requireNumber(day.blockedCount, `dailyStats[${index}].blockedCount`, { max: 10000 }),
      };
    });

    const result = await this.client.generateJson({
      schemaName: "pattern_insight",
      system: `당신은 14일 이상의 집중 통계를 분석하는 코치입니다. ${PRIVACY_RULE}
관찰 사실과 추측을 구분하고 과장하지 마세요.
출력 형식: {"insight":"1~2문장","patterns":[{"title":"...","evidence":"..."}],"actions":["실행 가능한 조언"],"planAdjustment":{"recommended":true,"reason":"..."}}.`,
      user: { dailyStats: safeStats },
    });
    return validatePattern(result);
  }

  async judgeAppBlock(input) {
    // 사용 기록을 수집하는 기능이 아직 없습니다. 그래서 앱의 성격과 목표만으로 판단하고,
    // 측정하지 않은 수치를 지어내지 않도록 프롬프트에서 명시적으로 막습니다.
    requireObject(input);
    const appId = requireString(input.appId, "appId", { max: 100 });
    const appCategory = optionalString(input.appCategory, "appCategory", 100);
    const focusGoal = requireString(input.focusGoal, "focusGoal", { max: 500 });

    const result = await this.client.generateJson({
      schemaName: "app_block_reason",
      system: `당신은 집중을 방해할 수 있는 앱을 평가합니다. ${PRIVACY_RULE}
사용 기록이 없으므로 앱의 성격과 사용자의 목표만으로 판단합니다.
사용 횟수, 이용 시간, 비율처럼 측정해야 알 수 있는 수치를 지어내지 마세요.
AI는 강제 차단을 결정하지 않고 추천만 합니다. 근거가 약하면 review를 고르세요.
출력 형식: {"action":"block|allow|review","reason":"2문장 이내","confidence":0.0,"factors":[{"title":"짧은 제목","detail":"1문장"}]}.
factors는 2~3개이며 판단을 항목으로 나눈 것입니다. 목표와의 관련성을 반드시 하나 넣으세요.`,
      user: { appId, appCategory, focusGoal },
    });
    return validateAppBlock(result);
  }

  async judgeNotification(input) {
    // 알림 본문은 보내지 않고 앱 종류와 현재 집중 목표만 사용합니다.
    requireObject(input);
    const appId = requireString(input.appId, "appId", { max: 100 });
    const appCategory = optionalString(input.appCategory, "appCategory", 100);
    const focusGoal = requireString(input.focusGoal, "focusGoal", { max: 500 });

    const result = await this.client.generateJson({
      schemaName: "notification_decision",
      system: `당신은 집중 세션 중 알림의 중요도를 판정합니다. ${PRIVACY_RULE}
개인정보 보호를 위해 알림 본문은 입력받지 않습니다. 긴급 여부가 불명확하면 review를 선택하세요.
출력 형식: {"action":"allow|block|review","reason":"...","confidence":0.0}.`,
      user: { appId, appCategory, focusGoal },
    });
    return validateNotification(result);
  }
}
