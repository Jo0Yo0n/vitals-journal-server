package io.github.jo0yo0n.vitalsjournal.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator
    implements ConstraintValidator<ValidDateRange, DateRangeValidatable> {

  String fromField;

  @Override
  public boolean isValid(DateRangeValidatable value, ConstraintValidatorContext context) {
    if (value == null || value.from() == null || value.to() == null) {
      return true; // @NotNull 등의 다른 제약 조건이 처리하도록 허용
    }

    if (value.from().isBefore(value.to()) || value.from().isEqual(value.to())) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        .addPropertyNode(fromField)
        .addConstraintViolation();

    return false;
  }

  @Override
  public void initialize(ValidDateRange constraintAnnotation) {
    this.fromField = constraintAnnotation.fromField();
  }
}
