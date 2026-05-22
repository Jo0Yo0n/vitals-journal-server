# Vitals Journal Server

심박수와 혈압을 기록하고, 사용자별 임계값을 기준으로 이상 징후를 평가해 앱 내 알림을 생성하는 모바일 앱 백엔드 서버입니다.

## 핵심 흐름

```text
회원가입 → 로그인 → 임계값 설정 → 건강 기록 입력 → 임계값 평가 → 알림 확인
```

---

## 구현 상태

| 영역 | 상태 | 설명 |
|------|------|------|
| 회원가입 | 완료 | 이메일/닉네임 중복 검증, 비밀번호 해싱 |
| 로그인 | 완료 | JWT Access Token 발급 |
| 내 정보 조회 | 완료 | 인증된 사용자 정보 조회 |
| Threshold 조회/설정 | 완료 | metric 단위 upsert |
| 건강 기록 생성/조회 | 완료 | HR/BP 기록 생성 및 조회 |
| Threshold 평가 | 완료 | 건강 기록 생성 시 현재 threshold 기준으로 violation 판단 |
| Alert | 완료 | violation이 1개 이상 발생하면 건강 기록당 alert 1개 생성 및 읽음 처리 |

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security + OAuth2 Resource Server (JWT) |
| Database | PostgreSQL 18 |
| ORM | Spring Data JPA |
| Migration | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Code Style | Spotless + Google Java Format |
| Build | Gradle (Kotlin DSL) |
| Container | Docker / Docker Compose |

---

## API Overview

| Method | Endpoint | 설명 | 인증 | 상태 |
|--------|----------|------|------|------|
| POST | `/auth/register` | 회원가입 | X | 완료 |
| POST | `/auth/login` | 로그인 및 Access Token 발급 | X | 완료 |
| GET | `/user/me` | 내 정보 조회 | O | 완료 |
| GET | `/thresholds` | 내 threshold 목록 조회 | O | 완료 |
| PUT | `/thresholds/{metric}` | metric 단위 threshold 생성/수정 | O | 완료 |
| POST | `/health-records` | 건강 기록 생성 | O | 완료 |
| GET | `/health-records` | 건강 기록 목록 조회 | O | 완료 |
| GET | `/health-records/{healthRecordId}` | 건강 기록 상세 조회 | O | 완료 |
| GET | `/alerts` | 알림 목록 조회 | O | 완료 |
| PATCH | `/alerts/{alertId}/read` | 알림 읽음 처리 | O | 완료 |

---

## MVP 기능 범위

### 인증/계정
- 회원가입, 로그인
- JWT Access Token 발급 (만료 시간: 30분)
- 내 정보 조회
- 로그아웃은 클라이언트 측에서 토큰 삭제로 처리

### 건강 기록 생성/조회
- 심박수(HR) 기록 입력
- 혈압(BP) 기록 입력 (수축기 + 이완기)
- 측정 시각(`measuredAt`) 저장
- 건강 기록 목록 및 단건 상세 조회 (평가 결과 및 위반 내역 포함)

### 사용자 임계값 설정
- 로그인한 사용자의 metric별 threshold를 조회하고, `PUT /thresholds/{metric}`으로 생성 또는 수정
- 지원 지표: `HR` (심박수), `BP_SYS` (수축기 혈압), `BP_DIA` (이완기 혈압)
- 규칙 타입: 범위 기반 (`minValue` ~ `maxValue`)

### 규칙 기반 평가
- 건강 기록 생성 시 현재 임계값 기준으로 즉시 평가
- 위반 발생 시 `record_violation` 생성 (평가 당시 threshold snapshot 저장)
- 건강 기록 1개에서 위반이 1개 이상 발생하면 `alert`를 최대 1개 생성

### 알림
- 앱 내 알림 목록 조회
- 알림 읽음 처리
- 알림의 `healthRecordId`로 건강 기록 상세로 이동 가능

---

## 핵심 설계 판단

### 1. 1차 MVP 범위 축소

최초 MVP에는 인증 고도화, 기록 수정/삭제, 고급 조회, 그래프, 푸시 알림같은 기능 후보가 많았습니다.

하지만 1차 MVP의 목표를 “기능을 많이 제공하는 것”이 아니라, 사용자가 실제로 아래 핵심 흐름을 끝까지 수행할 수 있는 상태를 만드는 것으로 정의했습니다.

```text
회원가입 → 로그인 → 임계값 설정 → 건강 기록 입력 → 임계값 평가 → 알림 확인
```

이 기준에 벗어나는 기능들은 1차 범위에서 제외했습니다.

인증도 같은 기준으로 단순화했습니다. 1차 MVP에서는 Refresh Token 없이 JWT Access Token만 사용하며, 토큰 만료 시 사용자가 다시 로그인하는 방식으로 처리합니다.

이를 통해 인증, 건강 기록, 임계값 평가, 알림 생성이라는 핵심 흐름을 먼저 배포 가능한 형태로 완성하는 것을 우선했습니다.

---

### 2. ProblemDetail 기반 에러 응답 표준화

자체 `ErrorResponse` DTO를 별도로 유지하기보다 Spring의 `ProblemDetail`을 기반으로 에러 응답을 구성했습니다.

HTTP 에러 응답의 공통 필드인 `type`, `title`, `status`, `detail`, `instance`를 사용하고, 서비스 도메인 오류 식별을 위해 `errorCode`를 확장 필드로 추가했습니다.

이를 통해 validation error, 인증 실패, 도메인 예외를 API 전반에서 일관된 형식으로 응답하도록 했습니다.

---

### 3. Threshold 변경 이력 대신 Violation Snapshot 저장

Threshold가 나중에 변경되더라도 과거 건강 기록의 평가 결과가 바뀌면 안 되므로, `record_violation` 테이블에 threshold의 snapshot을 저장하는 방식을 선택했습니다.

따라서 `record_violation`은 threshold row를 직접 참조하지 않고, 평가 당시의 `minValue`, `maxValue`를 snapshot으로 저장합니다.

1차 MVP에서는 `HR`, `BP_SYS`, `BP_DIA`에 대한 range rule만 지원하기 때문에 min/max snapshot으로 충분하다고 판단했습니다.

향후 rule type이 확장되면 `record_violation` 테이블을 `rule_type`, `rule_snapshot` 컬럼 방식으로 교체하거나, 조회와 검증 요구가 커질 경우 `threshold_rule` 상위 테이블과 rule type별 detail table로 정규화하는 방식도 검토할 수 있습니다.

---

### 4. BP 기록을 BP_SYS / BP_DIA metric으로 분리 평가

혈압은 하나의 기록이지만 수축기 혈압과 이완기 혈압은 서로 다른 기준으로 평가됩니다.

따라서 건강 기록 타입은 `BP`로 유지하되, 평가 시에는 `BP_SYS`, `BP_DIA` metric으로 분리했습니다.

이를 통해 하나의 혈압 기록에서 수축기와 이완기 각각의 threshold 위반 여부를 독립적으로 판단할 수 있도록 했습니다.

---

### 5. Threshold upsert의 유일성 보장

사용자 1명은 metric 하나에 대해 threshold를 최대 1개만 가질 수 있도록 `user_id`, `metric` 조합에 unique index를 적용했습니다.

애플리케이션 레벨에서 기존 threshold를 조회한 뒤 생성/수정하더라도, 동시에 같은 metric에 대한 생성 요청이 들어오면 중복 row가 생길 수 있습니다.

따라서 DB constraint를 최종 방어선으로 두어 사용자별 metric threshold의 유일성을 보장했습니다.

현재 MVP에서는 중복 row 생성을 막는 것에 집중하고, 동시 upsert 요청을 더 자연스럽게 처리하기 위한 retry 또는 DB-native upsert는 이후 개선 대상으로 둡니다.

---

## 데이터베이스 구조

```text
users 1 ── N health_record
users 1 ── N threshold
users 1 ── N alert

health_record 1 ── N record_violation
health_record 1 ── 0..1 alert
```

주요 테이블: `users`, `health_record`, `threshold`, `record_violation`, `alert`

> 자세한 내용은 [`docs/erd/ERD.md`](docs/erd/ERD.md) 참고.

---

## 프로젝트 구조

```
src/main/java/io/github/jo0yo0n/vitalsjournal/
├── alert/          # 알림 도메인
├── auth/           # 인증/JWT 처리
├── common/         # 공통 유틸리티, 예외 처리
├── config/         # Spring 설정 (Security, JPA 등)
├── healthrecord/   # 건강 기록 도메인
├── threshold/      # 임계값 도메인
└── user/           # 사용자 도메인
```

---

## 로컬 실행

최초 실행 시 키 생성
```bash
mkdir -p .local/keys
openssl genpkey -algorithm RSA -out .local/keys/local-private.key -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in .local/keys/local-private.key -out .local/keys/local-public.pub
```

```bash
cp .env.example .env  # .env 파일 생성 후, 필요한 경우 값 수정 (예: DB 접속 정보)
docker compose up -d
./gradlew bootRun
```

---

## 테스트
### 테스트 실행
```bash
# 테스트 실행
./gradlew test

# 테스트 + 코드 스타일/정적 검사
./gradlew check
```

### 테스트 전략
- Domain test: 도메인 객체의 불변식과 상태 변경 검증
- Service test: 비즈니스 규칙과 예외 흐름 검증
- Controller test: 요청 validation, 인증/인가, 응답 status/body 검증
- Repository test: DB 제약 조건과 조회 동작 검증

### 현재 테스트 범위

| 영역 | 테스트 내용 |
|------|-------------|
| Auth | 회원가입, 로그인, JWT 발급, 인증 실패 응답 |
| User | 인증된 사용자 조회, 사용자 없음, 잘못된 JWT subject 처리 |
| Threshold | threshold 생성/수정, 목록 조회, range 검증 |
| Common Error | Problem Details 기반 예외 응답, validation error mapping |

---

## Docs

| 문서 | 경로 |
|------|------|
| MVP 기획 | [`docs/mvp/MVP.md`](docs/mvp/MVP.md) |
| ERD | [`docs/erd/ERD.md`](docs/erd/ERD.md) |
| OpenAPI (Swagger UI) | `http://localhost:8080/swagger-ui.html` (서버 실행 후) |

---

## 향후 계획
1. 백엔드 배포
2. 최소 프론트엔드 연동
