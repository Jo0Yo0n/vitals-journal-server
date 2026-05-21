package io.github.jo0yo0n.vitalsjournal.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.alert.repository.AlertRepository;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AlertServiceTest {

  @Mock private AlertRepository alertRepository;

  private AlertService alertService;

  @BeforeEach
  void setUp() {
    alertService = new AlertService(alertRepository);
  }

  @DisplayName("사용자 ID로 알림 목록을 조회한다")
  @Test
  void getAlerts() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    Alert alert = Alert.ofRangeViolation(user, healthRecord);

    given(alertRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(alert));

    List<Alert> result = alertService.getAlerts(1L);

    assertThat(result).containsExactly(alert);
    then(alertRepository).should().findByUserIdOrderByCreatedAtDesc(1L);
  }
}
