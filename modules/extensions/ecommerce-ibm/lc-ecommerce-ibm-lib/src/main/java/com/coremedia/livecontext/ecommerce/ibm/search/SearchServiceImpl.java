package com.coremedia.livecontext.ecommerce.ibm.search;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchService;
import com.coremedia.livecontext.ecommerce.search.SuggestionResult;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coremedia.blueprint.base.livecontext.util.CommerceServiceHelper.getServiceProxyForStoreContext;

/**
 * IBM Commerce Service implementation.
 */
public class SearchServiceImpl implements SearchService {
  private WcSearchWrapperService searchWrapperService;

  @Override
  public List<SuggestionResult> getAutocompleteSuggestions(String term, @Nonnull StoreContext currentContext) {
    List<SuggestionResult> result = Collections.emptyList();
    List<WcSuggestion> wcSuggestions = searchWrapperService.
            getKeywordSuggestionsByTerm(term, currentContext);

    if (wcSuggestions != null && !wcSuggestions.isEmpty()) {
      result = new ArrayList<>();
      for (WcSuggestion wcSuggestion : wcSuggestions) {
        result.add(new SuggestionResult(wcSuggestion.getTerm(), term, wcSuggestion.getFrequency()));
      }
    }
    return result;
  }


  public WcSearchWrapperService getSearchWrapperService() {
    return searchWrapperService;
  }

  @Required
  public void setSearchWrapperService(WcSearchWrapperService searchWrapperService) {
    this.searchWrapperService = searchWrapperService;
  }

  @Nonnull
  @Override
  public SearchService withStoreContext(StoreContext storeContext) {
    return getServiceProxyForStoreContext(storeContext, this, SearchService.class);
  }
}
