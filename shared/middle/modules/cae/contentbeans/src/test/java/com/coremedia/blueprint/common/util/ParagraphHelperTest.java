package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.util.pagination.PagingRuleType;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.coremedia.xml.MarkupUtil;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class ParagraphHelperTest {
  private Markup markup;

  @Before
  public void setUp() {
    markup = MarkupFactory.fromString("<div xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.coremedia.com/2003/richtext-1.0'><p>I am markup</p><p>Me too</p></div>");
  }

  @Test
  public void testWhiteSpacesBetweenInlineTags() {
    Markup zdMarkup = MarkupFactory.fromString("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\">" +
            "<p><strong>Lorem</strong> <em>Ipsum</em></p>" +
            "</div>");
    List<Markup> markups = ParagraphHelper.createParagraphs(zdMarkup);
    assertThat(markups).anySatisfy((m) -> assertThat(m.asXml()).contains("<strong>Lorem</strong> <em>Ipsum</em>"));
  }

  @Test
  public void testCreateParagraphs1() {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup);
    assertThat(markups).hasSize(2);
    assertThat(MarkupUtil.asPlainText(markups.get(0)).trim()).isEqualTo("I am markup");
    assertThat(MarkupUtil.asPlainText(markups.get(1)).trim()).isEqualTo("Me too");
  }

  @Test
  public void testCreateParagraphs2() {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 2);
    assertThat(markups).hasSize(1);
    assertThat(MarkupUtil.asPlainText(markups.get(0)).trim()).isEqualTo("I am markup\n\nMe too");
  }

  @Test
  public void testCreateParagraphs3() {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 1, "CharactersCountAndNextParagraphRule");
    assertThat(markups).hasSize(2);
    assertThat(MarkupUtil.asPlainText(markups.get(0)).trim()).isEqualTo("I am markup");
    assertThat(MarkupUtil.asPlainText(markups.get(1)).trim()).isEqualTo("Me too");
  }

  @Test
  public void testCreateParagraphs4() {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 1, PagingRuleType.DelimitingBlockCountRule);
    assertThat(markups).hasSize(2);
    assertThat(MarkupUtil.asPlainText(markups.get(0)).trim()).isEqualTo("I am markup");
    assertThat(MarkupUtil.asPlainText(markups.get(1)).trim()).isEqualTo("Me too");
  }

  @Test
  public void testCreateParagraphsWithDelimiter() {
    String openDiv = "<div xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.coremedia.com/2003/richtext-1.0'>";
    Markup markup1 = MarkupFactory.fromString(openDiv + "<p>foo</p><p class=\"p--heading-3\">headline</p><p>bar</p></div>");
    List<Markup> markups = ParagraphHelper.createParagraphs(markup1, 5, PagingRuleType.DelimitingBlockCountRule);
    assertThat(markups).hasSize(2);
    assertThat(MarkupUtil.asPlainText(markups.get(0)).trim()).isEqualTo("foo");
    assertThat(MarkupUtil.asPlainText(markups.get(1)).trim()).isEqualTo("headline\n\nbar");
  }
}
