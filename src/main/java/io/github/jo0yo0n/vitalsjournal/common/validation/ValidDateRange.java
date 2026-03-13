package io.github.jo0yo0n.vitalsjournal.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// from 필드의 값이 to 필드의 값보다 이전인지 검증하는 데 사용하는 커스텀 제약 어노테이션
// from과 to 필드의 이름은 기본값으로 "from"과 "to"로 설정되어 있지만, 필요에 따라 어노테이션을 사용할 때 다른 필드 이름으로 지정할 수 있음
// ValidDateRangeValidator에서 from과 to 필드의 값을 가져오기 위해 DateRangeValidatable 인터페이스를 구현해야 함
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
  String message() default "invalid";

  String fromField() default "from";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
