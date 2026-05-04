package io.github.jo0yo0n.vitalsjournal.auth.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class InvalidTokenSubjectException extends BusinessException {

  public InvalidTokenSubjectException() {
    super(ErrorCode.UNAUTHORIZED, "Token subject is missing or malformed");
  }
}
