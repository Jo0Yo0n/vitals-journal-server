package io.github.jo0yo0n.vitalsjournal.threshold.controller.request;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record ThresholdUpsertRequest(
    @Digits(integer = 8, fraction = 2) BigDecimal minValue,
    @Digits(integer = 8, fraction = 2) BigDecimal maxValue) {}
