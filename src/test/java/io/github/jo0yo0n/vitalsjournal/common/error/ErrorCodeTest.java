package io.github.jo0yo0n.vitalsjournal.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

  @DisplayName("VALIDATION_ERROR는 400과 validation-error type을 가진다.")
  @Test
  void validationErrorMetadata() {
    assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.status());
    assertEquals(
        URI.create("http://localhost:8080/problems/validation-error"),
        ErrorCode.VALIDATION_ERROR.type());
    assertEquals("Request validation failed", ErrorCode.VALIDATION_ERROR.title());
  }

  @DisplayName("NICKNAME_ALREADY_EXISTS는 409와 nickname-already-exists type을 가진다.")
  @Test
  void nicknameAlreadyExistsMetadata() {
    assertEquals(HttpStatus.CONFLICT, ErrorCode.NICKNAME_ALREADY_EXISTS.status());
    assertEquals(
        URI.create("http://localhost:8080/problems/nickname-already-exists"),
        ErrorCode.NICKNAME_ALREADY_EXISTS.type());
    assertEquals("Nickname already exists", ErrorCode.NICKNAME_ALREADY_EXISTS.title());
  }
}
