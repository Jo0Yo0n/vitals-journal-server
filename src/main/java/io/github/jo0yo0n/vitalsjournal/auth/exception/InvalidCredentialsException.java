package io.github.jo0yo0n.vitalsjournal.auth.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class InvalidCredentialsException extends BusinessException {

  private static final String DETAIL = "Email or password is invalid";

  public InvalidCredentialsException() {
    super(ErrorCode.INVALID_CREDENTIALS, DETAIL);
  }
}
