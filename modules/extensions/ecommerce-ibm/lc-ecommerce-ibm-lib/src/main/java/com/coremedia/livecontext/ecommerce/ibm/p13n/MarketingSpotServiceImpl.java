package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.CommerceIdHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpotService;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import org.apache.commons.collections.Transformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.util.CommerceServiceHelper.getServiceProxyForStoreContext;
import static java.util.Arrays.asList;

public class MarketingSpotServiceImpl implements MarketingSpotService {

    private CommerceCache commerceCache;
    private CommerceBeanFactory commerceBeanFactory;
    private WcMarketingSpotWrapperService marketingSpotWrapperService;
    private boolean useExternalIdForBeanCreation;

    @Override
    @Nonnull
    public List<MarketingSpot> findMarketingSpots() throws CommerceException {
      // noinspection unchecked
      Map<String, Object> wcMarketingSpot = (Map<String, Object>) commerceCache.get(
                new MarketingSpotsCacheKey(StoreContextHelper.getCurrentContext(), UserContextHelper.getCurrentContext(),
                        marketingSpotWrapperService, commerceCache));
        return createMarketingSpotBeansFor(wcMarketingSpot);
    }

    @Nullable
    @Override
    public MarketingSpot findMarketingSpotById(@Nonnull String id) throws CommerceException {
        String externalId = CommerceIdHelper.parseExternalIdFromId(id);
        return findMarketingSpotByExternalId(externalId);
    }

    @Nullable
    public MarketingSpot findMarketingSpotByExternalTechId(@Nonnull String externalTechId) throws CommerceException {
      // noinspection unchecked
      Map<String, Object> wcMarketingSpot = (Map<String, Object>) commerceCache.get(
                new MarketingSpotCacheKey(CommerceIdHelper.formatMarketingSpotTechId(externalTechId),
                        StoreContextHelper.getCurrentContext(), UserContextHelper.getCurrentContext(),
                        marketingSpotWrapperService, commerceCache));
        return createMarketingSpotBeanFor(wcMarketingSpot, false);
    }

    @Override
    @Nullable
    public MarketingSpot findMarketingSpotByExternalId(@Nonnull final String externalId) throws CommerceException {
      // noinspection unchecked
        Map<String, Object> wcMarketingSpot = (Map<String, Object>) commerceCache.get(
                new MarketingSpotCacheKey(CommerceIdHelper.formatMarketingSpotId(externalId),
                        StoreContextHelper.getCurrentContext(), UserContextHelper.getCurrentContext(), marketingSpotWrapperService, commerceCache));
        return createMarketingSpotBeanFor(wcMarketingSpot, false);
    }

  @Override
    @Nonnull
    public SearchResult<MarketingSpot> searchMarketingSpots(@Nonnull final String searchTerm,
                                                            @Nullable Map<String, String> searchParams) throws CommerceException {
        Map<String, Object> wcMarketingSpots = marketingSpotWrapperService.searchMarketingSpots(searchTerm, searchParams,
                StoreContextHelper.getCurrentContext(), UserContextHelper.getCurrentContext());
        List<MarketingSpot> spots = createMarketingSpotBeansFor(wcMarketingSpots);
        SearchResult<MarketingSpot> result = new SearchResult<>();
        result.setSearchResult(spots);
        result.setTotalCount(spots.size());
        result.setPageNumber(1);
        result.setPageSize(1);
        return result;
    }

  @Nullable
  protected MarketingSpot createMarketingSpotBeanFor(@Nullable Map<String, Object> marketingSpotWrapper, boolean reloadById) {
    if (marketingSpotWrapper != null) {
      // results may come from different REST handlers identified by resourceName field ('spot' or 'espot')
      // must be distinguished when retrieving data
      String id = useExternalIdForBeanCreation ?
              CommerceIdHelper.formatMarketingSpotId(DataMapHelper.getValueForKey(marketingSpotWrapper,
                      isESpotResult(marketingSpotWrapper) ? "MarketingSpotData[0].eSpotName" : "MarketingSpot[0].spotName", String.class)) :
              CommerceIdHelper.formatMarketingSpotTechId(DataMapHelper.getValueForKey(marketingSpotWrapper,
                      isESpotResult(marketingSpotWrapper) ? "MarketingSpotData[0].marketingSpotIdentifier" : "MarketingSpot[0].spotId", String.class));

      StoreContext currentContext = StoreContextHelper.getCurrentContext();
      if (CommerceIdHelper.isMarketingSpotId(id)) {
        final MarketingSpotImpl spot = (MarketingSpotImpl) commerceBeanFactory.createBeanFor(id, currentContext);
        Transformer transformer = null;
        if (reloadById) {
          transformer = new Transformer() {

            private Map<String, Object> delegateFromCache;

            @Override
            public Object transform(Object input) {
              if (null == delegateFromCache) {
                delegateFromCache = spot.getDelegateFromCache();
              }
              //noinspection SuspiciousMethodCalls
              return delegateFromCache.get(input);
            }
          };
        }
        spot.setDelegate(CatalogServiceImpl.asLazyMap(marketingSpotWrapper, transformer));
        return spot;
      }
    }
    return null;
  }

  protected List<MarketingSpot> createMarketingSpotBeansFor(Map<String, Object> marketingSpotWrappers) {
    if (marketingSpotWrappers == null || marketingSpotWrappers.isEmpty()) {
      return Collections.emptyList();
    }
    List<MarketingSpot> result = new ArrayList<>();
    List<Map<String, Object>> marketingSpotWrapperList = getInnerElements(marketingSpotWrappers);
    if(marketingSpotWrapperList != null) {
      for (Map<String, Object> wrapper : marketingSpotWrapperList) {
        Map<String, Object> outerWrapper = new HashMap<>();
        // these properties are required by the model bean in order to distinguish which REST handler provided
        // the data and which keys to use for reading
        outerWrapper.put("resourceId", marketingSpotWrappers.get("resourceId"));
        outerWrapper.put("resourceName", marketingSpotWrappers.get("resourceName"));
        outerWrapper.put(isESpotResult(outerWrapper) ? "MarketingSpotData" : "MarketingSpot", asList(wrapper));
        result.add(createMarketingSpotBeanFor(outerWrapper, true));
      }
    }
    return Collections.unmodifiableList(result);
  }

  private boolean isESpotResult(Map<String, Object> marketingSpotWrappers) {
    return "espot".equals(DataMapHelper.getValueForKey(marketingSpotWrappers, "resourceName", String.class));
  }

  private List<Map<String, Object>> getInnerElements(Map<String, Object> wcMarketingSpot) {
    // results may come from different REST handlers identified by resourceName field ('spot' or 'espot')
    // must be distinguished when retrieving data
    // noinspection unchecked
    return DataMapHelper.getValueForPath(wcMarketingSpot, isESpotResult(wcMarketingSpot) ? "MarketingSpotData" : "MarketingSpot", List.class);
  }

  public CommerceCache getCommerceCache() {
        return commerceCache;
    }

    public void setCommerceCache(CommerceCache commerceCache) {
        this.commerceCache = commerceCache;
    }

    public CommerceBeanFactory getCommerceBeanFactory() {
        return commerceBeanFactory;
    }

    public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
        this.commerceBeanFactory = commerceBeanFactory;
    }

    public WcMarketingSpotWrapperService getMarketingSpotWrapperService() {
        return marketingSpotWrapperService;
    }

    public void setMarketingSpotWrapperService(WcMarketingSpotWrapperService marketingSpotWrapperService) {
        this.marketingSpotWrapperService = marketingSpotWrapperService;
    }

    @SuppressWarnings("unused")
    public boolean isUseExternalIdForBeanCreation() {
        return useExternalIdForBeanCreation;
    }

    @SuppressWarnings("unused")
    public void setUseExternalIdForBeanCreation(boolean useExternalIdForBeanCreation) {
        this.useExternalIdForBeanCreation = useExternalIdForBeanCreation;
    }

  @Nonnull
  @Override
  public MarketingSpotService withStoreContext(StoreContext storeContext) {
    return getServiceProxyForStoreContext(storeContext, this, MarketingSpotService.class);
  }
}
