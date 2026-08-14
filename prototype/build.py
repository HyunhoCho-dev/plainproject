# -*- coding: utf-8 -*-
"""프로토타입 빌드 스크립트.

1) 모든 화면 HTML에 app.js 스크립트 태그를 넣는다 (이미 있으면 건너뜀).
2) all.html 을 '자바스크립트 없는 정적 파일'로 다시 만든다.
   - 화면 25프레임을 한 페이지에 나열
   - CSS까지 파일 안에 넣어 외부 참조를 없앤다
   - Figma 변환 도구(html.to.design 등)가 그대로 읽을 수 있다

화면을 고친 뒤 다시 실행하면 all.html 이 갱신된다.

    py prototype/build.py
"""

import re
from pathlib import Path

HERE    = Path(__file__).parent
MOBILE  = HERE / "mobile"
DESK    = HERE / "desktop"
SHARED  = HERE / "shared"

SCREENS = [
    ("goal-time.html",         "목표 시간 입력",   "온보딩 1/3"),
    ("goal-level.html",        "현재 수준 입력",   "온보딩 2/3"),
    ("goal-content.html",      "목표 내용 입력",   "온보딩 3/3"),
    ("plan-loading.html",      "AI 분석 중",       "로딩"),
    ("plan-result.html",       "계획 생성 결과",   "결과"),
    ("home.html",              "홈 (계획)",        "메인"),
    ("home-empty.html",        "홈 — 빈 상태",     "빈 상태"),
    ("plan-edit.html",         "계획 편집",        "바텀시트"),
    ("plan-regenerate.html",   "계획 재생성 확인", "다이얼로그"),
    ("timer.html",             "포커스 타이머",    "집중 모드"),
    ("notify-permission.html", "알림 권한 동의",   "최초 1회"),
    ("blocked.html",           "차단 오버레이",    "집중 모드"),
    ("blocked-apps.html",      "차단 앱 관리",     "설정"),
    ("why-recommended.html",   "왜 추천됐나요?",   "바텀시트"),
    ("stats.html",             "통계",             "메인"),
    ("stats-empty.html",       "통계 — 빈 상태",   "빈 상태"),
    ("mypage.html",            "마이페이지",       "메인"),
    ("notify-settings.html",   "알림 설정",        "설정"),
    ("login.html",             "로그인",           "인증"),
    ("signup.html",            "회원가입",         "4단계"),
    ("withdraw.html",          "회원탈퇴",         "2단계"),
]

DESKTOP = [
    "home-desktop.html", "stats-desktop.html",
    "mypage-desktop.html", "plan-result-desktop.html",
]

SCRIPT_TAG = '  <script src="../shared/app.js" defer></script>\n'


def path_of(name):
    """화면 이름 → 실제 파일 경로"""
    return (DESK if name in DESKTOP else MOBILE) / name


def inject_script():
    """화면 파일에 app.js 를 연결하고, <body>에 화면 이름을 박아둔다.

    data-page 를 쓰면 주소가 /prototype/ 처럼 파일명 없이 들어와도
    app.js 가 어느 화면인지 정확히 안다.
    """
    targets = [s[0] for s in SCREENS] + DESKTOP
    changed = []
    for name in targets:
        p = path_of(name)
        html = p.read_text(encoding="utf-8")
        before = html

        if "app.js" not in html:
            html = html.replace("</body>", SCRIPT_TAG + "</body>")

        if "data-page" not in html:
            html = re.sub(r"<body(\s[^>]*)?>",
                          lambda m: f'<body data-page="{name}"{m.group(1) or ""}>',
                          html, count=1)

        if html != before:
            p.write_text(html, encoding="utf-8")
            changed.append(name)
    return changed


def extract_phones(html):
    """<div class="phone"> ... </div> 블록을 균형 맞춰 잘라낸다."""
    out = []
    for m in re.finditer(r'<div class="phone"[^>]*>', html):
        start = m.start()
        i = m.end()
        depth = 1
        tag = re.compile(r"</?div\b", re.I)
        while depth > 0:
            t = tag.search(html, i)
            if not t:
                break
            depth += -1 if t.group(0).startswith("</") else 1
            i = t.end()
        end = html.find(">", i - 1) + 1
        out.append(html[start:end])
    return out


def build_all():
    css = "\n".join(
        (SHARED / f).read_text(encoding="utf-8") for f in ("tokens.css", "components.css")
    )

    cells, count = [], 0
    for name, label, tag in SCREENS:
        html = (MOBILE / name).read_text(encoding="utf-8")
        mode = re.search(r'<html[^>]*data-mode="([^"]+)"', html)
        mode = mode.group(1) if mode else None

        phones = extract_phones(html)
        for i, phone in enumerate(phones):
            if mode:
                # 집중 모드는 원본에서 <html>에 걸려 있다. 프레임으로 옮긴다.
                phone = phone.replace('<div class="phone"', f'<div class="phone" data-mode="{mode}"', 1)
            suffix = f" {i + 1}" if len(phones) > 1 else ""
            cells.append(
                '<div class="cell">\n'
                f'  <div class="cell__label"><b>{label}{suffix}</b><span>{tag}</span></div>\n'
                f"  {phone}\n"
                "</div>"
            )
            count += 1

    doc = f"""<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>전체 화면 {count}개 — 집중 지원 앱</title>
<style>
{css}

/* 한 장 보기 전용 */
body {{ background: var(--surface-2); }}
.sheet {{
  display: flex; flex-wrap: wrap;
  gap: var(--space-10) var(--space-8);
  padding: var(--space-10);
  align-items: flex-start; justify-content: center;
}}
.cell {{ width: 375px; }}
.cell__label {{
  display: flex; align-items: baseline; gap: var(--space-2);
  margin-bottom: var(--space-3); padding-inline: var(--space-1);
}}
.cell__label b {{ color: var(--text); font-size: 15px; font-weight: 600; }}
.cell__label span {{ color: var(--text-3); font-size: 12px; }}
.cell .phone {{ box-shadow: 0 4px 16px rgba(26,29,35,.08); }}
.sheet__head {{ width: 100%; text-align: center; padding: var(--space-8) var(--space-5) 0; }}
</style>
</head>
<body>

<div class="sheet">
  <div class="sheet__head">
    <h1 class="t-h1 mb-3">집중 지원 앱 — 모바일 전체 화면</h1>
    <p class="t-body muted">화면 {count}개 · 자바스크립트 없이 정적으로 생성됨</p>
  </div>

{chr(10).join(cells)}
</div>

</body>
</html>
"""
    (HERE / "all.html").write_text(doc, encoding="utf-8")
    return count


if __name__ == "__main__":
    changed = inject_script()
    print(f"app.js 연결: {len(changed)}개 파일" + (f" → {', '.join(changed)}" if changed else " (이미 연결됨)"))
    n = build_all()
    print(f"all.html 생성 완료: 프레임 {n}개, 정적 HTML")
