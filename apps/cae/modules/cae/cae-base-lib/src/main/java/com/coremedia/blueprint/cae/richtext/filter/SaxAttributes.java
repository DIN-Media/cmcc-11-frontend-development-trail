package com.coremedia.blueprint.cae.richtext.filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.xml.sax.Attributes;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Some utilities for handling attributes.
 *
 * @since 2210.1
 */
final class SaxAttributes {
  private static final String ATTR_CLASS = "class";
  private static final String WHITESPACE_DELIM = "\\s+";
  static final Pattern WHITESPACE_DELIM_PATTERN = Pattern.compile(WHITESPACE_DELIM);

  private SaxAttributes() {
  }

  /**
   * Provides a representation of the {@code class} attribute, if available.
   * Empty, if there is no corresponding attribute in given attributes.
   *
   * @param attributes attributes to get attribute from
   * @return representation of attribute; empty if attribute is not available
   */
  @NonNull
  static Optional<SaxAttribute> classAttribute(@NonNull Attributes attributes) {
    return SaxAttribute.optionalOf(attributes, ATTR_CLASS);
  }

  /**
   * If existing, removes the attribute given by its qualified name and invokes
   * the given consumer with its value.
   *
   * @param attributes attributes to possibly remove attribute from
   * @param qName qualified name of attribute
   * @param valueConsumer consumer for original value of attribute, if existing
   * @return new attributes without given attribute
   * @since 2310.1
   */
  @NonNull
  static Attributes remove(@NonNull Attributes attributes, @NonNull String qName, @NonNull Consumer<? super String> valueConsumer) {
    Optional<SaxAttribute> optional = SaxAttribute.optionalOf(attributes, qName);
    if (optional.isEmpty()) {
      return attributes;
    }
    SaxAttribute attribute = optional.get();
    valueConsumer.accept(attribute.getValue());
    return attribute.remove();
  }
}
