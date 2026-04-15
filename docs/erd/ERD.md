# ERD (최종 반영본)

## 핵심 정책 요약

> - HealthRecord는 사실 데이터(기록)이며, 생성 후 10분(created_at 기준)까지만 수정 가능(애플리케이션 정책).
> - HealthRecord 생성/수정 시점에 현재 활성 Threshold(threshold.deleted_at IS NULL) 기준으로 평가하여 RecordViolation을 생성하고, 위반이 1개 이상이면 Alert 1개를 생성.
> - HealthRecord 삭제(soft-delete)시 해당 record로 생성된 Alert 및 RecordViolation도 함께 삭제(soft-delete)
> - Threshold 삭제/수정(soft delete + 버전 생성)은 향후 평가에만 영향. 기존 Alert/RecordViolation은 자동 변경/삭제하지 않음.
> - 기록 상세에서 표시하는 violations는 해당 record가 마지막으로 평가된 시점(evaluated_at)의 결과(이벤트/평가 이력).

## users

- id
  - PK, BIGSERIAL
- email
  - NOT NULL, CITEXT(320)
- hashed_password
  - NOT NULL, VARCHAR(255)
- nickname
  - NOT NULL, VARCHAR(50)
- created_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- deleted_at
  - TIMESTAMPZ NULL

➕ `CREATE UNIQUE INDEX ux_users_email_active ON users(email) WHERE deleted_at IS NULL;`
➕ `CREATE UNIQUE INDEX ux_users_nickname_active ON users(nickname) WHERE deleted_at IS NULL;`

## health_record

- id
  - PK, BIGSERIAL
- user_id
  - FK, BIGINT NOT NULL
- record_type
  - VARCHAR(16) NOT NULL CHECK (record_type IN (‘HR’,‘BP’))
- measured_at
  - TIMESTAMPZ NOT NULL
- bpm
  - SMALLINT NULL CHECK (bpm BETWEEN 1 AND 300)
- systolic
  - SMALLINT NULL CHECK (systolic BETWEEN 50 AND 300)
- diastolic
  - SMALLINT NULL CHECK (diastolic BETWEEN 30 AND 200)
- memo
  - TEXT NULL
- created_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
  - 입력 시각(평가 기준의 기본)
- updated_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- deleted_at
  - TIMESTAMPZ NULL

➕ `CHECK (systolic IS NULL OR diastolic IS NULL OR systolic > diastolic)`

➕ type별 무결성(DB CHECK로 강제)

- `CHECK (
(type=‘HR’ AND bpm IS NOT NULL AND systolic IS NULL AND diastolic IS NULL)
OR
(type=‘BP’ AND bpm IS NULL AND systolic IS NOT NULL AND diastolic IS NOT NULL)
)`

➕ `INDEX (user_id, measured_at DESC) WHERE deleted_at IS NULL`

➕ `INDEX (user_id, created_at DESC) WHERE deleted_at IS NULL`

## threshold (soft delete + 버전 row)

- id
  - PK, BIGSERIAL
- user_id
  - FK, BIGINT NOT NULL
- metric
  - VARCHAR(16) NOT NULL CHECK (metric IN (‘HR’,‘BP_SYS’,‘BP_DIA’))
- rule_type
  - VARCHAR(16) NOT NULL CHECK (rule_type IN (‘range’))
- created_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- updated_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- deleted_at
  - TIMESTAMPZ NULL

> ### 정책
>
> - 수정: 기존 row deleted_at=now() 처리 후, 새 row 생성.
> - 삭제: hard delete 금지, deleted_at=now() 처리.
> - updated_at은 내부 관리 필드로 API 응답에는 포함 안됨
> - 삭제/수정된 threshold는 **향후 평가에서만 제외/적용**. 기존 Alert/RecordViolation은 유지.

➕ `UNIQUE (user_id, metric, rule_type) WHERE deleted_at IS NULL`

➕ `INDEX (user_id, metric, rule_type) WHERE deleted_at IS NULL`

➕ `INDEX (user_id) WHERE deleted_at IS NULL`

## threshold_range (rule_type=range일 때)

- threshold_id
  - PK, FK BIGINT REFERENCES threshold(id) ON DELETE CASCADE
- min_value
  - NUMERIC(10,2) NULL
- max_value
  - NUMERIC(10,2) NULL

➕ `CHECK (min_value IS NULL OR max_value IS NULL OR min_value < max_value)`

## alert (record 당 최대 1개)

- id
  - PK, BIGSERIAL
- health_record_id
  - FK, BIGINT NOT NULL REFERENCES health_record(id) ON DELETE CASCADE
- is_silent
  - BOOLEAN NOT NULL DEFAULT false
- read_at
  - TIMESTAMPZ NULL
- created_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- deleted_at
  - TIMESTAMPZ NULL

> ### 정책
>
> - 위반이 1개 이상이면 alert 1개 생성.
> - 과거 기록 입력 시 Alert는 생성하되 Alert.is_silent=true (조회에서 is_silent=true 알림은 제외)
> - health_record 수정 시 해당 record의 alert는 soft delete 후 재평가 결과로 재생성.

➕ `UNIQUE (health_record_id) WHERE deleted_at IS NULL`

➕ `INDEX (created_at DESC) WHERE deleted_at IS NULL`

➕ `INDEX (health_record_id) WHERE deleted_at IS NULL`

## record_violation (평가 이력/상세)

- id
  - PK, BIGSERIAL
- health_record_id
  - FK, BIGINT NOT NULL REFERENCES health_record(id) ON DELETE CASCADE
- threshold_id
  - FK, BIGINT NOT NULL REFERENCES threshold(id)
- metric
  - VARCHAR(16) NOT NULL CHECK (metric IN (‘HR’,‘BP_SYS’,‘BP_DIA’))
- rule_type
  - VARCHAR(16) NOT NULL CHECK (rule_type IN (‘range’))
- measured_value
  - NUMERIC(10,2) NOT NULL
- direction
  - VARCHAR(16) NULL CHECK (direction IN (‘below_min’,‘above_max’))
- evaluated_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- deleted_at
  - TIMESTAMPZ NULL

> ### 정책:
>
> - 기록 상세에서 보여주는 violations는 해당 record가 마지막으로 평가된 시점(evaluated_at)의 결과.
> - threshold 삭제/수정은 기존 violations를 삭제하거나 수정하지 않음.

➕ `UNIQUE (health_record_id, threshold_id) WHERE deleted_at IS NULL`

➕ `INDEX (health_record_id) WHERE deleted_at IS NULL`

➕ `INDEX (threshold_id) WHERE deleted_at IS NULL`

➕ `INDEX (evaluated_at DESC) WHERE deleted_at IS NULL`

## refresh_token (로테이션/폐기)

- id
  - PK, BIGSERIAL
- user_id
  - FK, BIGINT NOT NULL REFERENCES “user”(id)
- token_hash
  - VARCHAR(255) NOT NULL
  - 원문 저장 금지
- issued_at
  - TIMESTAMPZ NOT NULL DEFAULT now()
- expires_at
  - TIMESTAMPZ NOT NULL
- revoked_at
  - TIMESTAMPZ NULL
- created_at
  - TIMESTAMPZ NOT NULL DEFAULT now()

> ### 정책:
>
> - 유효 = (revoked_at IS NULL) AND (now() < expires_at)
> - refresh 재발급 시 기존 토큰 revoked_at=now()로 폐기(로테이션)
> - logout 시 현재 refresh 토큰 revoked_at=now()

➕ `UNIQUE (token_hash)`

➕ `INDEX (user_id, expires_at DESC) WHERE revoked_at IS NULL`

➕ `INDEX (expires_at) WHERE revoked_at IS NULL`
