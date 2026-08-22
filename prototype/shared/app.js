/* 프로토타입 상호작용 레이어.
   서버·AI 없이 화면 연결과 UI 동작만 담당한다.
   모바일 21장 + PC 4장이 이 파일 하나를 공유한다. */

(() => {
  /* 화면 판별.
     <body data-page="...">를 우선 쓴다. 주소가 /prototype/ 처럼 파일명 없이 들어오거나
     테스트 프레임 안에서 열려도 안전하다. */
  const page = document.body.dataset.page
            || location.pathname.split('/').pop()
            || 'index.html';
  const $  = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  /* 화면은 mobile/ 과 desktop/ 에 한 단계씩 나뉘어 있다.
     어느 화면에서 어디로 가든 경로는 ../<폴더>/<파일> 형태로 같다.
     파일명에 'desktop'이 들어가면 PC 화면이다. */
  const go = f => {
    if (f === 'index.html') { location.href = '../index.html'; return; }
    location.href = '../' + (f.includes('desktop') ? 'desktop/' : 'mobile/') + f;
  };
  const byText = t => $$('button, a').filter(b => b.textContent.trim() === t);
  const wireText = (t, href) => byText(t).forEach(b => b.addEventListener('click', () => go(href)));
  const onText = (t, fn) => byText(t).forEach(b => b.addEventListener('click', e => fn(e, b)));

  const isDesktop = page.includes('desktop');

  /* ── 토스트 ─────────────────────────────────────── */
  function toast(msg) {
    const host = $('.phone') || document.body;
    $$('.toast', host).forEach(t => t.remove());
    const el = document.createElement('div');
    el.className = 'toast';
    el.textContent = msg;
    if (host === document.body) el.classList.add('toast--page');
    host.appendChild(el);
    requestAnimationFrame(() => el.setAttribute('data-on', 'true'));
    setTimeout(() => { el.removeAttribute('data-on'); setTimeout(() => el.remove(), 200); }, 1800);
  }

  /* ══ 공통 동작 (모든 화면) ═══════════════════════ */

  // 모바일 하단 탭
  const TABS = ['home.html', 'stats.html', 'mypage.html'];
  $$('.tabbar__item').forEach((btn, i) => {
    btn.addEventListener('click', () => {
      if (btn.getAttribute('aria-current') !== 'page') go(TABS[i]);
    });
  });

  // PC 좌측 사이드바
  const DESK_TABS = ['home-desktop.html', 'stats-desktop.html', 'mypage-desktop.html'];
  $$('.nav__item').forEach((btn, i) => {
    btn.addEventListener('click', () => {
      if (btn.getAttribute('aria-current') !== 'page') go(DESK_TABS[i]);
    });
  });
  const userchip = $('.userchip');
  if (userchip) userchip.addEventListener('click', () => go('mypage-desktop.html'));
  const brand = $('.brand');
  if (brand) brand.addEventListener('click', () => go('home-desktop.html'));

  // 뒤로 / 닫기
  $$('.icon-btn[aria-label="뒤로"], .icon-btn[aria-label="닫기"]').forEach(b => {
    b.addEventListener('click', () => history.length > 1 ? history.back() : go('index.html'));
  });

  // 계획 블록 완료 토글
  $$('.plan-block__check').forEach(btn => {
    btn.addEventListener('click', e => {
      e.stopPropagation();
      const block = btn.closest('.plan-block');
      const done = block.getAttribute('data-state') === 'done';
      block.setAttribute('data-state', done ? '' : 'done');
      toast(done ? '완료를 해제했어요' : '완료 처리했어요');
    });
  });

  // 세그먼트 탭
  $$('.segment').forEach(seg => $$('.segment__item', seg).forEach(item => {
    item.addEventListener('click', () => {
      $$('.segment__item', seg).forEach(i => i.setAttribute('aria-selected', 'false'));
      item.setAttribute('aria-selected', 'true');
      toast(item.textContent.trim() + ' 통계로 전환했어요');
    });
  }));

  // 스위치 — 버튼 자체 또는 감싼 행을 눌러도 동작
  const flip = sw => sw.setAttribute('aria-checked',
                      sw.getAttribute('aria-checked') === 'true' ? 'false' : 'true');
  $$('.switch').forEach(sw => {
    sw.addEventListener('click', e => { e.stopPropagation(); flip(sw); });
    const row = sw.closest('.list-row, .appcard');
    if (row) row.addEventListener('click', e => {
      if (e.target.closest('.switch, .appcard__tag, .appcard__why')) return;
      flip(sw);
    });
  });

  // 칩 — 묶음 안에서 하나만
  $$('.chip-row').forEach(row => $$('.chip', row).forEach(chip => {
    chip.addEventListener('click', () => {
      $$('.chip', row).forEach(c => c.removeAttribute('aria-selected'));
      chip.setAttribute('aria-selected', 'true');
    });
  }));

  // 설명이 붙은 큰 선택 카드
  const pickCards = $$('.chip-card').filter(c => c.querySelector('span'));
  pickCards.forEach(card => card.addEventListener('click', () => {
    pickCards.forEach(c => c.removeAttribute('aria-selected'));
    card.setAttribute('aria-selected', 'true');
  }));

  // 예시 문구 카드 → 입력창에 채워 넣기
  $$('.chip-card').filter(c => !c.querySelector('span')).forEach(card => {
    card.addEventListener('click', () => {
      const area = $('.field__area');
      if (area) { area.value = card.textContent.trim(); toast('예시를 넣었어요'); }
    });
  });

  // 숫자 스테퍼
  const stepVal = $('.stepper__value .t-display');
  if (stepVal) {
    const btns = $$('.stepper__btn');
    const set = d => {
      const current = parseFloat(stepVal.textContent);
      let v = (Number.isFinite(current) ? current : 0.5) + (Number.isFinite(current) ? d : 0);
      v = Math.min(12, Math.max(0.5, v));
      stepVal.textContent = Number.isInteger(v) ? v : v.toFixed(1);
      $$('.chip').forEach(c => c.removeAttribute('aria-selected'));
    };
    btns[0]?.addEventListener('click', () => set(-0.5));
    btns[1]?.addEventListener('click', () => set(+0.5));
    $$('.chip').forEach(c => c.addEventListener('click', () => {
      const n = parseFloat(c.textContent);
      if (!isNaN(n)) stepVal.textContent = Number.isInteger(n) ? n : n.toFixed(1);
    }));
  }

  // 인증번호 자동 이동
  const otp = $$('.otp input');
  otp.forEach((input, i) => {
    input.addEventListener('input', () => {
      input.value = input.value.replace(/\D/g, '').slice(0, 1);
      if (input.value && otp[i + 1]) otp[i + 1].focus();
      const done = otp.every(o => o.value);
      const next = $('.footbar .btn--primary');
      if (next) next.disabled = !done;
    });
    input.addEventListener('keydown', e => {
      if (e.key === 'Backspace' && !input.value && otp[i - 1]) otp[i - 1].focus();
    });
  });

  // 딤 클릭 → 뒤로
  $$('.dim').forEach(d => d.addEventListener('click', () => history.back()));

  /* ── 흐름: "다음" 버튼 ──────────────────────────── */
  const NEXT = {
    'goal-time.html':         'goal-level.html',
    'goal-level.html':        'goal-content.html',
    'goal-content.html':      'plan-loading.html',
    'plan-result.html':       'home.html',
    'notify-permission.html': 'timer.html'
  };
  if (NEXT[page]) {
    $$('.footbar .btn--primary').forEach(b => b.addEventListener('click', () => go(NEXT[page])));
  }

  /* ══ 화면별 ═══════════════════════════════════════ */

  const wireLongPress = href => $$('.plan-block').forEach(b => {
    let t;
    const start = () => { t = setTimeout(() => go(href), 500); };
    const stop  = () => clearTimeout(t);
    b.addEventListener('mousedown', start);
    b.addEventListener('touchstart', start, { passive: true });
    ['mouseup', 'mouseleave', 'touchend'].forEach(ev => b.addEventListener(ev, stop));
    b.addEventListener('contextmenu', e => { e.preventDefault(); go(href); });
  });

  // 흘러가는 타이머. ring이 있으면 원형 진행바까지 갱신한다.
  function runTimer({ totalSec, startSec, timeEl, subEl, pauseBtn, ring }) {
    let elapsed = startSec, running = true;
    const C = 2 * Math.PI * 115;
    const fmt = s => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
    const render = () => {
      const p = Math.min(elapsed / totalSec, 1);
      if (ring)  ring.setAttribute('stroke-dashoffset', String(C * (1 - p)));
      if (timeEl) timeEl.textContent = fmt(elapsed);
      if (subEl)  subEl.textContent  =
        `${Math.floor(totalSec / 60)}분 중 ${Math.round(p * 100)}%`;
    };
    render();
    setInterval(() => { if (running) { elapsed++; render(); } }, 1000);
    if (pauseBtn) pauseBtn.addEventListener('click', () => {
      running = !running;
      pauseBtn.textContent = running ? '일시정지' : '이어서 하기';
      toast(running ? '집중을 이어갑니다' : '일시정지했어요');
    });
  }

  switch (page) {

    /* ── 모바일 ── */

    case 'home.html':
    case 'home-empty.html':
      $('.appbar .icon-btn:last-child')?.addEventListener('click', () => go('plan-regenerate.html'));
      $('.minibar')?.addEventListener('click', () => go('timer.html'));
      $('.goal-card')?.addEventListener('click', () => go('stats.html'));
      wireText('목표 입력하고 첫 계획 받기', 'goal-time.html');
      wireText('다시 시도', 'plan-loading.html');
      $('.plan-block[data-state="active"]')?.addEventListener('click', () => go('timer.html'));
      $$('.dateitem').forEach(d => d.addEventListener('click', () => {
        $$('.dateitem').forEach(x => x.removeAttribute('aria-selected'));
        d.setAttribute('aria-selected', 'true');
      }));
      wireLongPress('plan-edit.html');
      break;

    case 'plan-loading.html':
      setTimeout(() => go('plan-result.html'), 2600);
      break;

    case 'plan-result.html':
      // 계획 근거 화면. why-recommended.html은 차단 앱 추천 근거라서 여기로 오면 안 된다.
      wireText('근거 보기', 'plan-why.html');
      break;

    case 'plan-edit.html':
      wireText('저장', 'home.html');
      wireText('삭제', 'home.html');
      break;

    case 'plan-regenerate.html':
      wireText('취소', 'home.html');
      wireText('다시 만들기', 'plan-loading.html');
      break;

    case 'timer.html':
      wireText('종료', 'home.html');
      runTimer({
        // 0초부터 센다. 총 시간은 live.js가 지금 일정의 길이를 body에 적어둔다.
        totalSec: Number(document.body.dataset.totalSec) || 90 * 60,
        startSec: 0,
        timeEl: $('.ring__center .t-display'),
        subEl:  $('.ring__center .t-caption'),
        pauseBtn: byText('일시정지')[0],
        ring: $('.ring__bar')
      });
      break;

    case 'blocked.html': {
      wireText('계속 집중하기', 'timer.html');
      wireText('차단 목록에서 제외', 'blocked-apps.html');
      const allow = byText('5분만 허용')[0];
      allow?.addEventListener('click', () => {
        let left = 300;
        allow.disabled = true;
        const tick = () => {
          allow.textContent = `${Math.floor(left / 60)}:${String(left % 60).padStart(2, '0')} 후 다시 차단`;
          if (left-- <= 0) location.reload();
        };
        tick(); setInterval(tick, 1000);
        toast('5분간 허용합니다');
      });
      break;
    }

    case 'notify-permission.html':
      wireText('나중에 할게요', 'timer.html');
      break;

    case 'blocked-apps.html':
      $$('.appcard__why').forEach(b => b.addEventListener('click', e => {
        e.stopPropagation(); go('why-recommended.html');
      }));
      byText('AI가 무엇을 기준으로 판단하나요?')[0]
        ?.closest('button')?.addEventListener('click', () => go('why-recommended.html'));
      $('.btn--quiet.btn--block')?.addEventListener('click', () => go('why-recommended.html'));
      $$('.appcard__tag').forEach(tag => tag.addEventListener('click', e => {
        e.stopPropagation();
        tag.closest('.appcard').setAttribute('data-changed', 'true');
        toast('이 선택을 기억할게요');
      }));
      wireText('저장하기', 'mypage.html');
      break;

    case 'why-recommended.html':
      wireText('추천 해제', 'blocked-apps.html');
      wireText('차단 유지', 'blocked-apps.html');
      break;

    case 'stats.html':
      wireText('다음 계획에 반영하기', 'home.html');
      break;

    case 'stats-empty.html':
      wireText('타이머 시작하기', 'timer.html');
      break;

    case 'mypage.html': {
      const MENU = {
        '목표 재설정': 'goal-time.html', '알림 설정': 'notify-settings.html',
        '차단 앱 관리': 'blocked-apps.html', '계정 정보 수정': 'signup.html',
        '내 데이터 보기': 'stats.html', '로그아웃': 'login.html', '회원탈퇴': 'withdraw.html'
      };
      $$('.list-row, .btn--text').forEach(row => {
        const label = (row.querySelector('.list-row__label')?.childNodes[0]?.textContent ?? row.textContent).trim();
        if (MENU[label]) row.addEventListener('click', () => go(MENU[label]));
      });
      $('.goal-card')?.addEventListener('click', () => go('home.html'));
      $('.card[role="button"]')?.addEventListener('click', () => go('stats.html'));
      break;
    }

    case 'login.html':
      wireText('로그인', 'home.html');
      $$('.social').forEach(b => b.addEventListener('click', () => go('goal-time.html')));
      wireText('회원가입', 'signup.html');
      break;

    case 'signup.html': {
      const steps = $$('.signup-step');
      let currentStep = 0;
      const showStep = index => {
        currentStep = Math.max(0, Math.min(index, steps.length - 1));
        steps.forEach((step, stepIndex) => {
          const active = stepIndex === currentStep;
          step.hidden = !active;
          step.classList.toggle('is-active', active);
        });
      };
      wireText('로그인', 'login.html');
      onText('전화번호로 가입하기', () => showStep(1));
      $$('[data-signup-next]').forEach(button =>
        button.addEventListener('click', () => showStep(currentStep + 1)));
      steps.forEach(step => step.querySelector('[aria-label="뒤로"]')
        ?.addEventListener('click', () => showStep(currentStep - 1)));
      onText('중복 확인', () => toast('아이디를 입력한 뒤 가입을 진행해주세요'));
      onText('인증번호 재전송', () => toast('전화번호 인증 서버 연결이 필요합니다'));
      // 약관 체크 토글
      $$('.list-row').forEach(row => {
        const mark = row.firstElementChild;
        if (mark && mark.textContent.trim() === '✓') {
          row.addEventListener('click', () => {
            const on = !mark.classList.contains('muted3');
            mark.classList.toggle('muted3', on);
            mark.style.color = on ? '' : 'var(--accent)';
            row.querySelector('.list-row__label')?.classList.toggle('muted3', on);
          });
        }
      });
      showStep(0);
      break;
    }

    case 'withdraw.html':
      wireText('계속 사용할게요', 'mypage.html');
      wireText('취소', 'mypage.html');
      wireText('탈퇴', 'login.html');
      onText('탈퇴하기', () => toast('아래 최종 확인 화면을 봐주세요'));
      break;

    /* ── PC ── */

    case 'home-desktop.html':
      wireText('이번 주 계획 다시 만들기', 'plan-regenerate.html');
      wireText('종료', 'home-desktop.html');
      $$('.dateitem').forEach(d => d.addEventListener('click', () => {
        $$('.dateitem').forEach(x => x.removeAttribute('aria-selected'));
        d.setAttribute('aria-selected', 'true');
      }));
      runTimer({
        // 0초부터 센다. 총 시간은 live.js가 지금 일정의 길이를 body에 적어둔다.
        totalSec: Number(document.body.dataset.totalSec) || 90 * 60,
        startSec: 0,
        timeEl: $('.session__time'), subEl: null,
        pauseBtn: byText('일시정지')[0], ring: null
      });
      break;

    case 'stats-desktop.html':
      wireText('다음 계획에 반영하기', 'home-desktop.html');
      break;

    case 'mypage-desktop.html': {
      // PC 버전이 있는 화면만 이동한다. 나머지는 모바일 전용이라고 알린다.
      const DESK_MENU = { '내 데이터 보기': 'stats-desktop.html' };
      const MOBILE_ONLY = ['목표 재설정', '알림 설정', '차단 앱 관리', '계정 정보 수정'];
      $$('.list-row, .btn--text').forEach(row => {
        const label = (row.querySelector('.list-row__label')?.childNodes[0]?.textContent ?? row.textContent).trim();
        if (DESK_MENU[label])            row.addEventListener('click', () => go(DESK_MENU[label]));
        else if (MOBILE_ONLY.includes(label)) row.addEventListener('click', () => toast(`'${label}' — 모바일 전용 화면이에요`));
        else if (label === '로그아웃')   row.addEventListener('click', () => go('login.html'));
        else if (label === '회원탈퇴')   row.addEventListener('click', () => go('withdraw.html'));
      });
      wireText('프로필 수정', 'signup.html');
      wireText('통계 보기', 'stats-desktop.html');
      $('.goal-card')?.addEventListener('click', () => go('home-desktop.html'));
      break;
    }

    case 'plan-result-desktop.html':
      wireText('계획 확인하러 가기', 'home-desktop.html');
      wireText('조건 바꾸기', 'goal-time.html');
      wireText('근거 보기', 'plan-why-desktop.html');
      break;
  }

  /* ══ 심사 제출용 데모 모드 ═════════════════════════
     실제 API를 호출하지 않고 각 화면이 설명 가능한 상태로 보이게 한다.
     하단 버튼은 심사위원이 정해진 순서대로 모든 화면을 넘겨보는 용도다. */
  if (window.PLAIN_DEMO_MODE) {
    document.body.dataset.demo = 'true';

    const DEMO_FLOW = [
      ['mobile', 'login.html', '로그인'],
      ['mobile', 'signup.html', '회원가입'],
      ['mobile', 'goal-time.html', '목표 시간 입력'],
      ['mobile', 'goal-level.html', '현재 수준 입력'],
      ['mobile', 'goal-content.html', '목표 내용 입력'],
      ['mobile', 'plan-loading.html', 'AI 계획 생성 중'],
      ['mobile', 'plan-result.html', 'AI 계획 생성 결과'],
      ['mobile', 'plan-why.html', 'AI 계획 추천 근거'],
      ['mobile', 'notify-permission.html', '알림 권한 안내'],
      ['mobile', 'home.html', '오늘의 계획'],
      ['mobile', 'home-empty.html', '계획 빈 상태'],
      ['mobile', 'plan-edit.html', '계획 편집'],
      ['mobile', 'plan-regenerate.html', '계획 재생성'],
      ['mobile', 'timer.html', '집중 타이머'],
      ['mobile', 'blocked.html', '방해요소 차단'],
      ['mobile', 'blocked-apps.html', '차단 앱 관리'],
      ['mobile', 'why-recommended.html', 'AI 차단 추천 근거'],
      ['mobile', 'notify-settings.html', '알림 설정'],
      ['mobile', 'stats.html', '학습 통계와 AI 인사이트'],
      ['mobile', 'stats-empty.html', '통계 빈 상태'],
      ['mobile', 'mypage.html', '마이페이지'],
      ['mobile', 'withdraw.html', '회원탈퇴'],
      ['desktop', 'home-desktop.html', 'PC 오늘의 계획'],
      ['desktop', 'plan-result-desktop.html', 'PC AI 계획 결과'],
      ['desktop', 'plan-why-desktop.html', 'PC AI 추천 근거'],
      ['desktop', 'stats-desktop.html', 'PC 통계'],
      ['desktop', 'mypage-desktop.html', 'PC 마이페이지']
    ];

    const demoText = (selector, value, index = 0) => {
      const element = $$(selector)[index];
      if (element) element.textContent = value;
    };
    const demoValue = (selector, value, index = 0) => {
      const element = $$(selector)[index];
      if (element) element.value = value;
    };

    // 모든 화면에서 동일하게 쓰는 대표 사용자 데이터.
    $$('.userchip__name').forEach(element => { element.textContent = '김민준'; });

    if (page === 'login.html') {
      demoValue('.field__input', 'plain_demo', 0);
      demoValue('.field__input', 'plain2026!', 1);
    }

    if (page === 'signup.html') {
      demoValue('input[autocomplete="username"]', 'plain_demo');
      demoValue('input[autocomplete="new-password"]', 'plain2026!');
      demoValue('input[autocomplete="nickname"]', '민준');
    }

    if (page === 'goal-time.html') {
      demoText('.stepper__value .t-display', '2.5');
      const selected = byText('2.5시간')[0];
      selected?.setAttribute('aria-selected', 'true');
    }

    if (page === 'goal-level.html') {
      const selected = $$('.chip-card').find(card => card.querySelector('b')?.textContent === '중급');
      selected?.setAttribute('aria-selected', 'true');
    }

    if (page === 'goal-content.html') {
      demoValue('.field__area', '2026년 9월 정보처리기사 실기 합격을 목표로 핵심 이론과 기출문제를 공부하고 싶어요.');
    }

    if (page === 'plan-result.html' || page === 'plan-result-desktop.html') {
      demoText('.badge--ai', 'AI 분석 완료');
      demoText('h1', '맞춤 학습 계획이 완성됐어요');
      demoText('.t-body.muted', '입력한 목표와 학습 가능 시간을 바탕으로 만든 계획입니다.');
      demoText('.card .t-h2', '6주', 0);
      demoText('.card .muted3', '시험일까지 복습 주간 1주를 포함했어요.', 0);
      demoText('.card .t-h2', '주 15시간', 1);
      demoText('.card .muted3', '평일 2시간 30분, 주말 보충 학습 기준입니다.', 1);
      demoText('.card .t-h2', '데이터베이스 · 실무 알고리즘', 2);
      demoText('.card .muted3', '최근 기출 비중과 현재 수준을 함께 반영했어요.', 2);
      demoText('.ai-card__body', '처음 2주는 취약 개념을 짧게 학습한 뒤 바로 기출문제로 확인하세요. 오전 집중도가 높아 어려운 과목을 오전에 배치했습니다.');
    }

    if (page === 'home.html' || page === 'home-desktop.html' || page === 'mypage.html' || page === 'mypage-desktop.html') {
      demoText('.goal-card__name', '정보처리기사 실기 합격');
      demoText('.goal-card__dday', 'D-42');
      demoText('.goal-card__rate strong', '68');
      const gauge = $('.goal-card .gauge > i');
      if (gauge) gauge.style.width = '68%';
      demoText('.goal-card .t-caption[style*="margin-left"]', '102분 / 150분');
    }

    if (page === 'home.html') demoText('.minibar__time', '24:13');

    if (page === 'stats.html' || page === 'stats-desktop.html') {
      demoText('.stat-tile__value', '9시간 40분', 0);
      demoText('.stat-tile__value', '82%', 1);
      demoText('.stat-tile__value', '37회', 2);
      demoText('.stat-tile__value', '41분', 3);
      demoText('.ai-card__body', '오전 10시부터 12시까지 집중 유지 시간이 평균보다 24% 길었어요. 다음 주에는 데이터베이스와 알고리즘 학습을 이 시간대에 우선 배치해보세요.');
    }

    if (page === 'mypage.html' || page === 'mypage-desktop.html') {
      const headings = $$('.t-h2');
      if (headings[0]) headings[0].textContent = '김민준';
      demoText('.num[style*="--warn"]', '🔥 12');
      demoText('.card .t-h2', '9시간 40분');
    }

    if (page === 'why-recommended.html') {
      demoText('[data-why-note]', '최근 2주간 집중 세션 18회와 방해 알림 37건을 분석했어요.');
      demoText('.card .t-body', '집중 세션 중 인스타그램을 연 직후 평균 복귀 시간이 8분 12초로 가장 길었습니다. 시험까지 42일 남아 우선 차단을 추천합니다.');
    }

    const ribbon = document.createElement('div');
    ribbon.className = 'demo-ribbon';
    ribbon.textContent = '심사용 프로토타입 · 데모 데이터';
    document.body.appendChild(ribbon);

    const currentIndex = Math.max(0, DEMO_FLOW.findIndex(([, file]) => file === page));
    const current = DEMO_FLOW[currentIndex];
    const navigate = index => {
      const [folder, file] = DEMO_FLOW[index];
      location.href = `../${folder}/${file}`;
    };
    const nav = document.createElement('nav');
    nav.className = 'demo-nav';
    nav.setAttribute('aria-label', '프로토타입 화면 이동');
    nav.innerHTML = `
      <button class="demo-nav__button" data-demo-prev ${currentIndex === 0 ? 'disabled' : ''}>← 이전</button>
      <div class="demo-nav__status">
        <b>${current[2]}</b>
        <span>${currentIndex + 1} / ${DEMO_FLOW.length}</span>
      </div>
      <button class="demo-nav__button" data-demo-next ${currentIndex === DEMO_FLOW.length - 1 ? 'disabled' : ''}>다음 →</button>`;
    nav.querySelector('[data-demo-prev]')?.addEventListener('click', () => navigate(currentIndex - 1));
    nav.querySelector('[data-demo-next]')?.addEventListener('click', () => navigate(currentIndex + 1));
    document.body.appendChild(nav);
  }
})();
