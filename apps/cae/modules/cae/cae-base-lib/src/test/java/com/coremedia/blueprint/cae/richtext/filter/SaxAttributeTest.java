package com.coremedia.blueprint.cae.richtext.filter;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Class under Test: {@link SaxAttribute}.
 */
class SaxAttributeTest {
  @Nested
  class InstantiationUseCases {
    @Test
    void shouldProvideEmptyRepresentationForUnmatchedAttribute() {
      Attributes attributes = new AttributesImpl();
      Optional<SaxAttribute> actual = SaxAttribute.optionalOf(attributes, "class");
      assertThat(actual).isEmpty();
    }

    @Test
    void shouldProvideRepresentationForMatchedAttribute() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      Optional<SaxAttribute> actual = SaxAttribute.optionalOf(attributes, "class");
      assertThat(actual)
              .isPresent();
    }
  }

  @Nested
  class ReadAccessUseCases {
    @ParameterizedTest(name = "[{index}] Should provide value of class attribute ''{0}''")
    @ValueSource(strings = {"class1", "class1 class2", "", " untrimmed\t"})
    @DisplayName("getValue()")
    void shouldProvideValueOfAttribute(@NonNull String classValue) {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", classValue);
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      assertThat(actual.getValue()).isEqualTo(classValue);
    }

    @ParameterizedTest(name = "[{index}] Should provide (non-distinct) value stream for probe: {0}")
    @ArgumentsSource(ValueProbeProvider.class)
    @DisplayName("values()")
    void shouldProvideNonDistinctValueStreamForProbe(@NonNull ValueProbe probe) {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", probe.attributeValue);
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      assertThat(actual.values()).containsExactlyInAnyOrderElementsOf(probe.expectedValues);
    }

    @ParameterizedTest(name = "[{index}] Should provide (distinct) values for probe: {0}")
    @ArgumentsSource(ValueProbeProvider.class)
    @DisplayName("getDistinctValues()")
    void shouldProvideDistinctValueStreamForProbe(@NonNull ValueProbe probe) {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", probe.attributeValue);
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      assertThat(actual.getDistinctValues()).containsExactlyInAnyOrderElementsOf(probe.expectedDistinctValues);
    }
  }

  @Nested
  class ReplaceAttributeValueUseCases {
    @Test
    @DisplayName("withOverriddenValue(String)")
    void shouldProvideAttributesWithOverriddenValue() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      Attributes overriddenAttributes = actual.withOverriddenValue("UPDATED");
      SaxAttribute overriddenAttribute = SaxAttribute.optionalOf(overriddenAttributes, "class").orElseThrow();
      assertThat(overriddenAttribute.getValue()).isEqualTo("UPDATED");
    }

    /**
     * This is a design decision. It may not be appropriate for mandatory
     * attributes. In this case, the design needs to be adapted.
     */
    @Test
    void shouldRemoveAttributeOnOverrideIfEmpty() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      Attributes overriddenAttributes = actual.withOverriddenValue("");
      assertThat(overriddenAttributes.getIndex("class"))
              .as("Empty class attribute should be removed on override.")
              .isLessThan(0);
    }

    @Test
    @DisplayName("withOverriddenValues(Iterable<String>)")
    void shouldProvideAttributesWithOverriddenValues() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      Attributes overriddenAttributes = actual.withOverriddenValues(List.of("UPDATED1", "UPDATED2"));
      SaxAttribute overriddenAttribute = SaxAttribute.optionalOf(overriddenAttributes, "class").orElseThrow();
      assertThat(overriddenAttribute.getValue()).isEqualTo("UPDATED1 UPDATED2");
    }

    @Test
    @DisplayName("withRemovedOnMatch(Predicate<String>)")
    void shouldProvideAttributeOverrideWithFilteredValues() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE1 VALUE2");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      Attributes overriddenAttributes = actual.withRemovedOnMatch("VALUE2"::equals);
      SaxAttribute overriddenAttribute = SaxAttribute.optionalOf(overriddenAttributes, "class").orElseThrow();
      assertThat(overriddenAttribute.getValue()).isEqualTo("VALUE1");
    }
  }

  /**
   * Should be able to remove attributes.
   */
  @Nested
  class RemoveAttributeUseCase {
    @Test
    void shouldBeAbleToRemoveAttribute() {
      String attributeValue = Locale.US.toLanguageTag();
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "lang", "lang", "CDATA", attributeValue);
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "lang").orElseThrow();
      Attributes removed = actual.remove();
      assertThat(removed.getIndex("lang"))
              .as("Lang attribute should have been removed.")
              .isLessThan(0);
    }
  }

  /**
   * These are design decisions, which may be subject to change, if we feel
   * that the behavior is not as expected.
   */
  @Nested
  class SnapshotStateUseCases {
    @Test
    void shouldNotUpdateValuesWhenAttributesInstanceUpdates() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      int classAttributeIndex = attributes.getIndex("class");
      attributes.setValue(classAttributeIndex, "UPDATED");
      assertThat(actual.values()).containsExactly("VALUE");
    }

    @Test
    void shouldNotUpdateValuesWhenAttributesInstanceRemovesAttribute() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      int classAttributeIndex = attributes.getIndex("class");
      attributes.removeAttribute(classAttributeIndex);
      assertThat(actual.values()).containsExactly("VALUE");
    }

    /**
     * <p>
     * This tests represents a design decision after discussing several
     * alternatives:
     * </p>
     * <ul>
     * <li>
     * <strong>No clone, fail preemptively:</strong>
     * We could have skipped cloning attributes, but at least do some validity
     * check, if the attributes for the given attribute did not change
     * unexpectedly. This raised code complexity and still was only a rough
     * guess, that something possibly bad happened to the original attributes.
     * </li>
     * <li>
     * <strong>Assume Attributes are immutable:</strong>
     * This would have simplified the code. But if, in any case, attributes
     * get modified concurrently, the effects are hard to predict and hard
     * to debug, like unrelated attributes being removed or set at stored
     * index position.
     * </li>
     * <li>
     * <strong>Clone Attributes (<em>chosen</em>):</strong>
     * We could just clone the attributes which provides safety by any means
     * regarding possible concurrent modification. This has been perceived as
     * the most lightweight approach.
     * </li>
     * </ul>
     * <p>
     * If you feel, the decision was wrong, you most likely have to adapt
     * this test.
     * </p>
     */
    @Test
    void shouldBehaveWellOnConcurrentAttributesModification() {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "class", "class", "CDATA", "VALUE");
      SaxAttribute actual = SaxAttribute.optionalOf(attributes, "class").orElseThrow();
      int classAttributeIndex = attributes.getIndex("class");

      // Provoke failure accessing stored index, if we did not clone the
      // attributes internally.
      attributes.removeAttribute(classAttributeIndex);

      Attributes overriddenAttributes = actual.withOverriddenValue("UPDATED");
      SaxAttribute overriddenAttribute = SaxAttribute.optionalOf(overriddenAttributes, "class").orElseThrow();

      assertThat(overriddenAttribute.values()).containsExactly("UPDATED");
    }
  }

  static final class ValueProbeProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
              ValueProbe.of("", List.of()),
              ValueProbe.of("some-class", List.of("some-class")),
              ValueProbe.of("class-1 class-2", List.of("class-1", "class-2")),
              ValueProbe.of(" untrimmed\t", List.of("untrimmed")),
              ValueProbe.of(" untrimmed-1 \tuntrimmed-2  ", List.of("untrimmed-1", "untrimmed-2")),
              ValueProbe.of("duplicate duplicate", List.of("duplicate", "duplicate")),
              ValueProbe.of("duplicate some-class duplicate", List.of("duplicate", "some-class", "duplicate"))
      );
    }
  }

  private static final class ValueProbe {
    @NonNull
    private final String attributeValue;
    @NonNull
    private final List<String> expectedValues;
    @NonNull
    private final Set<String> expectedDistinctValues;

    @NonNull
    private static Arguments of(@NonNull String value, @NonNull Collection<String> expected) {
      return Arguments.of(new ValueProbe(value, new ArrayList<>(expected)));
    }

    private ValueProbe(@NonNull String attributeValue, @NonNull List<String> expectedValues) {
      this.attributeValue = attributeValue;
      this.expectedValues = expectedValues;
      this.expectedDistinctValues = new HashSet<>(expectedValues);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
              .add("attributeValue", attributeValue)
              .add("expectedValues", expectedValues)
              .add("expectedDistinctValues", expectedDistinctValues)
              .toString();
    }
  }
}
