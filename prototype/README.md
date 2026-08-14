# 집중 지원 앱 — HTML 프로토타입

화면 25장. **`index.html`을 열면 전체 목록이 나옵니다.**

서버가 없어도 됩니다. 파일을 더블클릭하면 그대로 열립니다.
폴더째 복사하거나 압축해서 보내도 동작합니다.

---

## 파일 구조

| 파일 | 역할 |
|---|---|
| `index.html` | 화면 목록. 여기서 시작 |
| `tokens.css` | 색·타이포·간격·radius. **여기만 고치면 25장 전부 바뀝니다** |
| `components.css` | 컴포넌트 20개 + 모바일 프레임·탭바 |
| `desktop.css` | PC 배치 (사이드바·3컬럼) |
| `figma-tokens.json` | Figma용 토큰. Tokens Studio 플러그인에서 Import |

## 화면 목록

**온보딩·목표** `goal-time` `goal-level` `goal-content` `plan-loading` `plan-result`

**계획·실행** `home` `home-empty` `plan-edit` `plan-regenerate` `timer`🌙

**차단** `notify-permission` `blocked`🌙 `blocked-apps` `why-recommended`

**통계·마이** `stats` `stats-empty` `mypage` `notify-settings`

**인증** `login` `signup` `withdraw`

**PC 웹** `home-desktop` `stats-desktop` `mypage-desktop` `plan-result-desktop`

🌙 = 집중 모드(다크). `<html data-mode="focus">` 한 줄로 전환됩니다.
PC 화면은 **1200px 이상**에서 보세요.

---

## 고칠 때

색·간격·폰트 크기를 화면 파일에 직접 쓰지 마세요. 전부 `var(--토큰)`으로 되어 있습니다.

바꾸고 싶으면 **프로젝트 루트의 `DESIGN.md`를 먼저 고치고** `tokens.css`에 반영합니다.
그래야 이미 만든 화면과 앞으로 만들 화면이 어긋나지 않습니다.

확정 문구(오류 메시지, 빈 상태 안내 등)는 `DESIGN.md` §7에 표로 정리되어 있습니다. 임의로 바꾸지 마세요.

---

## 로컬 서버로 보려면

```
py -m http.server 8123
```

프로젝트 루트에서 실행한 뒤 `http://localhost:8123/prototype/index.html`
