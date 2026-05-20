package io.github.jo0yo0n.vitalsjournal.common.error;

import java.net.URI;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public enum ErrorCode {

  // 요청 오류
  VALIDATION_ERROR(
      HttpStatus.BAD_REQUEST,
      problemType("http://localhost:8080/problems/validation-error"),
      "Request validation failed"),
  INVALID_REQUEST(
      HttpStatus.BAD_REQUEST,
      problemType("http://localhost:8080/problems/invalid-request"),
      "Invalid request"),

  // 인증/인가 오류
  EMAIL_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      problemType("http://localhost:8080/problems/email-already-exists"),
      "Email already exists"),
  NICKNAME_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      problemType("http://localhost:8080/problems/nickname-already-exists"),
      "Nickname already exists"),
  INVALID_CREDENTIALS(
      HttpStatus.UNAUTHORIZED,
      problemType("http://localhost:8080/problems/invalid-credentials"),
      "Invalid credentials"),
  UNAUTHORIZED(
      HttpStatus.UNAUTHORIZED,
      problemType("http://localhost:8080/problems/unauthorized"),
      "Unauthorized"),

  // User
  USER_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      problemType("http://localhost:8080/problems/user-not-found"),
      "User not found"),

  // Health Record
  HEALTH_RECORD_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      problemType("http://localhost:8080/problems/health-record-not-found"),
      "Health record not found"),
  HEALTH_RECORD_TYPE_MISMATCH(
      HttpStatus.BAD_REQUEST,
      problemType("http://localhost:8080/problems/health-record-type-mismatch"),
      "Health record type mismatch"),
  INVALID_BLOOD_PRESSURE_RANGE(
      HttpStatus.BAD_REQUEST,
      problemType("http://localhost:8080/problems/invalid-blood-pressure-range"),
      "Invalid blood pressure range"),

  // Threshold
  THRESHOLD_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      problemType("http://localhost:8080/problems/threshold-not-found"),
      "Threshold not found"),
  INVALID_THRESHOLD_RANGE(
      HttpStatus.BAD_REQUEST,
      problemType("http://localhost:8080/problems/invalid-threshold-range"),
      "Invalid threshold range"),

  // Alert
  ALERT_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      problemType("http://localhost:8080/problems/alert-not-found"),
      "Alert not found"),

  // 서버 내부 오류
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      problemType("http://localhost:8080/problems/internal-server-error"),
      "Internal server error");

  private final @NonNull HttpStatus httpStatus;
  private final @NonNull URI type;
  private final @NonNull String title;

  ErrorCode(@NonNull HttpStatus status, @NonNull URI type, @NonNull String title) {
    this.httpStatus = status;
    this.type = type;
    this.title = title;
  }

  public @NonNull HttpStatus status() {
    return httpStatus;
  }

  public @NonNull URI type() {
    return type;
  }

  public @NonNull String title() {
    return title;
  }

  private static @NonNull URI problemType(String value) {
    return Objects.requireNonNull(URI.create(value));
  }
}
