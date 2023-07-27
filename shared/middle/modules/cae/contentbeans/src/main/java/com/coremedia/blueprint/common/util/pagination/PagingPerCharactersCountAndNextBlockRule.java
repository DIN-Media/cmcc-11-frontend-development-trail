package com.coremedia.blueprint.common.util.pagination;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

class PagingPerCharactersCountAndNextBlockRule implements PagingRule {

  private static final int DEFAULT_MAX_CHARACTERS_PER_PAGE = 2000;
  static final List<String> DEFAULT_PAGE_BLOCK_TAGS = List.of("p", "table", "pre", "blockquote", "ul", "ol");
  private Paginator paginator;
  private List<String> blocktags;
  private int maxCharactersPerPage = DEFAULT_MAX_CHARACTERS_PER_PAGE;

  @Override
  public void setPaginator(Paginator pagingFilter) {
    this.paginator = pagingFilter;
    blocktags = DEFAULT_PAGE_BLOCK_TAGS;
  }

  /**
   * @param blocktags list of block tags separated by a whitespace
   */
  public void setBlockTags(@NonNull String blocktags) {
    this.blocktags = Arrays.asList(blocktags.split(" "));
  }

  @Override
  public void setPagingUnitsNumber(int pagingUnitsNumber) {
    maxCharactersPerPage = pagingUnitsNumber;
  }

  @Override
  public int getPagingUnitsNumber() {
    return maxCharactersPerPage;
  }

  @Override
  public boolean match(String localName) {
    if (paginator == null) {
      throw new IllegalStateException("Must set a paginator before using match");
    }
    return (blocktags.contains(localName)) && (paginator.getCharacterCounter() > maxCharactersPerPage);
  }
}
