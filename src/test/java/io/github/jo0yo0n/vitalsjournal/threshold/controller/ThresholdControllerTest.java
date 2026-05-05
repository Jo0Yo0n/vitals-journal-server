package io.github.jo0yo0n.vitalsjournal.threshold.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.common.error.GlobalExceptionHandler;
import io.github.jo0yo0n.vitalsjournal.common.error.ProblemDetailFactory;
import io.github.jo0yo0n.vitalsjournal.config.ProblemAuthenticationEntryPoint;
import io.github.jo0yo0n.vitalsjournal.config.SecurityConfig;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.threshold.service.ThresholdService;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.math.BigDecimal;
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

@WebMvcTest(ThresholdController.class)
@Import({
  GlobalExceptionHandler.class,
  ProblemDetailFactory.class,
  SecurityConfig.class,
  ProblemAuthenticationEntryPoint.class
})
@SuppressWarnings("null")
class ThresholdControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ThresholdService thresholdService;
  @MockitoBean private JwtDecoder jwtDecoder;

  @DisplayName("GET /thresholds - 현재 사용자의 threshold 목록을 items로 반환한다")
  @Test
  void getAllThresholdsSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    Threshold threshold1 =
        Threshold.of(user, ThresholdMetric.HR, new BigDecimal("60"), new BigDecimal("100"));
    Threshold threshold2 = Threshold.of(user, ThresholdMetric.BP_DIA, new BigDecimal("90"), null);
    ReflectionTestUtils.setField(threshold1, "id", 1L);
    ReflectionTestUtils.setField(threshold2, "id", 2L);
    ReflectionTestUtils.setField(threshold1, "updatedAt", Instant.parse("2026-05-05T00:00:00Z"));
    ReflectionTestUtils.setField(threshold2, "updatedAt", Instant.parse("2026-05-06T00:00:00Z"));

    given(thresholdService.getThresholdsByUserId(1L)).willReturn(List.of(threshold1, threshold2));

    mockMvc
        .perform(get("/thresholds").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].id").value(1L))
        .andExpect(jsonPath("$.items[0].metric").value("HR"))
        .andExpect(jsonPath("$.items[0].minValue").value(60))
        .andExpect(jsonPath("$.items[0].maxValue").value(100))
        .andExpect(jsonPath("$.items[0].updatedAt").value("2026-05-05T00:00:00Z"))
        .andExpect(jsonPath("$.items[1].id").value(2L))
        .andExpect(jsonPath("$.items[1].metric").value("BP_DIA"))
        .andExpect(jsonPath("$.items[1].minValue").value(90))
        .andExpect(jsonPath("$.items[1].maxValue").doesNotExist())
        .andExpect(jsonPath("$.items[1].updatedAt").value("2026-05-06T00:00:00Z"));
  }

  @DisplayName("PUT /thresholds/{metric} - 성공")
  @Test
  void upsertThresholdSuccess() throws Exception {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    Threshold threshold =
        Threshold.of(user, ThresholdMetric.HR, new BigDecimal("60"), new BigDecimal("100"));
    ReflectionTestUtils.setField(threshold, "id", 1L);
    ReflectionTestUtils.setField(threshold, "updatedAt", Instant.parse("2026-05-05T00:00:00Z"));

    given(
            thresholdService.upsertThreshold(
                1L, ThresholdMetric.HR, new BigDecimal("60"), new BigDecimal("100")))
        .willReturn(threshold);

    mockMvc
        .perform(
            put("/thresholds/HR")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "minValue": 60,
                      "maxValue": 100
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.metric").value("HR"))
        .andExpect(jsonPath("$.minValue").value(60))
        .andExpect(jsonPath("$.maxValue").value(100))
        .andExpect(jsonPath("$.updatedAt").value("2026-05-05T00:00:00Z"));
  }

  @DisplayName("PUT /thresholds/{metric} - 잘못된 metric이면 400 Bad Request")
  @Test
  void upsertThresholdInvalidMetric() throws Exception {
    mockMvc
        .perform(
            put("/thresholds/INVALID_METRIC")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "minValue": 60,
                      "maxValue": 100
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
  }
}
