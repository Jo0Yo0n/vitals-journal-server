package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.repository.HealthRecordRepository;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class HealthRecordServiceTest {

  @Mock private HealthRecordRepository healthRecordRepository;

  @DisplayName("사용자 ID로 건강 기록 목록을 최신 measuredAt 순으로 조회한다")
  @Test
  void getHealthRecordsByUserId() {
    HealthRecordService healthRecordService = new HealthRecordService(healthRecordRepository);
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord record =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, null);

    given(healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(1L)).willReturn(List.of(record));

    List<HealthRecord> result = healthRecordService.getHealthRecordsByUserId(1L);

    assertThat(result).containsExactly(record);
    then(healthRecordRepository).should().findByUserIdOrderByMeasuredAtDesc(1L);
  }
}
