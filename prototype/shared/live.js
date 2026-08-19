/* 서버에서 받은 데이터를 화면에 그리는 층.

   app.js  — 화면 이동과 UI 동작. 서버를 모른다. 이 프로젝트에서 수정하지 않았다
   api.js  — 서버 통신. 화면을 모른다
   live.js — 둘을 잇는다. 여기서만 두 쪽을 함께 안다

   app.js를 고치지 않기로 해서, 이 파일은 스스로 화면에 붙는다.
   불러오는 순서가 api.js → live.js → app.js 라서 여기서 등록한 처리기가 먼저 실행된다.
   app.js의 동작을 대신해야 하는 자리(로그인 실패, 세션 종료)는 캡처 단계에서 가로챈다.

   원칙: 서버가 없거나 데이터가 없으면 예시 답변을 보여주지 않고
   빈 상태 또는 실제 오류를 표시한다. */

(() => {

  const $  = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const text = (el, value) => { if (el && value != null && value !== '') el.textContent = value; };
  const page = document.body.dataset.page || location.pathname.split('/').pop();

  /* 같은 폴더 구조를 쓰는 app.js의 이동 규칙과 동일하게 맞춘다. */
  const go = file => {
    location.href = '../' + (file.includes('desktop') ? 'desktop/' : 'mobile/') + file;
  };

  const buttonsWith = label =>
    $$('button, a').filter(b => b.textContent.trim() === label);

  /** 캡처 단계에서 가로채 app.js의 처리기까지 막는다. 꼭 필요한 자리에만 쓴다. */
  const takeOver = (label, handler) => {
    document.addEventListener('click', event => {
      const target = event.target.closest('button, a');
      if (!target || target.textContent.trim() !== label) return;
      event.preventDefault();
      event.stopImmediatePropagation();
      handler(target);
    }, true);
  };

  /** 가로채지 않고 먼저 한 가지 일만 하고 app.js에 넘긴다. */
  const before = (selector, handler) => {
    $$(selector).forEach(el => el.addEventListener('click', handler, true));
  };

  function toast(message) {
    const host = $('.phone') || document.body;
    const el = document.createElement('div');
    el.className = 'toast';
    el.textContent = message;
    if (host === document.body) el.classList.add('toast--page');
    host.appendChild(el);
    requestAnimationFrame(() => el.setAttribute('data-on', 'true'));
    setTimeout(() => { el.removeAttribute('data-on'); setTimeout(() => el.remove(), 200); }, 1800);
  }

  /* ── 계획 블록 하나를 HTML로 ─────────────────────── */
  function blockHtml(block, state) {
    const minutes = PlainAPI.spanMinutes(block.start, block.end);
    const meta = minutes ? `AI 예상 소요시간 ${PlainAPI.minutesToText(minutes)}` : '';
    return `
      <li class="plan-block" data-block-id="${block.id}"${state ? ` data-state="${state}"` : ''}>
        <button class="plan-block__check" aria-label="${state === 'done' ? '완료 해제' : '완료 표시'}">
          ${state === 'done' ? `<svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
            <path d="M2 6.2L4.6 8.8L10 3.4" stroke="currentColor" stroke-width="2"
                  stroke-linecap="round" stroke-linejoin="round"/></svg>` : ''}
        </button>
        <div>
          <div class="plan-block__head">
            <span class="t-caption plan-block__time num">${block.start} – ${block.end}</span>
            <span class="badge badge--ai t-micro">AI 추천</span>
          </div>
          ${state === 'active' ? '<span class="t-micro plan-block__now">진행중</span>' : ''}
          <p class="t-h3 plan-block__title"></p>
          ${meta ? `<p class="t-caption plan-block__meta">${meta}</p>` : ''}
        </div>
      </li>`;
  }

  /** 오늘 일정 중 지금 시각이 걸쳐 있는 것. 없으면 첫 미완료 일정. */
  function pickActive(blocks) {
    const now = new Date();
    const nowMinutes = now.getHours() * 60 + now.getMinutes();
    const inRange = blocks.findIndex(block => {
      if (!block.start || !block.end) return false;
      const [sh, sm] = block.start.split(':').map(Number);
      const [eh, em] = block.end.split(':').map(Number);
      return nowMinutes >= sh * 60 + sm && nowMinutes < eh * 60 + em;
    });
    return inRange >= 0 ? inRange : blocks.findIndex(block => !block.done);
  }

  /* ══ 목표 입력 3단계 ═══════════════════════════════
     app.js가 다음 화면으로 넘기기 직전에 입력값만 저장한다. 이동은 app.js가 그대로 한다. */

  function goalStep() {
    if (page === 'goal-time.html') {
      takeOver('다음', () => {
        const hours = parseFloat($('.stepper__value .t-display')?.textContent);
        if (!Number.isFinite(hours)) return toast('하루 가능 시간을 선택해주세요.');
        PlainAPI.draft.patch({ dailyHours: hours });
        go('goal-level.html');
      });
    }
    if (page === 'goal-level.html') {
      takeOver('다음', () => {
        const picked = $('.chip-card[aria-selected="true"]');
        if (!picked) return toast('현재 수준을 선택해주세요.');
        PlainAPI.draft.patch({ currentLevel: picked.textContent.trim().replace(/\s+/g, ' — ') });
        go('goal-content.html');
      });
    }
    if (page === 'goal-content.html') {
      takeOver('계획 만들기', () => {
        const goal = $('.field__area')?.value.trim();
        if (!goal) return toast('목표를 직접 입력해주세요.');
        PlainAPI.draft.patch({ goal });
        PlainAPI.plans.forget();
        go('plan-loading.html');
      });
    }
  }

  /* ══ 계획 생성 결과 ═══════════════════════════════
     [메모] 원래는 로딩 화면(plan-loading)에서 만드는 게 자연스럽다.
     app.js가 그 화면에서 2.6초 뒤 결과 화면으로 넘기도록 되어 있고 그 파일을 고치지 않기로 해서,
     계획 생성을 결과 화면에서 시작한다. app.js 수정이 허용되면 로딩 화면으로 옮기면 된다. */

  async function planResult() {
    let plan = PlainAPI.plans.cached();

    if (!plan) {
      // 서버 응답 전에는 기존 HTML의 샘플 분석값을 전부 지운다.
      text($('.badge--ai'), 'AI 분석 중');
      text($('h1.t-h1'), '계획을 만들고 있어요');
      text($('.screen .pad .t-body.muted') || $('.onboard__inner > .t-body.muted'),
           '입력한 내용으로 실시간 AI 계획을 만들고 있어요.');
      $$('.card .t-h2').forEach(el => { el.textContent = '계산 중…'; });
      $$('.card .t-caption.muted3').forEach(el => { el.textContent = ''; });
      const body = $('.ai-card__body');
      text(body, '실시간 AI 결과를 기다리고 있어요. 잠시만 기다려주세요.');
      try {
        plan = await PlainAPI.plans.generate();
      } catch (error) {
        return planError(error.message);
      }
    }
    render(plan);

    function render(plan) {
      text($('.badge--ai'), 'AI 분석 완료');
      text($('h1.t-h1'), '계획이 준비됐어요');
      text($('.screen .pad .t-body.muted') || $('.onboard__inner > .t-body.muted'),
           `${plan.goal} · 하루 ${plan.dailyHours}시간 기준`);

      const cards = $$('.card');
      if (plan.estimatedWeeks && cards[0]) text($('.t-h2', cards[0]), `약 ${plan.estimatedWeeks}주`);

      // 주간 추천 학습시간 — 계획에 들어 있는 실제 시간을 주 단위로 환산한다
      const allBlocks = plan.days.flatMap(day => day.blocks);
      const totalMinutes = allBlocks.reduce((sum, b) => sum + PlainAPI.spanMinutes(b.start, b.end), 0);
      const weeks = Math.max(1, Math.ceil(plan.days.length / 7));
      if (cards[1]) text($('.t-h2', cards[1]), `주 ${Math.round(totalMinutes / weeks / 60)}시간`);

      // 첫 주 집중 영역 — 첫날 첫 일정의 제목
      const first = plan.days[0] && plan.days[0].blocks[0];
      if (first && cards[2]) text($('.t-h2', cards[2]), first.title);

      const comment = [plan.summary, ...(plan.advice || [])].filter(Boolean).join(' ');
      text($('.ai-card__body'), comment);
    }
  }

  /* 계획 생성 실패 화면 (DESIGN.md §7 확정 문구) */
  function planError(reason) {
    const screen = $('.screen') || $('.onboard__inner');
    if (!screen) return;
    screen.innerHTML = `
      <header class="appbar"></header>
      <section class="empty-state" style="padding-block:var(--space-10)">
        <p class="t-body">계획을 만드는 데 문제가 생겼어요</p>
        <button class="btn btn--primary" data-retry>다시 시도</button>
      </section>
      <section class="pad mt-6"><p class="t-caption muted3" data-reason></p></section>`;
    text($('[data-reason]', screen), reason);
    $('[data-retry]', screen).addEventListener('click', () => location.reload());
  }

  /* ══ 홈 ═══════════════════════════════════════════ */

  async function home() {
    try { PlainAPI.auth.requireUser(); } catch { return go('login.html'); }
    const plan = await PlainAPI.tryOr(() => PlainAPI.plans.current());
    if (!plan) return go('home-empty.html');

    const day = PlainAPI.plans.today(plan);
    if (!day || !day.blocks.length) return;

    text($('.goal-card__name'), plan.goal);
    const totalMinutes = day.blocks.reduce((sum, b) => sum + PlainAPI.spanMinutes(b.start, b.end), 0);
    const doneMinutes  = day.blocks.filter(b => b.done)
                                   .reduce((sum, b) => sum + PlainAPI.spanMinutes(b.start, b.end), 0);
    const rate = totalMinutes ? Math.round(doneMinutes / totalMinutes * 100) : 0;
    text($('.goal-card__rate .num'), String(rate));
    text($('.goal-card__rate .t-caption'),
         `${PlainAPI.minutesToText(doneMinutes)} / ${PlainAPI.minutesToText(totalMinutes)}`);
    const gauge = $('.gauge i');
    if (gauge) gauge.style.width = rate + '%';
    if (plan.estimatedWeeks) {
      const left = plan.estimatedWeeks * 7;
      text($('.goal-card__dday'), `D-${left}`);
    }

    const list = $('.plan-list');
    if (!list) return;
    const activeIndex = pickActive(day.blocks);
    list.innerHTML = day.blocks
      .map((block, index) => blockHtml(block, block.done ? 'done' : (index === activeIndex ? 'active' : '')))
      .join('');

    // 제목은 AI가 만든 문자열이라 HTML로 해석되지 않게 textContent로 넣는다.
    $$('.plan-block', list).forEach((li, index) => {
      text($('.plan-block__title', li), day.blocks[index].title);
    });

    // 목록을 새로 그렸으므로 app.js가 걸어둔 처리기가 사라졌다. 여기서 다시 붙인다.
    $$('.plan-block__check', list).forEach(button => {
      button.addEventListener('click', event => {
        event.stopPropagation();
        const block = button.closest('.plan-block');
        const done = block.getAttribute('data-state') === 'done';
        block.setAttribute('data-state', done ? '' : 'done');
        toast(done ? '완료를 해제했어요' : '완료 처리했어요');
        PlainAPI.tryOr(() => PlainAPI.plans.setBlockDone(block.dataset.blockId, !done));
      });
    });
    $('.plan-block[data-state="active"]', list)
      ?.addEventListener('click', () => go('timer.html'));
  }

  /* ══ 타이머 ═══════════════════════════════════════
     [메모] 화면의 시계 자체는 app.js가 굴린다(00:00에서 시작).
     app.js를 고치지 않기로 해서 표시는 그대로 두고, 세션 기록만 서버에 남긴다.
     실제 집중 시간은 서버가 시작·종료 시각으로 계산하므로 기록은 정확하다. */

  async function timer() {
    const plan = PlainAPI.plans.cached();
    if (!plan) return go('home-empty.html');
    const day = plan && PlainAPI.plans.today(plan);
    const blocks = (day && day.blocks) || [];
    const active = pickActive(blocks);
    const block = active >= 0 ? blocks[active] : null;
    if (!block) return go('home-empty.html');
    const targetMinutes = PlainAPI.spanMinutes(block.start, block.end);

    const session = await PlainAPI.tryOr(() => PlainAPI.sessions.start({
      goal: block.title,
      targetMinutes
    }));

    if (session && block) {
      text($('.screen .pad .t-h3'), block.title);
      text($('.screen .pad .t-caption'), `AI 예상 소요시간 ${PlainAPI.minutesToText(targetMinutes)}`);
    }

    // 종료 — 서버에 기록한 뒤 홈으로. app.js도 홈으로 보내므로 여기서 가로챈다.
    if (session) {
      takeOver('종료', async () => {
        const ended = await PlainAPI.tryOr(() => PlainAPI.sessions.end());
        if (ended) toast(`${PlainAPI.minutesToText(ended.actualMinutes)} 기록했어요`);
        setTimeout(() => go('home.html'), ended ? 600 : 0);
      });
    }

    // 오늘 완료한 세션 목록
    const todaySessions = await PlainAPI.tryOr(() => PlainAPI.sessions.today());
    const list = $('.screen .list');
    if (!todaySessions || !todaySessions.length || !list) return;
    list.innerHTML = todaySessions.map(() => `
      <div class="list-row">
        <span class="t-body-sm list-row__label"></span>
        <span class="t-caption list-row__value num"></span>
      </div>`).join('');
    $$('.list-row', list).forEach((row, index) => {
      text($('.list-row__label', row), todaySessions[index].goal || '집중 세션');
      text($('.list-row__value', row), PlainAPI.minutesToText(todaySessions[index].actualMinutes));
    });
  }

  /* ══ 통계 ═════════════════════════════════════════ */

  async function stats() {
    try { PlainAPI.auth.requireUser(); } catch { return go('login.html'); }
    const summary = await PlainAPI.tryOr(() => PlainAPI.stats.summary());
    if (!summary || !summary.dailyStats?.some(day => day.studyMinutes > 0)) return go('stats-empty.html');

    const tiles = $$('.stat-tile__value');
    text(tiles[0], PlainAPI.minutesToText(summary.weekMinutes));
    text(tiles[1], Math.round(summary.averageCompletionRate * 100) + '%');
    text(tiles[2], summary.blockedCount + '회');
    const studied = summary.dailyStats.filter(day => day.studyMinutes > 0);
    const average = studied.length
      ? Math.round(studied.reduce((sum, day) => sum + day.studyMinutes, 0) / studied.length)
      : 0;
    text(tiles[3], PlainAPI.minutesToText(average));

    // 요일별 막대 — 최근 7일
    const bars = $$('.bars__col');
    if (bars.length === 7) {
      const recent = summary.dailyStats.slice(-7);
      const max = Math.max(1, ...recent.map(day => day.studyMinutes));
      const today = new Date().toISOString().slice(0, 10);
      const labels = ['일', '월', '화', '수', '목', '금', '토'];
      bars.forEach((col, index) => {
        const day = recent[index];
        const bar = $('.bars__bar', col);
        if (bar) bar.style.height = day ? Math.round(day.studyMinutes / max * 100) + '%' : '0%';
        if (!day) return;
        text($('.bars__label', col), labels[new Date(day.date).getDay()]);
        col.toggleAttribute('data-today', day.date === today);
      });
    }

    text($('.pad.t-caption.muted3'), '마지막 업데이트: ' + new Date().toLocaleString('ko-KR', {
      dateStyle: 'short', timeStyle: 'short'
    }));

    // AI 인사이트 — 14일이 안 되면 부족한 일수를 그대로 보여준다 (확정 문구)
    const insightBody = $('.ai-card__body');
    if (!insightBody) return;
    const patterns = await PlainAPI.tryOr(() => PlainAPI.stats.patterns());
    if (!patterns) return;
    if (!patterns.enough) {
      text(insightBody, `아직 패턴을 분석하기엔 데이터가 부족해요 (앞으로 ${patterns.needMoreDays}일 더 필요)`);
      return;
    }
    const insight = patterns.insight || {};
    const actions = Array.isArray(insight.actions) ? insight.actions : [];
    text(insightBody, [insight.insight, actions[0]].filter(Boolean).join(' '));
  }

  /* ══ 로그인 · 회원가입 ═════════════════════════════ */

  function login() {
    // 실패하면 화면에 남아야 하므로 app.js의 이동을 가로챈다.
    takeOver('로그인', async () => {
      const inputs = $$('.field__input');
      try {
        await PlainAPI.auth.login(inputs[0]?.value.trim(), inputs[1]?.value);
        go('home.html');
      } catch (error) {
        toast(error.message);
      }
    });
    $$('.social').forEach(button => {
      button.disabled = true;
      button.title = '소셜 로그인은 아직 연결되지 않았습니다.';
    });
  }

  function signup() {
    const fieldByLabel = label => $$('.field')
      .find(f => f.querySelector('.field__label')?.textContent.trim() === label)
      ?.querySelector('.field__input');

    takeOver('가입 완료', async () => {
      const username = fieldByLabel('아이디')?.value.trim();
      const password = fieldByLabel('비밀번호')?.value;
      try {
        await PlainAPI.auth.signup(username, password);
        go('goal-time.html');
      } catch (error) {
        toast(error.message);
      }
    });
    $$('.social').forEach(button => {
      button.disabled = true;
      button.title = '소셜 회원가입은 아직 연결되지 않았습니다.';
    });
  }

  /* ══ 화면별 실행 ═══════════════════════════════════ */

  const SCREENS = {
    'goal-time.html':    goalStep,
    'goal-level.html':   goalStep,
    'goal-content.html': goalStep,
    'plan-result.html':  planResult,
    'plan-result-desktop.html': planResult,
    'home.html':         home,
    'home-desktop.html': home,
    'timer.html':        timer,
    'stats.html':        stats,
    'stats-desktop.html': stats,
    'login.html':        login,
    'signup.html':       signup
  };

  const run = SCREENS[page];
  if (run) run();

  // 마이페이지에서 로그아웃하면 저장해 둔 로그인 정보도 지운다.
  if (page === 'mypage.html' || page === 'mypage-desktop.html') {
    before('.list-row, .btn--text', event => {
      const label = event.currentTarget.textContent.trim();
      if (label.startsWith('로그아웃')) PlainAPI.auth.logout();
    });
  }
})();
