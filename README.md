# plainproject

`all` 브랜치는 프론트엔드(`prototype`), Spring 백엔드(`backend`), OpenRouter AI 서버(`ai`)를 함께 담은 통합 브랜치입니다.

## OpenRouter 키 사용 방식

API 키 값은 코드나 커밋에 넣지 않습니다. 저장소 설정의 GitHub Actions Secret `OPENROUTER_API_KEY`에 암호화하여 보관하고, `.github/workflows/integration-check.yml`이 실행될 때 AI 서버 환경변수로만 전달합니다.

`all` 브랜치에 새 커밋이 올라오면 GitHub Actions가 다음 과정을 자동으로 확인합니다.

1. AI 서버 설치와 단위 테스트
2. Spring 백엔드 빌드와 테스트
3. 두 서버 실행
4. 프론트엔드 정적 화면 확인
5. Spring 백엔드를 거쳐 OpenRouter의 실제 AI 계획 생성 호출
6. 생성된 계획이 데이터베이스에 저장되는지 검증

실제 키 문자열은 GitHub 코드 화면과 Actions 로그에 출력되지 않습니다.

## 로컬 실행

로컬에서는 `ai/.env.example`을 `ai/.env`로 복사한 뒤 `OPENROUTER_API_KEY`를 입력합니다. `ai/.env`는 Git에 커밋하지 않습니다.

```powershell
cd ai
npm ci
npm start
```

새 터미널에서 Java 17로 Spring 백엔드를 실행합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

통합 화면은 `http://localhost:8080/prototype/index.html`에서 확인합니다.

