# Git Workflow Guide

이 문서는 개인/소규모 팀에서 `main` 품질을 유지하면서 빠르게 개발하기 위한 최소 규칙입니다.

## 1) 브랜치 전략

### 기본 원칙
- `main`은 항상 배포 가능한 상태를 유지합니다.
- 기능/수정은 짧은 수명 브랜치에서 작업하고 PR로 병합합니다.

### 브랜치 종류와 네이밍
- `main`: 배포 기준 브랜치
- `feature/<scope>-<topic>`: 기능 개발
- `fix/<scope>-<topic>`: 일반 버그 수정
- `hotfix/<scope>-<topic>`: 운영 긴급 수정
- `chore/<scope>-<topic>`: 설정/문서/빌드 작업

네이밍 규칙
- 소문자 kebab-case 사용
- scope는 도메인 또는 계층(`auth`, `vitals`, `api`, `infra`)
- topic은 작업 의도를 짧게 표현

예시
- `feature/auth-social-login`
- `feature/vitals-weekly-summary`
- `fix/api-null-response`
- `hotfix/auth-token-validation`
- `chore/infra-gradle-upgrade`

검증 포인트
- 브랜치 이름만 보고 작업 목적이 이해되는가?
- 한 브랜치에 한 목적만 담겨 있는가?

## 2) 커밋 메시지 규칙 (Conventional Commits)

형식
```text
<type>(<scope>): <subject>
```

권장 type
- `feat`: 사용자 기능 추가
- `fix`: 버그 수정
- `refactor`: 동작 변화 없는 구조 개선
- `test`: 테스트 추가/수정
- `docs`: 문서 수정
- `chore`: 빌드/설정/의존성 등

작성 규칙
- subject는 명령형 현재 시제, 50자 이내 권장
- 한 커밋에는 하나의 논리적 변경만 포함
- 필요 시 본문에 변경 이유와 영향 범위를 기록

예시
- `feat(auth): add refresh token rotation`
- `fix(vitals): handle empty blood pressure payload`
- `refactor(api): split journal service validation`
- `docs(readme): add git workflow link`

## 3) PR 규칙

필수 규칙
- `main` 직접 push 금지
- PR 기반 병합

## 4) 태그/릴리즈 규칙

형식
- `vMAJOR.MINOR.PATCH` (SemVer)

증가 기준
- `MAJOR`: 하위 호환 깨짐
- `MINOR`: 하위 호환 유지 + 기능 추가
- `PATCH`: 버그 수정/내부 개선

예시
- `v1.0.0`: 첫 안정 릴리즈
- `v1.1.0`: 신규 API 추가
- `v1.1.1`: 버그 수정

검증 포인트
- 특정 이슈 발생 시 태그 기준으로 즉시 이전 버전 배포가 가능한가?

## 5) 기능 작업 표준 절차

1. 이슈 정의: 목표/범위/완료 조건(DoD) 작성
2. 브랜치 생성: `main` 최신화 후 `feature/*` 생성
3. 구현: 작은 커밋 단위로 진행
4. 검증: 로컬 테스트 + 수동 시나리오 확인
5. PR 작성: 변경점/검증/영향도/롤백 방법 명시
6. 리뷰 반영: 피드백 반영 후 재검증
7. 병합/배포: squash merge 후 태그 생성(필요 시)
8. 정리: 작업 브랜치 삭제

명령 예시
```bash
git switch main
git pull origin main
git switch -c feature/vitals-weekly-summary

# ... 작업 ...

git add .
git commit -m "feat(vitals): add weekly summary endpoint"
git push -u origin feature/vitals-weekly-summary
```

## 6) PR 전 셀프 체크리스트

- [ ] PR 목적이 1문장으로 설명된다.
- [ ] API/DB/보안 영향이 문서화되었다.
- [ ] 테스트(자동/수동) 결과를 남겼다.
- [ ] 롤백 방법을 적었다.
- [ ] 불필요한 리팩터링/변경이 섞이지 않았다.