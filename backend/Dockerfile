# ==============================================================================
# 1단계: 빌드 환경 (Build Stage)
# ==============================================================================
# Java 17을 지원하는 공식 가벼운(slim) 이미지를 가져와 빌드를 진행합니다.
FROM eclipse-temurin:17-jdk-jammy AS builder

# 컨테이너 내부에 작업 디렉토리를 생성합니다.
WORKDIR /app

# Gradle Wrapper와 관련된 설정 파일들을 먼저 복사합니다.
# (소스 코드 변경과 무관하게 캐시를 활용하기 위함입니다)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 윈도우 환경에서 복사된 gradlew 파일에 실행 권한을 부여합니다.
RUN chmod +x ./gradlew

# 의존성 라이브러리들을 미리 다운로드합니다. (소스 코드가 바뀌어도 캐시를 타서 속도가 빠릅니다)
RUN ./gradlew dependencies --no-daemon || true

# 실제 프로젝트의 소스 코드를 복사합니다.
COPY src src

# 프로젝트를 빌드하여 실행 가능한 JAR 파일을 생성합니다. (테스트는 생략하여 빌드 속도를 높입니다)
RUN ./gradlew bootJar -x test --no-daemon

# ==============================================================================
# 2단계: 실행 환경 (Run Stage)
# ==============================================================================
# 실제로 애플리케이션을 구동할 런타임용 얇은(slim) 이미지를 가져옵니다.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 1단계(builder)에서 완성된 JAR 파일만 런타임 이미지로 쏙 복사해 옵니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 밖으로 개방할 포트 번호를 명시합니다. (기본 스프링 부트 포트 8080)
EXPOSE 8080

# 컨테이너가 켜질 때 스프링 부트 서버를 가동시키는 명령어입니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
