package com.coremedia.livecontext.asset;

import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleHelperTest {

  @Test
  public void testLocaleExpectedToWork() {
    assertThat(LocaleHelper.getLocaleFromString("en-Emoji")).isNotNull();
    assertThat(LocaleHelper.getLocaleFromString("en-001")).isNotNull();
    assertThat(LocaleHelper.getLocaleFromString("en_US")).isNotNull();
    assertThat(LocaleHelper.getLocaleFromString("en-US")).isNotNull();
    assertThat(LocaleHelper.getLocaleFromString("en")).isNotNull();

    Locale localeFromString = LocaleHelper.getLocaleFromString("en-001");
    assertThat(LocaleHelper.getLocaleFromString(localeFromString.toString())).isNotNull();
  }

  @Test
  public void testLocaleExpectedToFail() {
    assertThat(LocaleHelper.getLocaleFromString("en-x-Emoji")).isNull();
    assertThat(LocaleHelper.getLocaleFromString("xyz123")).isNull();
    assertThat(LocaleHelper.getLocaleFromString("xy-z")).isNull();
    assertThat(LocaleHelper.getLocaleFromString("xy_z")).isNull();
    assertThat(LocaleHelper.getLocaleFromString("")).isNull();
    assertThat(LocaleHelper.getLocaleFromString(null)).isNull();
  }
}
