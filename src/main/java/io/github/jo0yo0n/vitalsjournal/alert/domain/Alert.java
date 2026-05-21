package io.github.jo0yo0n.vitalsjournal.alert.domain;

import io.github.jo0yo0n.vitalsjournal.common.domain.CreatedTimeEntity;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "alert")
public class Alert extends CreatedTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "health_record_id", nullable = false, unique = true)
  private HealthRecord healthRecord;

  @Column(name = "message", nullable = false, length = 255)
  private String message;

  @Column(name = "read_at")
  private Instant readAt;

  public static Alert ofRangeViolation(User user, HealthRecord healthRecord) {
    Alert alert = new Alert();
    alert.user = user;
    alert.healthRecord = healthRecord;
    alert.message = "설정한 건강 기준을 벗어난 기록이 있습니다.";
    return alert;
  }

  public Long getId() {
    return id;
  }

  public Long getHealthRecordId() {
    return healthRecord.getId();
  }

  public String getMessage() {
    return message;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public void markAsRead(Instant readAt) {
    if (this.readAt == null) {
      this.readAt = readAt;
    }
  }
}
