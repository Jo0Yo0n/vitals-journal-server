package io.github.jo0yo0n.vitalsjournal.healthrecord.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class EditWindowExpiredException extends BusinessException {
  public EditWindowExpiredException() {
    super(ErrorCode.EDIT_WINDOW_EXPIRED);
  }
}
