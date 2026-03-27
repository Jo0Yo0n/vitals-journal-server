package io.github.jo0yo0n.vitalsjournal.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * from 시점이 to 시점보다 늦지 않은지(from <= to) 검증하는 클래스 레벨 제약이다.
 *
 * <p>제약 위반은 기본적으로 "from" 프로퍼티에 매핑되며, {@link #fromField()}로 변경할 수 있다.
 *
 * <p>검증 대상은 {@link DateRangeValidatable}을 구현해야 하며, 시작/종료 시점은 각각 {@link
 * DateRangeValidatable#from()}와 {@link DateRangeValidatable#to()}를 통해 제공한다.
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
  String message() default "invalid";

  String fromField() default "from";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
