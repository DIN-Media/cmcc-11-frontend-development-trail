package com.coremedia.livecontext.fragment;


import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class FragmentParametersTest {

  @Test
  public void testRequestUrl() {
    var url = "http://localhost:40081/blueprint/servlet/service/fragment/10851/en-US/params;placement=header;view=test;parameter=abc";
    assertThat(FragmentParametersFactory.create(url))
            .returns("10851", FragmentParameters::getStoreId)
            .returns(Locale.forLanguageTag("en-US"), FragmentParameters::getLocale)
            .returns("header", FragmentParameters::getPlacement)
            .returns("test", FragmentParameters::getView)
            .returns("abc", FragmentParameters::getParameter);
  }

  @Test
  public void testEncoded() {
    var url = "/blueprint/servlet/service/fragment/UKIE/en-GB/params;pageId=;view=asInGrid;categoryId=Outlet;placement=ingrid-content;parameter=%7B%22tags%22:%2241%25_to_50%25%22%7D";
    assertThat(FragmentParametersFactory.create(url))
            .returns(null, FragmentParameters::getExternalRef)
            .returns("{\"tags\":\"41%_to_50%\"}", FragmentParameters::getParameter);
  }
}
