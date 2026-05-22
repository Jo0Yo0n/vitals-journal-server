package io.github.jo0yo0n.vitalsjournal.alert.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;
import org.springframework.lang.NonNull;

public class AlertNotFoundException extends BusinessException {
  public AlertNotFoundException() {
    super(ErrorCode.ALERT_NOT_FOUND);
  }

  public AlertNotFoundException(@NonNull String detail) {
    super(ErrorCode.ALERT_NOT_FOUND, detail);
  }
}
