package io.github.jo0yo0n.vitalsjournal.threshold.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class ThresholdAlreadyExistsException extends BusinessException {
  public ThresholdAlreadyExistsException() {
    super(ErrorCode.THRESHOLD_ALREADY_EXISTS);
  }
}
