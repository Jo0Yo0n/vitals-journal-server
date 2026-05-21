package io.github.jo0yo0n.vitalsjournal.alert.controller;

import io.github.jo0yo0n.vitalsjournal.alert.controller.response.AlertListResponse;
import io.github.jo0yo0n.vitalsjournal.alert.controller.response.AlertResponse;
import io.github.jo0yo0n.vitalsjournal.alert.service.AlertService;
import io.github.jo0yo0n.vitalsjournal.auth.util.JwtSubjects;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @GetMapping
  public AlertListResponse getAlerts(@AuthenticationPrincipal Jwt jwt) {

    Long userId = JwtSubjects.requireUserId(jwt);

    return new AlertListResponse(
        alertService.getAlerts(userId).stream().map(AlertResponse::from).toList());
  }
}
