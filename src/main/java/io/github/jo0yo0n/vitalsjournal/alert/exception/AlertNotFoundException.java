package io.github.jo0yo0n.vitalsjournal.alert.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class AlertNotFoundException extends BusinessException {
  public AlertNotFoundException() {
    super(ErrorCode.ALERT_NOT_FOUND);
  }

  public AlertNotFoundException(String message) {
    super(ErrorCode.ALERT_NOT_FOUND, message);
  }
}
