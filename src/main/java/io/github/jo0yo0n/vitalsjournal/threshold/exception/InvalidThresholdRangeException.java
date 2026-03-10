package io.github.jo0yo0n.vitalsjournal.threshold.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class InvalidThresholdRangeException extends BusinessException {
  public InvalidThresholdRangeException() {
    super(ErrorCode.INVALID_THRESHOLD_RANGE);
  }

  public InvalidThresholdRangeException(String message) {
    super(ErrorCode.INVALID_THRESHOLD_RANGE, message);
  }
}
