# Vitals Journal Server

건강 지표(심박/혈압)를 기록 및 개인 규칙으로 이상 징후를 감지하고, 인앱 알림으로 알려주는 크로스플랫폼 모바일 앱

## 기술 스택
- Java 17
- Spring Boot 4
- PostgreSQL 18
- Flyway

## Docs
- MVP: `docs/mvp/`
- ERD: `docs/erd/`
- OpenAPI: `docs/api/`

## A. 빠른 시작 (fallback 기본값)
```bash
docker compose up -d
./gradlew bootRun
```

## B. 권장 시작 방식 (.env 사용)
```bash
cp .env.example .env

// .env 환경 변수를 개인 설정 후
docker compose up
set -a; source .env; set +a
./gradlew bootRun
```