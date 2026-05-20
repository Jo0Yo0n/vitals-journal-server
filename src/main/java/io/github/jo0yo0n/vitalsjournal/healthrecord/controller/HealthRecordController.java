package io.github.jo0yo0n.vitalsjournal.healthrecord.controller;

import io.github.jo0yo0n.vitalsjournal.auth.util.JwtSubjects;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.request.HealthRecordCreateRequest;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response.HealthRecordListResponse;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response.HealthRecordResponse;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response.HealthRecordWithViolationsResponse;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.HealthRecordService;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.HealthRecordWithViolationsResult;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.command.HealthRecordCreateCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-records")
public class HealthRecordController {

  private final HealthRecordService healthRecordService;

  public HealthRecordController(HealthRecordService healthRecordService) {
    this.healthRecordService = healthRecordService;
  }

  @GetMapping()
  public HealthRecordListResponse getHealthRecords(@AuthenticationPrincipal Jwt jwt) {

    Long userId = JwtSubjects.requireUserId(jwt);

    List<HealthRecordResponse> items =
        healthRecordService.getHealthRecordsByUserId(userId).stream()
            .map(HealthRecordResponse::from)
            .toList();

    return new HealthRecordListResponse(items);
  }

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public HealthRecordWithViolationsResponse saveHealthRecord(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody HealthRecordCreateRequest request) {

    Long userId = JwtSubjects.requireUserId(jwt);

    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            request.type(),
            request.measuredAt(),
            request.bpm(),
            request.systolic(),
            request.diastolic(),
            request.memo());

    HealthRecordWithViolationsResult result = healthRecordService.saveHealthRecord(userId, command);
    return HealthRecordWithViolationsResponse.from(result.healthRecord(), result.violations());
  }

  @GetMapping("/{healthRecordId}")
  public HealthRecordWithViolationsResponse getHealthRecord(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long healthRecordId) {

    Long userId = JwtSubjects.requireUserId(jwt);

    HealthRecordWithViolationsResult result =
        healthRecordService.getHealthRecord(healthRecordId, userId);
    return HealthRecordWithViolationsResponse.from(result.healthRecord(), result.violations());
  }
}
