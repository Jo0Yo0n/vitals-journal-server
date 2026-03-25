package io.github.jo0yo0n.vitalsjournal.common.error;

// 유효성 검사 오류 코드를 api 명세에 맞는 FieldErrorReason으로 매핑하는 유틸리티 클래스
public class ValidationErrorReasonMapper {
  public static String mapping(String code) {
    if (code == null) {
      return "invalid";
    }

    return switch (code) {
      case "NotNull", "NotEmpty", "NotBlank" -> "required";
      default -> "invalid";
    };
  }
}
