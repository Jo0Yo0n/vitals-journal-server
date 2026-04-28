package io.github.jo0yo0n.vitalsjournal.common.error;

import org.springframework.lang.NonNull;

public class BusinessException extends RuntimeException {

  private final @NonNull ErrorCode errorCode;

  protected BusinessException(@NonNull ErrorCode errorCode) {
    super(errorCode.title());
    this.errorCode = errorCode;
  }

  public @NonNull ErrorCode getErrorCode() {
    return errorCode;
  }
}
