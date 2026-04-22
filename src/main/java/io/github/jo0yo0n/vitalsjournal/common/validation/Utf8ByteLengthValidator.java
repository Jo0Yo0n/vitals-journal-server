package io.github.jo0yo0n.vitalsjournal.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Utf8ByteLengthValidator implements ConstraintValidator<Utf8ByteLength, CharSequence> {

  private int min;
  private int max;

  @Override
  public void initialize(Utf8ByteLength constraintAnnotation) {
    this.min = constraintAnnotation.min();
    this.max = constraintAnnotation.max();
  }

  @Override
  public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    int byteCount = 0;
    for (int i = 0; i < value.length(); ) {
      int codePoint = Character.codePointAt(value, i);
      if (codePoint < 0x80) {
        byteCount += 1;
      } else if (codePoint < 0x800) {
        byteCount += 2;
      } else if (codePoint < 0x10000) {
        byteCount += 3;
      } else {
        byteCount += 4;
      }

      if (byteCount > max) {
        return false;
      }

      i += Character.charCount(codePoint);
    }

    return byteCount >= min;
  }
}
