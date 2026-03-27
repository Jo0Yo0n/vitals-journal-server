package io.github.jo0yo0n.vitalsjournal.common.validation;

import java.time.LocalDateTime;

// 날짜 범위 유효성 검증을 위한 인터페이스
// ValidDateRangeValidator에서 from과 to 필드의 값을 가져오기 위해 사용
public interface DateRangeValidatable {
  LocalDateTime from();

  LocalDateTime to();
}
