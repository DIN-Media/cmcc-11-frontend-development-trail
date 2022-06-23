package com.coremedia.blueprint.training.validation;

import com.coremedia.blueprint.common.services.validation.AbstractValidator;
import com.google.common.base.Predicate;

/**
 * This validator checks, if the given bean implements the interface {@link SupportsCustomValidation} and
 * and {@link SupportsCustomValidation#validate()} returns {@code true}.
 *
 */
public class CustomValidator extends AbstractValidator<SupportsCustomValidation> {

  @Override
  public boolean supports(Class<?> clazz) {
    return SupportsCustomValidation.class.isAssignableFrom(clazz);
  }

  @Override
  protected Predicate<SupportsCustomValidation> createPredicate() {
    return (item) -> item!=null && item.validate();
  }


}
