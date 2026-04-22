package io.github.jo0yo0n.vitalsjournal.common.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Utf8ByteLengthValidatorTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setupValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDownValidator() {
    if (validatorFactory != null) {
      validatorFactory.close();
    }
  }

  @DisplayName("ASCII 72바이트 문자열은 검증에 성공")
  @Test
  void validWhenAsciiLengthIs72Bytes() {
    TestRequest request = new TestRequest("a".repeat(72));

    assertTrue(validator.validate(request).isEmpty());
  }

  @DisplayName("멀티바이트 문자열이 UTF-8 기준 72바이트를 초과하면 검증에 실패")
  @Test
  void invalidWhenUtf8ByteLengthExceeds72() {
    TestRequest request = new TestRequest("가".repeat(25));

    assertTrue(
        validator.validate(request).stream()
            .anyMatch(
                violation ->
                    violation.getPropertyPath().toString().equals("password")
                        && violation.getMessage().equals("invalid")));
  }

  private record TestRequest(@Utf8ByteLength(max = 72) String password) {}
}
