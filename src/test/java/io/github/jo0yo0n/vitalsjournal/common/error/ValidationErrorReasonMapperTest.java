package io.github.jo0yo0n.vitalsjournal.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationErrorReasonMapperTest {

  @DisplayName("NotNull은 required로 매핑된다.")
  @Test
  void mapNotNullToRequired() {

    String reason = ValidationErrorReasonMapper.mapping("NotNull");

    assertEquals("required", reason);
  }

  @DisplayName("알 수 없는 코드는 invalid로 매핑된다.")
  @Test
  void mapUnknownToInvalid() {

    String reason = ValidationErrorReasonMapper.mapping("UnknownCode");

    assertEquals("invalid", reason);
  }

  @DisplayName("null이 입력되면 invalid로 매핑된다.")
  @Test
  void mapNullToInvalid() {

    String reason = ValidationErrorReasonMapper.mapping(null);

    assertEquals("invalid", reason);
  }
}
