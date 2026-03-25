package io.github.jo0yo0n.vitalsjournal.common.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> fieldErrors,
    String traceId,
    OffsetDateTime timestamp) {
  public static ErrorResponse of(
      ErrorCode errorCode, String message, List<FieldErrorDetail> fieldErrors, String traceId) {
    return new ErrorResponse(
        errorCode.name(),
        message != null ? message : errorCode.getDefaultMessage(),
        fieldErrors != null ? fieldErrors : List.of(),
        traceId,
        OffsetDateTime.now());
  }

  public static ErrorResponse of(ErrorCode errorCode, String traceId) {
    return of(errorCode, errorCode.getDefaultMessage(), List.of(), traceId);
  }
}
