# MVP

건강 지표(심박/혈압)를 기록 및 개인 규칙으로 이상 징후를 감지하고, 인앱 알림으로 알려주는 크로스플랫폼 모바일 앱

## MVP 기능

1. 인증/계정
   - 회원가입/로그인
   - JWT Access Token + Refresh Token
   - 내 정보 조회/탈퇴
     - 탈퇴 후 재가입 가능(재가입은 새 user row 생성. 탈퇴 시 refresh_token 전부 revoked 처리)
2. 건강 데이터 입력/조회
   - 지표 입력(심박, 혈압)
   - 날짜/시간 포함하여 저장
   - 기간 조회(최근 7일/30일)
   - 상세 조회(입력값/임계값/위반 사항 리스트)
   - 내 기록 조회
3. 규칙 기반 이상 감지 + 알림 생성
   - 사용자별 기준 설정(임계값)
   - 임계값 기반 규칙 평가
4. 알림
   - 측정값 저장 시 임계치 초과(out of threshold) 여부를 평가하여 Alert 생성
   - 과거 입력 데이터는 기록 분석을 위해 Alert는 생성하되 인앱/푸시 생략을 DB로 명시 (Alert.is_silent = true)
     - 과거 기준: `measured_at < now() - 10m`
     - api 응답 시 is_silent=true는 기본 제외
   - 앱 내 알림 보관함
   - 읽음 처리

### 추후 추가 기능

- 연속성 평가(ex. 24시간 내 3회 중 2회 초과)
- 쿨다운(ex. 동일 유형 알림 30분 내 중복 방지)
- 데이터 내보내기(CSV)
- 측정 값 추가
- 푸시 알림
- 재평가 버튼
- 그래프

## 주요 기술 스택

- Java 17
- Spring 3.x
- PostgreSQL
- JPA
- JWT (Authorization 헤더에 저장)
- Swagger
- AWS EC2 + RDS
- React Native(Expo)
