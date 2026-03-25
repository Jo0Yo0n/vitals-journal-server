package io.github.jo0yo0n.vitalsjournal.threshold.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class ThresholdNotFoundException extends BusinessException {
  public ThresholdNotFoundException() {
    super(ErrorCode.THRESHOLD_NOT_FOUND);
  }

  public ThresholdNotFoundException(String message) {
    super(ErrorCode.THRESHOLD_NOT_FOUND, message);
  }
}
