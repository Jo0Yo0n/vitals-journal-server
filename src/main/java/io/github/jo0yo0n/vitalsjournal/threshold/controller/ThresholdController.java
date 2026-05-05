package io.github.jo0yo0n.vitalsjournal.threshold.controller;

import io.github.jo0yo0n.vitalsjournal.auth.util.JwtSubjects;
import io.github.jo0yo0n.vitalsjournal.threshold.controller.request.ThresholdUpsertRequest;
import io.github.jo0yo0n.vitalsjournal.threshold.controller.response.ThresholdListResponse;
import io.github.jo0yo0n.vitalsjournal.threshold.controller.response.ThresholdResponse;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.threshold.service.ThresholdService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thresholds")
public class ThresholdController {

  private final ThresholdService thresholdService;

  public ThresholdController(ThresholdService thresholdService) {
    this.thresholdService = thresholdService;
  }

  @GetMapping()
  public ThresholdListResponse getAllThresholds(@AuthenticationPrincipal Jwt jwt) {

    Long userId = JwtSubjects.requireUserId(jwt);

    List<ThresholdResponse> items =
        thresholdService.getThresholdsByUserId(userId).stream()
            .map(ThresholdResponse::from)
            .toList();

    return new ThresholdListResponse(items);
  }

  @PutMapping("/{metric}")
  public ThresholdResponse upsertThreshold(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable ThresholdMetric metric,
      @Valid @RequestBody ThresholdUpsertRequest request) {

    Long userId = JwtSubjects.requireUserId(jwt);

    Threshold threshold =
        thresholdService.upsertThreshold(userId, metric, request.minValue(), request.maxValue());

    return ThresholdResponse.from(threshold);
  }
}
