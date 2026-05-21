package io.github.jo0yo0n.vitalsjournal.alert.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.alert.exception.AlertNotFoundException;
import io.github.jo0yo0n.vitalsjournal.alert.service.AlertService;
import io.github.jo0yo0n.vitalsjournal.common.error.GlobalExceptionHandler;
import io.github.jo0yo0n.vitalsjournal.common.error.ProblemDetailFactory;
import io.github.jo0yo0n.vitalsjournal.config.ProblemAuthenticationEntryPoint;
import io.github.jo0yo0n.vitalsjournal.config.SecurityConfig;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlertController.class)
@Import({
  GlobalExceptionHandler.class,
  ProblemDetailFactory.class,
  SecurityConfig.class,
  ProblemAuthenticationEntryPoint.class
})
@SuppressWarnings("null")
class AlertControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AlertService alertService;
  @MockitoBean private JwtDecoder jwtDecoder;

  @DisplayName("GET /alerts - 현재 사용자의 알림 목록을 items로 반환한다")
  @Test
  void getAlertsSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    Alert alert = Alert.ofRangeViolation(user, healthRecord);
    ReflectionTestUtils.setField(healthRecord, "id", 10L);
    ReflectionTestUtils.setField(alert, "id", 1L);
    ReflectionTestUtils.setField(alert, "createdAt", Instant.parse("2026-05-11T10:01:00Z"));

    given(alertService.getAlerts(1L)).willReturn(List.of(alert));

    mockMvc
        .perform(get("/alerts").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].id").value(1L))
        .andExpect(jsonPath("$.items[0].userId").doesNotExist())
        .andExpect(jsonPath("$.items[0].healthRecordId").value(10L))
        .andExpect(jsonPath("$.items[0].message").value("설정한 건강 기준을 벗어난 기록이 있습니다."))
        .andExpect(jsonPath("$.items[0].readAt").doesNotExist())
        .andExpect(jsonPath("$.items[0].createdAt").value("2026-05-11T10:01:00Z"));

    then(alertService).should().getAlerts(1L);
  }

  @DisplayName("GET /alerts - 알림이 없으면 빈 items를 반환한다")
  @Test
  void getAlertsEmpty() throws Exception {
    given(alertService.getAlerts(1L)).willReturn(List.of());

    mockMvc
        .perform(get("/alerts").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items").isEmpty());

    then(alertService).should().getAlerts(1L);
  }

  @DisplayName("GET /alerts - 401 unauthorized")
  @Test
  void getAlertsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/alerts"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Authentication is required"));

    then(alertService).should(never()).getAlerts(anyLong());
  }

  @DisplayName("PATCH /alerts/{alertId}/read - 알림을 읽음 처리하고 반환한다")
  @Test
  void markAlertAsReadSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    Alert alert = Alert.ofRangeViolation(user, healthRecord);
    alert.markAsRead(Instant.parse("2026-05-11T10:02:00Z"));
    ReflectionTestUtils.setField(healthRecord, "id", 10L);
    ReflectionTestUtils.setField(alert, "id", 1L);
    ReflectionTestUtils.setField(alert, "createdAt", Instant.parse("2026-05-11T10:01:00Z"));

    given(alertService.markAlertAsRead(1L, 1L)).willReturn(alert);

    mockMvc
        .perform(patch("/alerts/1/read").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.userId").doesNotExist())
        .andExpect(jsonPath("$.healthRecordId").value(10L))
        .andExpect(jsonPath("$.message").value("설정한 건강 기준을 벗어난 기록이 있습니다."))
        .andExpect(jsonPath("$.readAt").value("2026-05-11T10:02:00Z"))
        .andExpect(jsonPath("$.createdAt").value("2026-05-11T10:01:00Z"));

    then(alertService).should().markAlertAsRead(1L, 1L);
  }

  @DisplayName("PATCH /alerts/{alertId}/read - 401 unauthorized")
  @Test
  void markAlertAsReadUnauthorized() throws Exception {
    mockMvc
        .perform(patch("/alerts/1/read"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Authentication is required"));

    then(alertService).should(never()).markAlertAsRead(anyLong(), anyLong());
  }

  @DisplayName("PATCH /alerts/{alertId}/read - 404 alert not found")
  @Test
  void markAlertAsReadNotFound() throws Exception {
    given(alertService.markAlertAsRead(123L, 1L))
        .willThrow(new AlertNotFoundException("No alert found for the given id"));

    mockMvc
        .perform(patch("/alerts/123/read").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No alert found for the given id"))
        .andExpect(jsonPath("$.errorCode").value("ALERT_NOT_FOUND"))
        .andExpect(jsonPath("$.instance").value("/alerts/123/read"));

    then(alertService).should().markAlertAsRead(123L, 1L);
  }
}
