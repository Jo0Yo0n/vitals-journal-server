package io.github.jo0yo0n.vitalsjournal.auth.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class NicknameAlreadyExistsException extends BusinessException {
  public NicknameAlreadyExistsException() {
    super(ErrorCode.NICKNAME_ALREADY_EXISTS);
  }
}
