package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * <p>
 * This filter ensures that the {@code lang} attribute is used to represent
 * parts in CoreMedia Rich Text 1.0 that are marked as using another language.
 * </p>
 * <p>
 * The language of a given element can be specified with either
 * {@code lang} or {@code xml:lang} attribute in CoreMedia Rich Text 1.0.
 * While both are valid, in context of accessibility, the {@code lang} attribute
 * is the preferred choice for screen-reader-support, for example. Similar,
 * also the CSS-{@code :lang}-Pseudo-Selector only works for this attribute.
 * Thus, this filter will ensure, that {@code lang} is used in delivery.
 * </p>
 * <p>
 * <strong>Conflict Resolution Strategy:</strong>
 * If both, {@code lang} and {@code xml:lang} attribute are given, a conflict
 * resolution strategy is applied. This follows the specification as documented
 * by
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/lang#accessibility">lang - HTML: HyperText Markup Language</a>
 * at
 * <a href="https://developer.mozilla.org/">MDN Web Docs</a>:
 * </p>
 * <blockquote>
 * Even if the lang attribute is set, it may not be taken into account, as
 * the xml:lang attribute has priority.
 * </blockquote>
 * <p>
 * Thus, if both exists, it is the {@code xml:lang} attribute, which gets
 * transformed to {@code lang} in the rendered result.
 * </p>
 * <p>
 * <strong>Locale Normalization:</strong>
 * To incorporate accessibility guidelines, locales are also normalized during
 * filtering, such as replacing so-called "grandfathered" language tags. The
 * normalization may be customized. By default, depends on the normalization as
 * done by your JDKs {@link Locale#forLanguageTag(String)}.
 * </p>
 *
 * @since 2310.1
 */
public class LangAttributeFilter extends Filter implements FilterFactory {
  /**
   * The default normalizer for language tag based on
   * {@link Locale#forLanguageTag(String)}.
   */
  public static final UnaryOperator<String> DEFAULT_LOCALE_NORMALIZER = original -> Locale.forLanguageTag(original).toLanguageTag();

  /**
   * Used to normalize locales, e.g., to replace so-called "grandfathered"
   * tags as recommended by accessibility guidelines.
   *
   * @see <a href="https://www.rfc-editor.org/rfc/rfc5646.html#section-2.2.8">RFC 5646: Tags for Identifying Languages</a>
   * @see <a href="https://act-rules.github.io/rules/5b7ae0">Rule | HTML page lang and xml:lang attributes have matching values | ACT-Rules Community</a>
   */
  @NonNull
  private final UnaryOperator<String> localeNormalizer;

  /**
   * Constructor to initialize with default locale normalizer.
   *
   * @see #DEFAULT_LOCALE_NORMALIZER
   */
  public LangAttributeFilter() {
    this(DEFAULT_LOCALE_NORMALIZER);
  }

  /**
   * Constructor with given locale normalizer. The normalizer may be used, for
   * example, to replace meanwhile so-called "grandfathered" tags. If the
   * normalizer provides an empty string as the result, no correspondíng
   * language attribute will be applied to the filtered result.
   *
   * @param localeNormalizer normalizer to use
   */
  public LangAttributeFilter(@NonNull UnaryOperator<String> localeNormalizer) {
    this.localeNormalizer = localeNormalizer;
  }

  /**
   * Copy-constructor to provide an instance per request.
   *
   * @param original original filter
   */
  @VisibleForTesting
  LangAttributeFilter(@NonNull LangAttributeFilter original) {
    localeNormalizer = original.localeNormalizer;
  }

  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    return new LangAttributeFilter(this);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes originalAttributes) throws SAXException {
    AtomicReference<String> langAttributeValue = new AtomicReference<>();
    Consumer<String> langAttributeUpdater = lang -> {
      // Ensures that we only override with relevant language tag.
      if (isNotEmptyOrBlank(lang)) {
        langAttributeValue.set(lang);
      }
    };

    Attributes attributesWithoutLang = SaxAttributes.remove(originalAttributes, "lang", langAttributeUpdater);
    // Order ensures that a valid xml:lang value is preferred.
    Attributes updatedAttributes = SaxAttributes.remove(attributesWithoutLang, "xml:lang", langAttributeUpdater);

    if (isNotEmptyOrBlank(langAttributeValue.get())) {
      String normalizedLanguageTag = normalizeLanguageTag(langAttributeValue.get());
      if (!normalizedLanguageTag.isEmpty()) {
        AttributesImpl impl = requireImpl(updatedAttributes);
        impl.addAttribute(uri, "lang", "lang", "CDATA", normalizedLanguageTag);
        updatedAttributes = impl;
      }
    }

    super.startElement(uri, localName, qName, updatedAttributes);
  }

  /**
   * <p>
   * Normalizes the language tag prior to applying it to the filtered
   * result.
   * </p>
   * <p>
   * This normalization may, for example, adapt grandfathered tags as
   * recommended by accessibility guidelines.
   * </p>
   *
   * @param originalTag original tag to normalize
   * @return normalized language tag; empty string will remove {@code lang}
   * attribute from the result
   * @implNote Defaults to use {@link Locale#forLanguageTag(String)} for the
   * normalization.
   */
  @NonNull
  private String normalizeLanguageTag(@NonNull String originalTag) {
    return localeNormalizer.apply(originalTag);
  }

  /**
   * Method to check, if given attribute value is {@code null} or blank.
   * More sophisticated approaches may also prefer a valid RFC3066 language
   * code over an invalid one. Skipped here, as code would become a lot more
   * complex.
   *
   * @param attrValue attribute value to validate
   * @return {@code true} if the attribute values has some relevant value; {@code false} otherwise
   */
  private boolean isNotEmptyOrBlank(@Nullable String attrValue) {
    return attrValue != null && !attrValue.isBlank();
  }

  /**
   * If attributes already provide {@code *Impl} API, returning as is. Otherwise,
   * returns a mutable new set of attributes.
   *
   * @param attributes attributes to change to implementation, if required
   * @return attribute implementation
   */
  @NonNull
  private AttributesImpl requireImpl(@NonNull Attributes attributes) {
    if (attributes instanceof AttributesImpl) {
      return (AttributesImpl) attributes;
    }
    return new AttributesImpl(attributes);
  }
}
