package io.github.jo0yo0n.vitalsjournal.auth.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {
  public EmailAlreadyExistsException() {
    super(ErrorCode.EMAIL_ALREADY_EXISTS);
  }
}
