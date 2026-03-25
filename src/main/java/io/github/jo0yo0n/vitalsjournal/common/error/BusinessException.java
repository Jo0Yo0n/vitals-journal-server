package io.github.jo0yo0n.vitalsjournal.common.error;

public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  protected BusinessException(ErrorCode errorCode) {
    super(errorCode.title());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
