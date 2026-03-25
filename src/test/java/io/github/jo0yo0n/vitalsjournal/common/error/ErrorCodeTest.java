package io.github.jo0yo0n.vitalsjournal.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class ErrorCodeTest {

  @DisplayName("ACCOUNT_DELETED는 403과 account-deleted type을 가진다.")
  @Test
  void accountDeletedMetadata() {

    assertEquals(HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_DELETED.status());
    assertEquals(
        URI.create("http://localhost:8080/problems/account-deleted"),
        ErrorCode.ACCOUNT_DELETED.type());
    assertEquals("Account deleted", ErrorCode.ACCOUNT_DELETED.title());
  }

  @DisplayName("VALIDATION_ERROR는 400과 validation-error type을 가진다.")
  @Test
  void validationErrorMetadata() {
    assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.status());
    assertEquals(
        URI.create("http://localhost:8080/problems/validation-error"),
        ErrorCode.VALIDATION_ERROR.type());
    assertEquals("Request validation failed", ErrorCode.VALIDATION_ERROR.title());
  }
}
