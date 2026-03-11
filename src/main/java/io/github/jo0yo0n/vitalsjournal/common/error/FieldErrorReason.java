package io.github.jo0yo0n.vitalsjournal.common.error;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldErrorReason {
  REQUIRED("required"),
  INVALID("invalid");

  private final String value;

  FieldErrorReason(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
