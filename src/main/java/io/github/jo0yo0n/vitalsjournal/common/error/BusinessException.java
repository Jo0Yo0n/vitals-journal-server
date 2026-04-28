package io.github.jo0yo0n.vitalsjournal.common.error;

import jakarta.annotation.Nonnull;
import org.springframework.lang.NonNull;

public class BusinessException extends RuntimeException {

  private final @NonNull ErrorCode errorCode;

  protected BusinessException(@NonNull ErrorCode errorCode) {
    super(errorCode.title());
    this.errorCode = errorCode;
  }

  protected BusinessException(@NonNull ErrorCode errorCode, @NonNull String detail) {
    super(detail);
    this.errorCode = errorCode;
  }

  public @NonNull ErrorCode getErrorCode() {
    return errorCode;
  }

  public @Nonnull String detail() {
    return getMessage();
  }
}
