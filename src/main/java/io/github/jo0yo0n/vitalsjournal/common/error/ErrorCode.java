package io.github.jo0yo0n.vitalsjournal.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // 요청 오류
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  // 인증/인가 오류
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

  // Health Record
  HEALTH_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "건강 기록을 찾을 수 없습니다."),
  EDIT_WINDOW_EXPIRED(HttpStatus.FORBIDDEN, "수정 가능 시간이 지났습니다."),
  HEALTH_RECORD_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "건강 기록 타입과 요청 데이터가 일치하지 않습니다."),

  // Threshold
  THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "설정된 임계값을 찾을 수 없습니다."),
  THRESHOLD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 설정된 임계값이 있습니다."),
  INVALID_THRESHOLD_RANGE(HttpStatus.BAD_REQUEST, "임계값 범위가 올바르지 않습니다."),

  // Alert
  ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

  // 서버 내부 오류
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getDefaultMessage() {
    return message;
  }
}
