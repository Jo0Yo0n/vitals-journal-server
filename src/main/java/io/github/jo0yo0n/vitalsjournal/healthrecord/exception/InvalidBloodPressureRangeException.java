package io.github.jo0yo0n.vitalsjournal.healthrecord.exception;

import io.github.jo0yo0n.vitalsjournal.common.error.BusinessException;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;

public class InvalidBloodPressureRangeException extends BusinessException {

  public InvalidBloodPressureRangeException() {
    super(
        ErrorCode.INVALID_BLOOD_PRESSURE_RANGE,
        "For type BP, systolic must be greater than diastolic");
  }
}
