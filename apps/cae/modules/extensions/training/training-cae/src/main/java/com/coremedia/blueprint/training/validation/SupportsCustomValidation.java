package com.coremedia.blueprint.training.validation;

/**
 * Marker interface for the CustomValidator
 */
public interface SupportsCustomValidation {

  /**
   * Returns false if the object is invalid in terms of the CustomValidator.
   *
   * <p>If this method returns {@code true}, the object still might be invalid due to other validators.</p>
   *
   * @return false if the object is invalid in terms of custom validation, true otherwise
   */
  boolean validate();

}
