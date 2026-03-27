package io.github.jo0yo0n.vitalsjournal.common.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ValidDateRangeValidatorTest {

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

  @DisplayName("from이 to보다 이전이면 검증에 성공")
  @Test
  void validWhenFromIsBeforeTo() {
    TestRequest request = new TestRequest(LocalDateTime.now(), LocalDateTime.now().plusDays(1));

    assertTrue(validator.validate(request).isEmpty());
  }

  @DisplayName("from과 to가 같으면 검증에 성공")
  @Test
  void validWhenFromIsEqualToTo() {
    LocalDateTime now = LocalDateTime.now();
    TestRequest request = new TestRequest(now, now);

    assertTrue(validator.validate(request).isEmpty());
  }

  @DisplayName("from이 to보다 이후이면 from 필드에 invalid 메시지로 검증에 실패")
  @Test
  void invalidWhenFromIsAfterTo() {
    TestRequest request = new TestRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now());

    assertTrue(
        validator.validate(request).stream()
            .anyMatch(
                violation ->
                    violation.getPropertyPath().toString().equals("from")
                        && violation.getMessage().equals("invalid")));
  }

  @DisplayName("from 또는 to가 null이면 검증에 성공")
  @Test
  void validWhenFromOrToIsNull() {
    TestRequest request1 = new TestRequest(null, LocalDateTime.now());
    TestRequest request2 = new TestRequest(LocalDateTime.now(), null);

    assertTrue(validator.validate(request1).isEmpty());
    assertTrue(validator.validate(request2).isEmpty());
  }

  @DisplayName("커스텀 필드 이름을 사용하는 경우 해당 필드명으로 에러 매핑")
  @Test
  void invalidWhenUsingCustomFieldName() {
    CustomFieldTestRequest request =
        new CustomFieldTestRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now());

    assertTrue(
        validator.validate(request).stream()
            .anyMatch(
                violation ->
                    violation.getPropertyPath().toString().equals("startedAt")
                        && violation.getMessage().equals("invalid")));
  }

  @ValidDateRange
  private static class TestRequest implements DateRangeValidatable {
    private final LocalDateTime from;
    private final LocalDateTime to;

    private TestRequest(LocalDateTime from, LocalDateTime to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public LocalDateTime from() {
      return from;
    }

    @Override
    public LocalDateTime to() {
      return to;
    }
  }

  @ValidDateRange(fromField = "startedAt")
  private static class CustomFieldTestRequest implements DateRangeValidatable {
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    private CustomFieldTestRequest(LocalDateTime startedAt, LocalDateTime endedAt) {
      this.startedAt = startedAt;
      this.endedAt = endedAt;
    }

    @Override
    public LocalDateTime from() {
      return startedAt;
    }

    @Override
    public LocalDateTime to() {
      return endedAt;
    }
  }
}
