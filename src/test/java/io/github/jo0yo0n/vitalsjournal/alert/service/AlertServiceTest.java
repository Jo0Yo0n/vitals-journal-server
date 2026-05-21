package io.github.jo0yo0n.vitalsjournal.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.alert.exception.AlertNotFoundException;
import io.github.jo0yo0n.vitalsjournal.alert.repository.AlertRepository;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

  @DisplayName("알림 ID와 사용자 ID로 알림을 읽음 처리한다")
  @Test
  void markAlertAsRead() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    Alert alert = Alert.ofRangeViolation(user, healthRecord);
    Instant readAt = Instant.parse("2026-05-11T10:01:00Z");
    ReflectionTestUtils.setField(alert, "readAt", readAt);

    given(alertRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(alert));

    Alert result = alertService.markAlertAsRead(10L, 1L);

    assertThat(result).isSameAs(alert);
    assertThat(result.getReadAt()).isEqualTo(readAt);
    then(alertRepository).should().markAsReadIfUnread(eq(10L), eq(1L), any(Instant.class));
    then(alertRepository).should().findByIdAndUserId(10L, 1L);
    then(alertRepository).should(never()).save(alert);
  }

  @DisplayName("이미 읽은 알림은 기존 readAt을 유지한다")
  @Test
  void markAlertAsReadKeepsExistingReadAt() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    Alert alert = Alert.ofRangeViolation(user, healthRecord);
    Instant existingReadAt = Instant.parse("2026-05-11T10:01:00Z");
    ReflectionTestUtils.setField(alert, "readAt", existingReadAt);

    given(alertRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(alert));

    Alert result = alertService.markAlertAsRead(10L, 1L);

    assertThat(result).isSameAs(alert);
    assertThat(alert.getReadAt()).isEqualTo(existingReadAt);
    then(alertRepository).should().markAsReadIfUnread(eq(10L), eq(1L), any(Instant.class));
    then(alertRepository).should().findByIdAndUserId(10L, 1L);
    then(alertRepository).should(never()).save(alert);
  }

  @DisplayName("알림이 없으면 예외를 던진다")
  @Test
  void markAlertAsReadNotFound() {
    given(alertRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> alertService.markAlertAsRead(10L, 1L))
        .isInstanceOf(AlertNotFoundException.class);

    then(alertRepository).should().markAsReadIfUnread(eq(10L), eq(1L), any(Instant.class));
    then(alertRepository).should().findByIdAndUserId(10L, 1L);
  }
}
