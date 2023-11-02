package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.XMLFilter;

import java.util.Locale;

import static com.coremedia.cap.common.XmlGrammar.RICH_TEXT_1_0_NAME;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;

class LangAttributeFilterTest {
  @SuppressWarnings("HttpUrlsUsage")
  private static final String RICH_TEXT_NAMESPACE = "http://www.coremedia.com/2003/richtext-1.0";
  @NonNull
  private final XMLFilter filter = new LangAttributeFilter();

  @Nested
  class MainUseCases {
    @ParameterizedTest
    @EnumSource(LocaleFixture.class)
    void shouldKeepLangAttribute(@NonNull LocaleFixture localeFixture) {
      String languageTag = localeFixture.getMain().toLanguageTag();
      String filtered = applyFilterToInline("Lorem <span lang=\"" + languageTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + languageTag + "\">ipsum</span> dolor");
    }

    @ParameterizedTest
    @EnumSource(LocaleFixture.class)
    void shouldTransformXmlLangToLang(@NonNull LocaleFixture localeFixture) {
      String languageTag = localeFixture.getMain().toLanguageTag();
      String filtered = applyFilterToInline("Lorem <span xml:lang=\"" + languageTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + languageTag + "\">ipsum</span> dolor");
    }

    @Test
    void shouldNotAddLanguageAttributeIfNotExisting() {
      String filtered = applyFilterToInline("Lorem <span>ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span>ipsum</span> dolor");
    }
  }

  @Nested
  class ConflictResolution {
    @ParameterizedTest
    @EnumSource(LocaleFixture.class)
    void shouldPreferXmlLangOverLangAttribute(@NonNull LocaleFixture localeFixture) {
      String mainTag = localeFixture.getMain().toLanguageTag();
      String otherTag = localeFixture.getOther().toLanguageTag();

      String filtered = applyFilterToInline("Lorem <span lang=\"" + otherTag + "\" xml:lang=\"" + mainTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + mainTag + "\">ipsum</span> dolor");
    }

    @ParameterizedTest
    @EnumSource(LocaleFixture.class)
    void shouldPreferNonEmptyValue(@NonNull LocaleFixture localeFixture) {
      String nonEmptyTag = localeFixture.getMain().toLanguageTag();

      String filtered = applyFilterToInline("Lorem <span lang=\"" + nonEmptyTag + "\" xml:lang=\"\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + nonEmptyTag + "\">ipsum</span> dolor");
    }

    @ParameterizedTest
    @EnumSource(LocaleFixture.class)
    void shouldPreferNonBlankValue(@NonNull LocaleFixture localeFixture) {
      String nonEmptyTag = localeFixture.getMain().toLanguageTag();

      String filtered = applyFilterToInline("Lorem <span lang=\"" + nonEmptyTag + "\" xml:lang=\" \t\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + nonEmptyTag + "\">ipsum</span> dolor");
    }
  }

  @Nested
  class RemoveEmptyOrBlank {
    @ParameterizedTest
    @ValueSource(strings = {"", " ", " \t"})
    void shouldRemoveEmptyOrBlankXmlLang(@NonNull String languageTag) {
      String filtered = applyFilterToInline("Lorem <span xml:lang=\"" + languageTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .as("Should remove irrelevant language attribute, but should not try to perform any clean-up of possibly irrelevant elements.")
              .contains("Lorem <span>ipsum</span> dolor");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " \t"})
    void shouldRemoveEmptyOrBlankLang(@NonNull String languageTag) {
      String filtered = applyFilterToInline("Lorem <span lang=\"" + languageTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .as("Should remove irrelevant language attribute, but should not try to perform any clean-up of possibly irrelevant elements.")
              .contains("Lorem <span>ipsum</span> dolor");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " \t"})
    void shouldRemoveEmptyOrBlankLangAndXmlLang(@NonNull String languageTag) {
      String filtered = applyFilterToInline("Lorem <span lang=\"" + languageTag + "\" xml:lang=\"" + languageTag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .as("Should remove irrelevant language attribute, but should not try to perform any clean-up of possibly irrelevant elements.")
              .contains("Lorem <span>ipsum</span> dolor");
    }
  }

  @Nested
  class LocaleNormalization {
    @ParameterizedTest
    @EnumSource(LocaleNormalizationFixture.class)
    void shouldApplySomeDefaultNormalizationToLanguageTags(@NonNull LocaleNormalizationFixture fixture) {
      String deprecatedSubtag = fixture.getDeprecatedSubtag();
      // JDK specific, thus, we don't trust the configured mapping here.
      String preferredSubtag = Locale.forLanguageTag(deprecatedSubtag).toLanguageTag();

      String filtered = applyFilterToInline("Lorem <span lang=\"" + deprecatedSubtag + "\">ipsum</span> dolor", filter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + preferredSubtag + "\">ipsum</span> dolor");
    }

    @ParameterizedTest
    @EnumSource(LocaleNormalizationFixture.class)
    void shouldNormalizeLanguageTagsViaCustomNormalizer(@NonNull LocaleNormalizationFixture fixture) {
      String deprecatedSubtag = fixture.getDeprecatedSubtag();
      String preferredSubtag = fixture.getPreferredSubtag();

      XMLFilter customNormalizationFilter = new LangAttributeFilter(original -> {
        if (deprecatedSubtag.equals(original)) {
          return preferredSubtag;
        }
        return original;
      });

      String filtered = applyFilterToInline("Lorem <span lang=\"" + deprecatedSubtag + "\">ipsum</span> dolor", customNormalizationFilter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + preferredSubtag + "\">ipsum</span> dolor");
    }

    @ParameterizedTest
    @EnumSource(LocaleNormalizationFixture.class)
    void shouldRespectCustomNormalizerInCopyConstructor(@NonNull LocaleNormalizationFixture fixture) {
      String deprecatedSubtag = fixture.getDeprecatedSubtag();
      String preferredSubtag = fixture.getPreferredSubtag();

      LangAttributeFilter customNormalizationFilter = new LangAttributeFilter(original -> {
        if (deprecatedSubtag.equals(original)) {
          return preferredSubtag;
        }
        return original;
      });
      XMLFilter clonedFilter = new LangAttributeFilter(customNormalizationFilter);

      String filtered = applyFilterToInline("Lorem <span lang=\"" + deprecatedSubtag + "\">ipsum</span> dolor", clonedFilter);
      assertThat(filtered)
              .contains("Lorem <span lang=\"" + preferredSubtag + "\">ipsum</span> dolor");
    }
  }

  @NonNull
  private static String applyFilterToInline(@NonNull String inlineText, @NonNull XMLFilter filter) {
    return applyFilter(wrapInline(inlineText), filter);
  }

  @NonNull
  private static String applyFilter(@NonNull String markupAsString, @NonNull XMLFilter filter) {
    return asRichText(markupAsString).transform(filter).asXml();
  }

  @NonNull
  private static Markup asRichText(@NonNull String markupAsString) {
    return MarkupFactory.fromString(markupAsString)
            .withGrammar(RICH_TEXT_1_0_NAME);
  }

  @NonNull
  private static String wrapInline(@NonNull String inlineText) {
    if (inlineText.isBlank()) {
      return wrapRichText("<p/>");
    }
    return wrapRichText(format("<p>%s</p>", inlineText));
  }

  @NonNull
  private static String wrapRichText(@NonNull String richTextWithoutDiv) {
    return format("<div xmlns=\"%s\">%s</div>", RICH_TEXT_NAMESPACE, richTextWithoutDiv);
  }

  enum LocaleFixture {
    US(Locale.US, Locale.GERMANY),
    FRANCE(Locale.FRANCE, Locale.CANADA);

    @NonNull
    private final Locale main;
    @NonNull
    private final Locale other;

    LocaleFixture(@NonNull Locale main, @NonNull Locale other) {
      this.main = main;
      this.other = other;
    }

    @NonNull
    public Locale getMain() {
      return main;
    }

    @NonNull
    public Locale getOther() {
      return other;
    }
  }

  /**
   * These fixtures may require adaptations, once the IANA registration changes
   * its preferred value here, and it is incorporated into the JDK.
   */
  enum LocaleNormalizationFixture {
    Indonesian("in", "id"),
    Lojban("art-lojban", "jbo"),
    OxfordEnglishDictionary("en-GB-oed", "en-GB-oxendict");

    @NonNull
    private final String deprecatedSubtag;
    @NonNull
    private final String preferredSubtag;

    LocaleNormalizationFixture(@NonNull String deprecatedSubtag, @NonNull String preferredSubtag) {

      this.deprecatedSubtag = deprecatedSubtag;
      this.preferredSubtag = preferredSubtag;
    }

    @NonNull
    public String getDeprecatedSubtag() {
      return deprecatedSubtag;
    }

    @NonNull
    public String getPreferredSubtag() {
      return preferredSubtag;
    }
  }
}
