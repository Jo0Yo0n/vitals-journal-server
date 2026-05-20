package io.github.jo0yo0n.vitalsjournal.threshold.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ThresholdUpsertRequest(
    @Min(1) @Max(300) Short minValue, @Min(1) @Max(300) Short maxValue) {}
