package com.coremedia.livecontext.asset;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * Utility to handle Locale and its String representation.
 */
class LocaleHelper {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  private LocaleHelper() {
  }

  @NonNull
  static Optional<Locale> parseLocaleFromString(@NonNull String localeStr) {
    Locale locale = getLocaleFromString(localeStr);
    return Optional.ofNullable(locale);
  }

  @Nullable
  static Locale getLocaleFromString(String localeStr) {
    if (localeStr == null) {
      return null;
    }

    Locale result = null;

    // try to use commons-lang LocaleUtils first
    if (localeStr.contains("_")) {
      try {
        result = LocaleUtils.toLocale(localeStr);
      } catch (IllegalArgumentException ex) { //NOSONAR - ignore LocaleUtils Exception
        result = null;
      }
    }

    // use Locale#forLanguageTag instead, since LocaleUtils can't handle world tags (e.g. en-001)
    if (result == null) {
      String normalizedLocaleStr = localeStr;
      if (localeStr.contains("_")) {
        normalizedLocaleStr = normalizedLocaleStr.replace('_', '-');
      }
      Locale locale = Locale.forLanguageTag(normalizedLocaleStr);
      if (normalizedLocaleStr.equals(locale.toLanguageTag())) {
        result = locale;
      }
    }

    if (result == null) {
      LOG.warn("Invalid locale: {}", localeStr, new IllegalArgumentException());
    }

    return result;
  }
}
