package io.github.jo0yo0n.vitalsjournal.auth.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class AccountDeletedException extends BusinessException {
  public AccountDeletedException() {
    super(ErrorCode.ACCOUNT_DELETED);
  }
}
