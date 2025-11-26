###########################################
# 1) Build Stage
###########################################
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace

# Gradle 캐시 최적화를 위해 설정 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 미리 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon

# 전체 소스 복사
COPY . .

# Spring Boot JAR 빌드
RUN gradle clean bootJar --no-daemon


###########################################
# 2) Runtime Stage
###########################################
FROM eclipse-temurin:17-jre
ENV APP_HOME=/app
WORKDIR $APP_HOME

# 한국 시간대 설정
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

# 비루트 사용자 생성 (보안 강화)
RUN groupadd --system app && useradd --system -g app app

# 빌드 결과물 복사 (권한 포함)
COPY --from=builder --chown=app:app /workspace/build/libs/*.jar app.jar

USER app

# Spring Boot 기본 포트
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
