# ERD (1차 MVP 단순화 버전)

1차 MVP의 ERD는 “기능 확장 가능성”보다 “빠르게 구현하고 배포 가능한 구조”를 우선합니다.

핵심 목표는 다음 흐름을 안정적으로 구현하는 것입니다.

```text
User → Threshold 설정
User → HealthRecord 생성
HealthRecord 생성 시 Threshold 평가
평가 결과로 RecordViolation 생성
Violation이 있으면 Alert 생성
```

---

## 1. 단순화 원칙

### 제거한 것

- `refresh_token` 테이블
- 회원 탈퇴를 위한 `users.deleted_at`
- `threshold_range` 테이블
- threshold 버전 관리
- threshold soft-delete
- health_record soft-delete
- health_record 수정/삭제 정책
- alert soft-delete
- alert 삭제 정책
- record_violation soft-delete
- alert `is_silent`
- record_violation → threshold 직접 FK

### 유지한 것

- 사용자별 건강 기록
- 사용자별 임계값
- 건강 기록 생성 시점의 평가 결과 저장
- 앱 내 알림
- 과거 평가 결과 보존을 위한 violation snapshot

### 수정한 것

`record_violation`은 threshold row를 직접 참조하지 않습니다.
대신 평가 당시의 `min_value`, `max_value`를 snapshot으로 저장합니다.

---

## 2. 테이블 관계 요약

```text
users 1 ── N health_record
users 1 ── N threshold
users 1 ── N alert

health_record 1 ── N record_violation
health_record 1 ── 0..1 alert
```

---

## 3. users

사용자 계정 정보입니다.

### columns

- `id`
  - PK, BIGSERIAL
- `email`
  - NOT NULL, CITEXT(320)
- `hashed_password`
  - NOT NULL, VARCHAR(255)
- `nickname`
  - NOT NULL, VARCHAR(50)
- `created_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()

### constraints / indexes

```sql
CREATE UNIQUE INDEX ux_users_email ON users(email);
CREATE UNIQUE INDEX ux_users_nickname ON users(nickname);
```

### 정책

- 1차 MVP에서는 회원 탈퇴를 제공하지 않습니다.
- 따라서 `deleted_at`은 두지 않습니다.
- 탈퇴/재가입 정책은 2차 이후에 별도 설계합니다.

---

## 4. health_record

사용자가 입력한 심박/혈압 측정 기록입니다.

### columns

- `id`
  - PK, BIGSERIAL
- `user_id`
  - FK, BIGINT NOT NULL REFERENCES users(id)
- `record_type`
  - VARCHAR(16) NOT NULL
  - `HR`, `BP`
- `measured_at`
  - TIMESTAMPTZ NOT NULL
- `bpm`
  - SMALLINT NULL
- `systolic`
  - SMALLINT NULL
- `diastolic`
  - SMALLINT NULL
- `memo`
  - TEXT NULL
  - 최대 500자
- `created_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()

### constraints / indexes

```sql
ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_record_type
  CHECK (record_type IN ('HR', 'BP'));

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_bpm_range
  CHECK (bpm IS NULL OR bpm BETWEEN 1 AND 300);

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_systolic_range
  CHECK (systolic IS NULL OR systolic BETWEEN 50 AND 300);

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_diastolic_range
  CHECK (diastolic IS NULL OR diastolic BETWEEN 30 AND 200);

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_memo_length
  CHECK (memo IS NULL OR char_length(memo) <= 500);

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_bp_order
  CHECK (systolic IS NULL OR diastolic IS NULL OR systolic > diastolic);

ALTER TABLE health_record
  ADD CONSTRAINT ck_health_record_record_type_columns
  CHECK (
    (record_type = 'HR' AND bpm IS NOT NULL AND systolic IS NULL AND diastolic IS NULL)
    OR
    (record_type = 'BP' AND bpm IS NULL AND systolic IS NOT NULL AND diastolic IS NOT NULL)
  );

CREATE INDEX ix_health_record_user_measured_at
  ON health_record(user_id, measured_at DESC);

CREATE INDEX ix_health_record_user_created_at
  ON health_record(user_id, created_at DESC);
```

### 정책

- 1차 MVP에서는 건강 기록 수정/삭제를 제공하지 않습니다.
- 기록 생성 시 현재 threshold 기준으로 즉시 평가합니다.
- 설정된 threshold가 없는 metric은 평가하지 않습니다.
- 평가 결과는 `record_violation`에 snapshot으로 저장합니다.

---

## 5. threshold

사용자별 건강 지표 임계값입니다.

기존 ERD의 `threshold` + `threshold_range`를 하나의 테이블로 합칩니다.
1차 MVP에서는 range 규칙만 지원하므로 별도 rule table이 필요 없습니다.

### columns

- `id`
  - PK, BIGSERIAL
- `user_id`
  - FK, BIGINT NOT NULL REFERENCES users(id)
- `metric`
  - VARCHAR(16) NOT NULL
  - `HR`, `BP_SYS`, `BP_DIA`
- `min_value`
  - NUMERIC(10,2) NULL
- `max_value`
  - NUMERIC(10,2) NULL
- `created_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()
- `updated_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()

### constraints / indexes

```sql
ALTER TABLE threshold
  ADD CONSTRAINT ck_threshold_metric
  CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA'));

ALTER TABLE threshold
  ADD CONSTRAINT ck_threshold_at_least_one_bound
  CHECK (min_value IS NOT NULL OR max_value IS NOT NULL);

ALTER TABLE threshold
  ADD CONSTRAINT ck_threshold_min_max
  CHECK (min_value IS NULL OR max_value IS NULL OR min_value <= max_value);

CREATE UNIQUE INDEX ux_threshold_user_metric
  ON threshold(user_id, metric);

CREATE INDEX ix_threshold_user
  ON threshold(user_id);
```

### 정책

- 사용자 1명은 metric 하나당 threshold를 최대 1개만 가질 수 있습니다.
- API는 metric 기준 upsert 방식으로 동작합니다.
  - 기존 threshold가 없으면 생성합니다.
  - 기존 threshold가 있으면 해당 row를 단순 업데이트합니다.
- threshold 삭제는 1차 MVP에서 제공하지 않습니다.
- threshold 버전 관리는 2차 이후 기능으로 미룹니다.

---

## 6. record_violation

건강 기록 생성 시 평가된 위반 내역입니다.

threshold row를 직접 참조하지 않고, 평가 당시의 threshold 값을 snapshot으로 저장합니다.

### columns

- `id`
  - PK, BIGSERIAL
- `health_record_id`
  - FK, BIGINT NOT NULL REFERENCES health_record(id) ON DELETE CASCADE
- `metric`
  - VARCHAR(16) NOT NULL
  - `HR`, `BP_SYS`, `BP_DIA`
- `measured_value`
  - NUMERIC(10,2) NOT NULL
- `min_value_snapshot`
  - NUMERIC(10,2) NULL
- `max_value_snapshot`
  - NUMERIC(10,2) NULL
- `direction`
  - VARCHAR(16) NOT NULL
  - `below_min`, `above_max`
- `evaluated_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()

### constraints / indexes

```sql
ALTER TABLE record_violation
  ADD CONSTRAINT ck_record_violation_metric
  CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA'));

ALTER TABLE record_violation
  ADD CONSTRAINT ck_record_violation_direction
  CHECK (direction IN ('below_min', 'above_max'));

ALTER TABLE record_violation
  ADD CONSTRAINT ck_record_violation_at_least_one_bound
  CHECK (min_value_snapshot IS NOT NULL OR max_value_snapshot IS NOT NULL);

ALTER TABLE record_violation
  ADD CONSTRAINT ck_record_violation_min_max_snapshot
  CHECK (
    min_value_snapshot IS NULL
    OR max_value_snapshot IS NULL
    OR min_value_snapshot <= max_value_snapshot
  );

CREATE UNIQUE INDEX ux_record_violation_record_metric
  ON record_violation(health_record_id, metric);

CREATE INDEX ix_record_violation_health_record
  ON record_violation(health_record_id);

CREATE INDEX ix_record_violation_evaluated_at
  ON record_violation(evaluated_at DESC);
```

### 정책

- violation은 건강 기록 생성 시점의 평가 결과입니다.
- threshold가 나중에 수정되어도 기존 violation은 변경하지 않습니다.
- threshold 버전 관리를 하지 않아도 과거 평가 결과를 설명할 수 있도록 snapshot을 저장합니다.
- 혈압 기록(`BP`)은 `BP_SYS`, `BP_DIA` metric으로 나누어 평가하므로 한 health record에서 violation이 최대 2개 생성될 수 있습니다.

---

## 7. alert

사용자에게 보여줄 앱 내 알림입니다.

### columns

- `id`
  - PK, BIGSERIAL
- `user_id`
  - FK, BIGINT NOT NULL REFERENCES users(id)
- `health_record_id`
  - FK, BIGINT NOT NULL REFERENCES health_record(id) ON DELETE CASCADE
- `message`
  - VARCHAR(255) NOT NULL
- `read_at`
  - TIMESTAMPTZ NULL
- `created_at`
  - TIMESTAMPTZ NOT NULL DEFAULT now()

### constraints / indexes

```sql
CREATE UNIQUE INDEX ux_alert_health_record
  ON alert(health_record_id);

CREATE INDEX ix_alert_user_created_at
  ON alert(user_id, created_at DESC);
```

### 정책

- 건강 기록 1개당 alert는 최대 1개만 생성합니다.
- violation이 1개 이상일 때만 alert를 생성합니다.
- alert 목록 item에는 `health_record_id`가 포함됩니다.
- alert 상세가 필요하면 `health_record_id`로 건강 기록 상세 API를 조회합니다.
- alert 삭제는 1차 MVP에서 제공하지 않습니다.
- `is_silent`는 1차 MVP에서 사용하지 않습니다.
- 푸시 알림은 2차 이후 기능입니다.

---

## 8. 제거된 테이블

### refresh_token

1차 MVP에서는 refresh token을 사용하지 않으므로 제거합니다.

### threshold_range

1차 MVP에서는 range rule만 지원하므로 threshold 테이블의 `min_value`, `max_value`로 통합합니다.

---

## 9. 2차 이후에 다시 검토할 설계

아래 기능을 도입할 때만 ERD를 확장합니다.

- Refresh Token 도입
  - `refresh_token` 테이블 추가
- 회원 탈퇴 도입
  - `users.deleted_at` 또는 별도 계정 상태 필드 추가
- threshold 버전 관리 도입
  - threshold history 또는 version table 추가
- threshold rule type 확장
  - `threshold_range` 또는 rule detail table 재도입
- health record 수정/삭제 도입
  - soft-delete 정책 재검토
  - violation/alert 재평가 정책 재검토
- silent alert/푸시 알림 도입
  - `alert.is_silent` 또는 notification channel 정책 추가
- alert 상세/삭제 정책 도입
