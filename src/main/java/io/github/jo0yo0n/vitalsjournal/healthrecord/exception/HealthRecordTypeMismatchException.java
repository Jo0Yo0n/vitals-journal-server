package io.github.jo0yo0n.vitalsjournal.healthrecord.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class HealthRecordTypeMismatchException extends BusinessException {
  public HealthRecordTypeMismatchException() {

    super(ErrorCode.HEALTH_RECORD_TYPE_MISMATCH);
  }
}
