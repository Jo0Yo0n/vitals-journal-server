package io.github.jo0yo0n.vitalsjournal.common.error;

import java.net.URI;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // 요청 오류
  VALIDATION_ERROR(
      HttpStatus.BAD_REQUEST,
      URI.create("http://localhost:8080/problems/validation-error"),
      "Request validation failed"),
  INVALID_REQUEST(
      HttpStatus.BAD_REQUEST,
      URI.create("http://localhost:8080/problems/invalid-request"),
      "Invalid request"),

  // 인증/인가 오류
  EMAIL_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      URI.create("http://localhost:8080/problems/email-already-exists"),
      "Email already exists"),
  NICKNAME_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      URI.create("http://localhost:8080/problems/nickname-already-exists"),
      "Nickname already exists"),
  INVALID_CREDENTIALS(
      HttpStatus.UNAUTHORIZED,
      URI.create("http://localhost:8080/problems/invalid-credentials"),
      "Invalid credentials"),
  ACCOUNT_DELETED(
      HttpStatus.FORBIDDEN,
      URI.create("http://localhost:8080/problems/account-deleted"),
      "Account deleted"),
  INVALID_REFRESH_TOKEN(
      HttpStatus.UNAUTHORIZED,
      URI.create("http://localhost:8080/problems/invalid-refresh-token"),
      "Invalid refresh token"),
  UNAUTHORIZED(
      HttpStatus.UNAUTHORIZED,
      URI.create("http://localhost:8080/problems/unauthorized"),
      "Unauthorized"),

  // Health Record
  HEALTH_RECORD_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      URI.create("http://localhost:8080/problems/health-record-not-found"),
      "Health record not found"),
  EDIT_WINDOW_EXPIRED(
      HttpStatus.CONFLICT,
      URI.create("http://localhost:8080/problems/edit-window-expired"),
      "Edit window expired"),
  HEALTH_RECORD_TYPE_MISMATCH(
      HttpStatus.BAD_REQUEST,
      URI.create("http://localhost:8080/problems/health-record-type-mismatch"),
      "Health record type mismatch"),

  // Threshold
  THRESHOLD_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      URI.create("http://localhost:8080/problems/threshold-not-found"),
      "Threshold not found"),
  THRESHOLD_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      URI.create("http://localhost:8080/problems/threshold-already-exists"),
      "Threshold already exists"),
  INVALID_THRESHOLD_RANGE(
      HttpStatus.BAD_REQUEST,
      URI.create("http://localhost:8080/problems/invalid-threshold-range"),
      "Invalid threshold range"),

  // Alert
  ALERT_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      URI.create("http://localhost:8080/problems/alert-not-found"),
      "Alert not found"),

  // 서버 내부 오류
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      URI.create("http://localhost:8080/problems/internal-server-error"),
      "Internal server error");

  private final HttpStatus httpStatus;
  private final URI type;
  private final String title;

  ErrorCode(HttpStatus status, URI type, String title) {
    this.httpStatus = status;
    this.type = type;
    this.title = title;
  }

  public HttpStatus status() {
    return httpStatus;
  }

  public URI type() {
    return type;
  }

  public String title() {
    return title;
  }
}
