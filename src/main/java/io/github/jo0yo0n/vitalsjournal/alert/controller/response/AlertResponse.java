package io.github.jo0yo0n.vitalsjournal.alert.controller.response;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import java.time.Instant;

public record AlertResponse(
    Long id, Long healthRecordId, String message, Instant readAt, Instant createdAt) {

  public static AlertResponse from(Alert alert) {
    return new AlertResponse(
        alert.getId(),
        alert.getHealthRecordId(),
        alert.getMessage(),
        alert.getReadAt(),
        alert.getCreatedAt());
  }
}
