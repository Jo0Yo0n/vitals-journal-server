package io.github.jo0yo0n.vitalsjournal.healthrecord.controller;

import io.github.jo0yo0n.vitalsjournal.auth.util.JwtSubjects;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response.HealthRecordListResponse;
import io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response.HealthRecordResponse;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.HealthRecordService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
