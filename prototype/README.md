# 집중 지원 앱 — HTML 프로토타입

화면 25장. **`index.html`을 열면 전체 목록이 나옵니다.**

서버가 없어도 됩니다. 파일을 더블클릭하면 그대로 열립니다.
폴더째 복사하거나 압축해서 보내도 동작합니다.

---

## 폴더 구조

```
prototype/
├── index.html          ← 화면 목록. 여기서 시작
│
├── mobile/             ← 모바일 화면 21장
├── desktop/            ← PC 화면 4장
│
├── shared/             ← 두 버전이 함께 쓰는 것
│   ├── tokens.css      색·타이포·간격. 여기만 고치면 25장 전부 바뀜
│   ├── components.css  컴포넌트 20개 + 모바일 프레임·탭바
│   ├── desktop.css     PC 배치 (사이드바·3컬럼)
│   └── app.js          화면 연결과 UI 동작
│
├── DESIGN.md           ← 디자인 시스템. 색·타이포·컴포넌트 20개·확정 문구
├── all.html            ← 모바일 25프레임을 한 페이지에 (Figma 변환용)
├── build.py            ← all.html 재생성. 화면 고친 뒤 py build.py
└── figma-tokens.json   ← Figma용 토큰 (Tokens Studio 플러그인)
```

**모바일과 PC는 화면 파일만 다르고, 색·글자·컴포넌트는 `shared/`를 함께 씁니다.**
그래서 `tokens.css`에서 색 하나를 바꾸면 25장이 동시에 바뀝니다.

## 화면 목록

### `mobile/` — 21장

**온보딩·목표** `goal-time` `goal-level` `goal-content` `plan-loading` `plan-result`

**계획·실행** `home` `home-empty` `plan-edit` `plan-regenerate` `timer`🌙

**차단** `notify-permission` `blocked`🌙 `blocked-apps` `why-recommended`

**통계·마이** `stats` `stats-empty` `mypage` `notify-settings`

**인증** `login` `signup` `withdraw`

🌙 = 집중 모드(다크). 프레임에 `data-mode="focus"` 한 줄로 전환됩니다.

### `desktop/` — 4장

`home-desktop` `stats-desktop` `mypage-desktop` `plan-result-desktop`

**1200px 이상에서 보세요.** 앱 차단·알림 권한처럼 모바일 OS에 붙는 기능은
PC에서 성립하지 않아 만들지 않았습니다.

---

## 동작하는 것 (백엔드 없음)

- 하단 탭 3개 이동, 뒤로가기
- 온보딩 흐름 — 목표 시간 → 수준 → 내용 → AI 분석(2.6초) → 결과 → 홈
- 계획 블록 체크박스 완료/해제 + 토스트, **길게 눌러 편집 시트**
- 포커스 타이머 — 실제로 시간이 흐르고 원형 진행바가 따라 움직임, 일시정지/이어서 하기
- 차단 오버레이 — '5분만 허용' 누르면 카운트다운으로 전환
- 통계 주간/월간 전환, 스위치·칩 토글
- 마이페이지 설정 메뉴 전체 연결, 로그인·회원가입·탈퇴 흐름

저장·조회 같은 서버 기능은 없습니다. 화면 전환과 UI 반응만 구현되어 있습니다.

## 고칠 때

색·간격·폰트 크기를 화면 파일에 직접 쓰지 마세요. 전부 `var(--토큰)`으로 되어 있습니다.

바꾸고 싶으면 **같은 폴더의 [`DESIGN.md`](DESIGN.md)를 먼저 고치고** `shared/tokens.css`에 반영합니다.
그래야 이미 만든 화면과 앞으로 만들 화면이 어긋나지 않습니다.

확정 문구(오류 메시지, 빈 상태 안내 등)는 `DESIGN.md` §7에 표로 정리되어 있습니다. 임의로 바꾸지 마세요.

---

## 로컬 서버로 보려면

```
py -m http.server 8123
```

프로젝트 루트에서 실행한 뒤 `http://localhost:8123/prototype/index.html`
