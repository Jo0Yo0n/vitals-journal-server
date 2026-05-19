package io.github.jo0yo0n.vitalsjournal.healthrecord.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.common.error.GlobalExceptionHandler;
import io.github.jo0yo0n.vitalsjournal.common.error.ProblemDetailFactory;
import io.github.jo0yo0n.vitalsjournal.config.ProblemAuthenticationEntryPoint;
import io.github.jo0yo0n.vitalsjournal.config.SecurityConfig;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecordType;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.HealthRecordService;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.command.HealthRecordCreateCommand;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthRecordController.class)
@Import({
  GlobalExceptionHandler.class,
  ProblemDetailFactory.class,
  SecurityConfig.class,
  ProblemAuthenticationEntryPoint.class
})
@SuppressWarnings("null")
class HealthRecordControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private HealthRecordService healthRecordService;
  @MockitoBean private JwtDecoder jwtDecoder;

  @DisplayName("GET /health-records - 현재 사용자의 건강 기록 목록을 items로 반환한다")
  @Test
  void getHealthRecordsSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord heartRate =
        HealthRecord.ofHeartRate(
            user, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, "morning");
    HealthRecord bloodPressure =
        HealthRecord.ofBloodPressure(
            user, Instant.parse("2026-05-10T10:00:00Z"), (short) 120, (short) 80, null);
    ReflectionTestUtils.setField(heartRate, "id", 1L);
    ReflectionTestUtils.setField(bloodPressure, "id", 2L);
    ReflectionTestUtils.setField(heartRate, "createdAt", Instant.parse("2026-05-11T10:01:00Z"));
    ReflectionTestUtils.setField(bloodPressure, "createdAt", Instant.parse("2026-05-10T10:01:00Z"));

    given(healthRecordService.getHealthRecordsByUserId(1L))
        .willReturn(List.of(heartRate, bloodPressure));

    mockMvc
        .perform(get("/health-records").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].id").value(1L))
        .andExpect(jsonPath("$.items[0].userId").doesNotExist())
        .andExpect(jsonPath("$.items[0].type").value("HR"))
        .andExpect(jsonPath("$.items[0].measuredAt").value("2026-05-11T10:00:00Z"))
        .andExpect(jsonPath("$.items[0].bpm").value(72))
        .andExpect(jsonPath("$.items[0].systolic").doesNotExist())
        .andExpect(jsonPath("$.items[0].diastolic").doesNotExist())
        .andExpect(jsonPath("$.items[0].memo").value("morning"))
        .andExpect(jsonPath("$.items[0].createdAt").value("2026-05-11T10:01:00Z"))
        .andExpect(jsonPath("$.items[1].id").value(2L))
        .andExpect(jsonPath("$.items[1].userId").doesNotExist())
        .andExpect(jsonPath("$.items[1].type").value("BP"))
        .andExpect(jsonPath("$.items[1].measuredAt").value("2026-05-10T10:00:00Z"))
        .andExpect(jsonPath("$.items[1].bpm").doesNotExist())
        .andExpect(jsonPath("$.items[1].systolic").value(120))
        .andExpect(jsonPath("$.items[1].diastolic").value(80))
        .andExpect(jsonPath("$.items[1].memo").doesNotExist())
        .andExpect(jsonPath("$.items[1].createdAt").value("2026-05-10T10:01:00Z"));

    then(healthRecordService).should().getHealthRecordsByUserId(1L);
  }

  @DisplayName("GET /health-records - 401 unauthorized")
  @Test
  void getHealthRecordsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/health-records"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Authentication is required"));

    then(healthRecordService).should(never()).getHealthRecordsByUserId(1L);
  }

  @DisplayName("POST /health-records - HR 건강 기록을 저장하고 생성된 기록을 반환한다")
  @Test
  void saveHealthRecordSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(
            user, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, "morning");
    ReflectionTestUtils.setField(healthRecord, "id", 1L);
    ReflectionTestUtils.setField(healthRecord, "createdAt", Instant.parse("2026-05-11T10:01:00Z"));

    given(healthRecordService.saveHealthRecord(eq(1L), any(HealthRecordCreateCommand.class)))
        .willReturn(healthRecord);

    mockMvc
        .perform(
            post("/health-records")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "type": "HR",
                      "measuredAt": "2026-05-11T10:00:00Z",
                      "bpm": 72,
                      "memo": "morning"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.userId").doesNotExist())
        .andExpect(jsonPath("$.type").value("HR"))
        .andExpect(jsonPath("$.measuredAt").value("2026-05-11T10:00:00Z"))
        .andExpect(jsonPath("$.bpm").value(72))
        .andExpect(jsonPath("$.systolic").doesNotExist())
        .andExpect(jsonPath("$.diastolic").doesNotExist())
        .andExpect(jsonPath("$.memo").value("morning"))
        .andExpect(jsonPath("$.createdAt").value("2026-05-11T10:01:00Z"));

    HealthRecordCreateCommand expectedCommand =
        new HealthRecordCreateCommand(
            HealthRecordType.HR,
            Instant.parse("2026-05-11T10:00:00Z"),
            (short) 72,
            null,
            null,
            "morning");

    then(healthRecordService).should().saveHealthRecord(eq(1L), eq(expectedCommand));
  }

  @DisplayName("POST /health-records - 401 unauthorized")
  @Test
  void saveHealthRecordUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/health-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "type": "HR",
                      "measuredAt": "2026-05-11T10:00:00Z",
                      "bpm": 72
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Authentication is required"));

    then(healthRecordService).should(never()).saveHealthRecord(eq(1L), any());
  }

  @DisplayName("POST /health-records - 필수 값이 없으면 400 Bad Request")
  @Test
  void saveHealthRecordValidationFailure() throws Exception {
    mockMvc
        .perform(
            post("/health-records")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "type": "HR",
                      "bpm": 72
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[0].name").value("measuredAt"))
        .andExpect(jsonPath("$.errors[0].reason").value("required"));

    then(healthRecordService).should(never()).saveHealthRecord(eq(1L), any());
  }
}
