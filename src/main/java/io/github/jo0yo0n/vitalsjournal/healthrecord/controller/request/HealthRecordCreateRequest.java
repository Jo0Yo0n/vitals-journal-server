package io.github.jo0yo0n.vitalsjournal.healthrecord.controller.request;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecordType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record HealthRecordCreateRequest(
    @NotNull HealthRecordType type,
    @NotNull Instant measuredAt,
    @Min(1) @Max(300) Short bpm,
    @Min(50) @Max(300) Short systolic,
    @Min(30) @Max(200) Short diastolic,
    @Size(max = 500) String memo) {}
