# plainproject

## 팀원이 GitHub에서 실제 서비스 실행하기

`all` 브랜치에는 프론트엔드, Spring 백엔드, AI 서버가 함께 들어 있습니다.
관리자가 등록한 `OPENROUTER_API_KEY`는 **GitHub Codespaces Secret**으로만 전달되며 코드와 커밋에는 저장되지 않습니다.

1. GitHub 저장소에서 브랜치를 `all`로 선택합니다.
2. **Code → Codespaces → Create codespace on all**을 누릅니다.
3. 환경 구성이 끝나면 AI 서버(3000)와 통합 서비스(8080)가 자동으로 실행됩니다.
4. 자동으로 열린 **PLAIN 서비스** 탭에서 기능을 사용합니다. 열리지 않으면 Codespace의 **PORTS** 탭에서 8080을 엽니다.

Codespace 안에서 서버 로그를 확인하려면 다음 파일을 봅니다.

- AI 서버: `/tmp/plainproject/ai.log`
- 백엔드/프론트엔드: `/tmp/plainproject/backend.log`

개인 컴퓨터에 저장소만 내려받는 일반 `git clone`에는 GitHub Secret이 자동 전달되지 않습니다. 이 경우에는 각자 `ai/.env`에 키를 설정해야 합니다.

