package com.coremedia.blueprint.cae.richtext.filter;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.coremedia.blueprint.cae.richtext.filter.SaxAttributes.WHITESPACE_DELIM_PATTERN;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Represents a given SAX attribute.
 *
 * @since 2210.1
 */
final class SaxAttribute {
  private static final String MSG_INDEX_OUT_OF_RANGE = "Index out of range.";
  @NonNull
  private final Attributes attributes;
  private final int attributeIndex;
  @NonNull
  private final String uri;
  @NonNull
  private final String localName;
  @NonNull
  private final String qName;
  @NonNull
  private final String type;
  @NonNull
  private final String value;

  private SaxAttribute(@NonNull Attributes attributes,
                       int attributeIndex) {
    // Premature optimization? We clone the attributes here. While this may
    // be premature optimization, concurrent modifications, if they happen in
    // any way, may produce unexpected and hard to debug states.
    this.attributes = new AttributesImpl(attributes);
    this.attributeIndex = attributeIndex;

    // Any failure below would signal, that the index is invalid.
    this.uri = requireNonNull(attributes.getURI(attributeIndex), MSG_INDEX_OUT_OF_RANGE);
    this.localName = requireNonNull(attributes.getLocalName(attributeIndex), MSG_INDEX_OUT_OF_RANGE);
    this.qName = requireNonNull(attributes.getQName(attributeIndex), MSG_INDEX_OUT_OF_RANGE);
    this.type = requireNonNull(attributes.getType(attributeIndex), MSG_INDEX_OUT_OF_RANGE);
    this.value = requireNonNull(attributes.getValue(attributeIndex), MSG_INDEX_OUT_OF_RANGE);
  }

  /**
   * Get value of attribute.
   *
   * @return value
   */
  @NonNull
  String getValue() {
    return value;
  }

  /**
   * Get values of attribute, assuming that values are whitespace-separated.
   *
   * @return stream of values
   */
  @NonNull
  Stream<String> values() {
    return WHITESPACE_DELIM_PATTERN.splitAsStream(value)
            .filter(s -> !s.isEmpty());
  }

  /**
   * Gets distinct values of attribute. Assumption for multi-value is, that
   * values are whitespace-separated.
   *
   * @return list of distinct values
   */
  @NonNull
  List<String> getDistinctValues() {
    return values()
            .distinct()
            .collect(toCollection(ArrayList::new));
  }

  /**
   * <p>
   * Provides a clone of the attributes instance, this attribute belongs to,
   * replacing its value with the given new value.
   * </p>
   * <p>
   * If the value is empty, the attribute will be removed instead.
   * </p>
   *
   * @param newValue clone of attributes with replaced value for attribute
   * @return new attributes instance
   */
  @NonNull
  Attributes withOverriddenValue(@NonNull String newValue) {
    AttributesImpl newAttributes = new AttributesImpl(attributes);
    if (newValue.isEmpty()) {
      newAttributes.removeAttribute(attributeIndex);
    } else {
      newAttributes.setValue(attributeIndex, newValue);
    }
    return newAttributes;
  }

  /**
   * <p>
   * Provides a clone of the attributes instance, this attribute belongs to,
   * by removing the attribute itself.
   * </p>
   *
   * @return new attribute instance
   * @since 2310.1
   */
  @NonNull
  Attributes remove() {
    AttributesImpl newAttributes = new AttributesImpl(attributes);
    newAttributes.removeAttribute(attributeIndex);
    return newAttributes;
  }

  /**
   * Provides a clone of the attributes instance, this attribute belongs to,
   * replacing its value with the given set of values. Values will be
   * joined with space characters between.
   *
   * @param newValues new values to replace original value with
   * @return new attributes instance
   */
  @NonNull
  Attributes withOverriddenValues(@NonNull Iterable<String> newValues) {
    return withOverriddenValue(join(" ", newValues));
  }

  /**
   * Provides a clone of the attributes instance, this attribute belongs to,
   * removing any value matching the given predicate. Value is assumed to
   * be whitespace-separated for representing multiple values.
   *
   * @param removePredicate filter predicate to apply
   * @return new attributes instance
   */
  @NonNull
  Attributes withRemovedOnMatch(Predicate<? super String> removePredicate) {
    // As filter() accepts values on positive result, we need to negate
    // the incoming predicate.
    Predicate<? super String> negated = removePredicate.negate();
    return withOverriddenValues(values().filter(negated).collect(toList()));
  }

  /**
   * <p>
   * Provides an optional representation of a SAX attribute identified by its
   * qualified name. Empty, if there is no such attribute.
   * </p>
   * <p>
   * Note, that the representation is a snapshot of the given attribute and
   * assumes, that provided attributes are not modified.
   * </p>
   *
   * @param attributes     attributes to get attribute from
   * @param attributeQName qualified name of attribute
   * @return representation of attribute; empty, if no attribute of given name
   * exists
   */
  @NonNull
  static Optional<SaxAttribute> optionalOf(@NonNull Attributes attributes, @NonNull String attributeQName) {
    int attributeIndex = attributes.getIndex(attributeQName);
    if (attributeIndex < 0) {
      return Optional.empty();
    }
    return Optional.of(new SaxAttribute(
            attributes,
            attributeIndex
    ));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("attributes", attributes)
            .add("attributeIndex", attributeIndex)
            .add("uri", uri)
            .add("localName", localName)
            .add("qName", qName)
            .add("type", type)
            .add("value", value)
            .toString();
  }
}
