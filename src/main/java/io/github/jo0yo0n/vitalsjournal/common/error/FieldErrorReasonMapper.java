package io.github.jo0yo0n.vitalsjournal.common.error;

public class FieldErrorReasonMapper {
  public static FieldErrorReason from(String code) {
    if (code == null) {
      return FieldErrorReason.INVALID;
    }

    return switch (code) {
      case "NotNull", "NotEmpty", "NotBlank" -> FieldErrorReason.REQUIRED;
      default -> FieldErrorReason.INVALID;
    };
  }
}
