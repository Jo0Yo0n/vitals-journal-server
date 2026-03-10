package io.github.jo0yo0n.vitalsjournal.healthrecord.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class HealthRecordNotFoundException extends BusinessException {
  public HealthRecordNotFoundException() {
    super(ErrorCode.HEALTH_RECORD_NOT_FOUND);
  }

  public HealthRecordNotFoundException(String message) {
    super(ErrorCode.HEALTH_RECORD_NOT_FOUND, message);
  }
}
