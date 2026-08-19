/* 서버와 이야기하는 유일한 파일.

   구조:
     화면 ──▶ Spring(:8080) ──▶ AI 서버(:3000, Node) ──▶ OpenRouter
   화면은 AI 서버를 직접 부르지 않는다. API 키가 브라우저에 내려오지 않게 하기 위해서다.

   실제 데이터가 없거나 서버 요청이 실패하면 예시 답변으로 대신하지 않는다. */

window.PlainAPI = (() => {

  /* ── 서버 주소 정하기 ─────────────────────────────
     1) Spring이 /prototype/ 아래로 화면을 서빙하면 같은 주소를 쓴다 (CORS 없음)
     2) 그 외(파일 열기, Live Server)는 localhost:8080을 부른다
     3) localStorage의 plain.apiBase로 언제든 덮어쓸 수 있다 */
  const API_BASE = (() => {
    const saved = localStorage.getItem('plain.apiBase');
    if (saved) return saved.replace(/\/$/, '');
    if (location.protocol.startsWith('http') && location.pathname.includes('/prototype/')) {
      return location.origin;
    }
    return 'http://localhost:8080';
  })();

  const KEY = {
    userId:  'plain.userId',
    draft:   'plain.goalDraft',
    plan:    'plain.plan',
    session: 'plain.sessionId'
  };

  let offline = false;

  const store = {
    get(key, fallback = null) {
      try { const raw = localStorage.getItem(key); return raw ? JSON.parse(raw) : fallback; }
      catch { return fallback; }
    },
    set(key, value) { localStorage.setItem(key, JSON.stringify(value)); },
    remove(key) { localStorage.removeItem(key); }
  };

  const userId    = () => store.get(KEY.userId);
  const setUserId = id => store.set(KEY.userId, id);

  /* ── 공통 요청 ────────────────────────────────────
     성공하면 data만 꺼내 돌려주고, 실패하면 Error를 던진다.
     서버 자체에 닿지 못하면 offline 표시를 켠다.
     keepalive는 화면을 떠나는 순간 보내는 요청(세션 종료)이 끊기지 않게 한다. */
  async function request(path, { method = 'GET', body, keepalive = false } = {}) {
    let response;
    try {
      response = await fetch(API_BASE + path, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : undefined,
        body: body ? JSON.stringify(body) : undefined,
        keepalive
      });
    } catch (error) {
      offline = true;
      showOfflineBanner();
      const failure = new Error('서버에 연결하지 못했습니다.');
      failure.offline = true;
      throw failure;
    }

    offline = false;
    let payload = null;
    try { payload = await response.json(); } catch { /* 본문이 없을 수 있다 */ }

    if (!response.ok || !payload || payload.ok !== true) {
      throw new Error(payload?.error?.message || '요청을 처리하지 못했습니다.');
    }
    return payload.data;
  }

  const get   = path => request(path);
  const post  = (path, body, options) => request(path, { method: 'POST', body, ...options });
  const patch = path => request(path, { method: 'PATCH' });

  /* ── 오프라인 배너 (DESIGN.md §7 확정 문구) ──────── */
  function showOfflineBanner() {
    if (document.querySelector('[data-offline-banner]')) return;
    const host = document.querySelector('.screen');
    if (!host) return;
    const banner = document.createElement('div');
    banner.className = 'banner';
    banner.setAttribute('data-offline-banner', 'true');
    banner.textContent = '오프라인 상태예요, 일부 기능이 제한돼요';
    host.prepend(banner);
  }

  /* ══ 계정 ═══════════════════════════════════════════ */

  const auth = {
    async login(username, password) {
      const user = await post('/api/users/login', { username, password });
      setUserId(user.id);
      return user;
    },
    async signup(username, password) {
      const user = await post('/api/users/signup', { username, password });
      setUserId(user.id);
      return user;
    },
    logout() { store.remove(KEY.userId); store.remove(KEY.plan); store.remove(KEY.session); },
    requireUser() {
      const id = userId();
      if (!id) throw new Error('로그인 후 이용해주세요.');
      return id;
    }
  };

  /* ══ 목표 입력 3단계 임시 저장 ═════════════════════ */

  const draft = {
    read()  { return store.get(KEY.draft, {}); },
    patch(values) { store.set(KEY.draft, { ...draft.read(), ...values }); },
    clear() { store.remove(KEY.draft); }
  };

  /* ══ 계획 ═══════════════════════════════════════════ */

  const plans = {
    cached() { return store.get(KEY.plan); },
    forget() { store.remove(KEY.plan); },

    /* 목표 입력 3단계 값으로 AI 계획을 만든다. Spring이 AI 서버를 대신 부르고 결과를 저장한다. */
    async generate() {
      const id = auth.requireUser();
      const d = draft.read();
      if (!d.goal || !d.currentLevel || !Number.isFinite(d.dailyHours)) {
        throw new Error('목표, 현재 수준, 하루 가능 시간을 모두 직접 입력해주세요.');
      }
      const plan = await post('/api/plans/generate', {
        userId: id,
        goal: d.goal,
        currentLevel: d.currentLevel,
        dailyHours: d.dailyHours,
        startDate: new Date().toISOString().slice(0, 10),
        constraints: d.constraints
      });
      store.set(KEY.plan, plan);
      return plan;
    },

    async current() {
      const id = auth.requireUser();
      const plan = await get(`/api/plans/current?userId=${id}`);
      if (plan) store.set(KEY.plan, plan);
      return plan;
    },

    setBlockDone(blockId, done) {
      return patch(`/api/plans/blocks/${blockId}?done=${done}`);
    },

    /* 계획에서 오늘 날짜의 일정만 꺼낸다. 오늘이 없으면 가장 이른 날을 쓴다. */
    today(plan) {
      if (!plan || !plan.days || !plan.days.length) return null;
      const today = new Date().toISOString().slice(0, 10);
      return plan.days.find(day => day.date === today) || plan.days[0];
    }
  };

  /* ══ 집중 세션 ═══════════════════════════════════════ */

  const sessions = {
    currentId() { return store.get(KEY.session); },

    async start({ goal, targetMinutes }) {
      const id = auth.requireUser();
      const session = await post('/api/focus/start', { userId: id, goal, targetMinutes });
      store.set(KEY.session, session.id);
      return session;
    },

    async end() {
      const sessionId = sessions.currentId();
      if (!sessionId) return null;
      // 화면을 떠나면서 보내는 요청이라 keepalive를 켠다.
      const session = await post('/api/focus/end', { sessionId }, { keepalive: true });
      store.remove(KEY.session);
      return session;
    },

    async today() {
      const id = auth.requireUser();
      return get(`/api/focus/today?userId=${id}`);
    }
  };

  /* ══ 통계 · AI 분석 ══════════════════════════════════ */

  const stats = {
    async summary() {
      const id = auth.requireUser();
      return get(`/api/stats/summary?userId=${id}`);
    },
    /* 14일이 안 되면 enough:false와 needMoreDays가 온다 */
    async patterns() {
      const id = auth.requireUser();
      return post(`/api/stats/patterns?userId=${id}`, {});
    }
  };

  const ai = {
    analyzeDistractions(payload) { return post('/api/ai/distractions/analyze', payload); },
    judgeNotification(payload)   { return post('/api/ai/notifications/judge', payload); }
  };

  /* ══ 화면에서 쓰는 작은 도구 ═════════════════════════ */

  /* 성공하면 결과를, 실패하면 null을 돌려준다. 예시 데이터로 대체하지 않는다. */
  async function tryOr(promiseFactory, onError) {
    try { return await promiseFactory(); }
    catch (error) { if (onError) onError(error); return null; }
  }

  const minutesToText = minutes => {
    const m = Math.max(0, Math.round(minutes || 0));
    if (m < 60) return `${m}분`;
    const hour = Math.floor(m / 60);
    const rest = m % 60;
    return rest ? `${hour}시간 ${rest}분` : `${hour}시간`;
  };

  /* "09:00"~"10:30" → 90 */
  const spanMinutes = (start, end) => {
    if (!start || !end) return 0;
    const [sh, sm] = start.split(':').map(Number);
    const [eh, em] = end.split(':').map(Number);
    const diff = (eh * 60 + em) - (sh * 60 + sm);
    return diff > 0 ? diff : 0;
  };

  return {
    base: API_BASE,
    isOffline: () => offline,
    auth, draft, plans, sessions, stats, ai,
    tryOr, minutesToText, spanMinutes, showOfflineBanner
  };
})();
