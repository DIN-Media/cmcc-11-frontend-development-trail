package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentCommerceConnection;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommercePropertyProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Default implementation for the CommercePropertyProvider, mainly providing formatted not encoded commerce URLs.
 */
public class CommerceUrlPropertyProvider implements CommercePropertyProvider {

  protected static final String PARAM_STORE_ID = "storeId";
  private static final String PARAM_CATALOG_ID = "catalogId";
  protected static final String PARAM_LANG_ID = "langId";
  private static final String PARAM_LANGUAGE = "language";
  private static final String PARAM_SEO_SEGMENT = "seoSegment";
  private static final String PARAM_STORE_NAME = "storeName";
  private static final String PARAM_SEARCH_TERM = "searchTerm";
  public static final String URL_TEMPLATE = "urlTemplate";
  public static final String STORE_CONTEXT = "storeContext";
  public static final String SEO_SEGMENT = "seoSegment";
  public static final String SEARCH_TERM = "searchTerm";
  public static final String IS_STUDIO_PREVIEW = "isStudioPreview";
  public static final String IS_INITIAL_STUDIO_REQUEST = "isInitialStudioRequest";
  protected static final String NEW_PREVIEW_SESSION_VARIABLE = "newPreviewSession";
  private static final String REDIRECT_URL = "redirectUrl";
  private static final String PARAM_CONTRACT_ID_FOR_PREVIEW = "contractId";
  protected static final String QUERY_PARAMS = "queryParams";
  protected static final String PRODUCT_ID = "productId";
  protected static final String CATEGORY_ID = "categoryId";
  public static final String CATALOG_ID = "catalogId";

  private String defaultStoreFrontUrl;
  private String previewStoreFrontUrl;
  private String urlPattern;
  private String shoppingFlowUrlForContractPreview;
  private String productNonSeoUrl;
  private String categoryNonSeoUrl;

  private CatalogAliasTranslationService catalogAliasTranslationService;

  /**
   * The URL template is not mandatory and may be passed with the
   * parameter array of the "provideValue" method.
   */
  public String getUrlPattern() {
    return urlPattern;
  }

  public String getDefaultStoreFrontUrl() {
    return defaultStoreFrontUrl;
  }

  public void setDefaultStoreFrontUrl(String defaultStoreFrontUrl) {
    this.defaultStoreFrontUrl = defaultStoreFrontUrl;
  }

  public String getPreviewStoreFrontUrl() {
    return previewStoreFrontUrl;
  }

  public void setPreviewStoreFrontUrl(String previewStoreFrontUrl) {
    this.previewStoreFrontUrl = previewStoreFrontUrl;
  }

  public void setUrlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  public void setShoppingFlowUrlForContractPreview(String shoppingFlowUrlForContractPreview) {
    this.shoppingFlowUrlForContractPreview = shoppingFlowUrlForContractPreview;
  }

  public void setProductNonSeoUrl(String productNonSeoUrl) {
    this.productNonSeoUrl = productNonSeoUrl;
  }

  public void setCategoryNonSeoUrl(String categoryNonSeoUrl) {
    this.categoryNonSeoUrl = categoryNonSeoUrl;
  }

  /**
   * The method expects at least the store context for the URl formatting.
   * Additional optional values may be passed with the array.
   *
   * @param parameters The parameters that are used to format the URL. The following values may be passed:
   *                   <ol>
   *                   <li>StoreContext (optional)</li>
   *                   <li>URL Template (optional)</li>
   *                   <li>URL Parameters Map (optional)</li>
   *                   <li>SEO Segment (optional)</li>
   *                   <li>Search Term (optional)</li>
   *                   <li>StudioPreview Flag(optional)</li>
   *                   </ol>
   */
  @Nullable
  @Override
  public Object provideValue(@Nonnull Map<String, Object> parameters) {
    String resultUrl = getUrlPattern();
    boolean isStudioPreview = isStudioPreview(parameters);

    if (!parameters.isEmpty()) {
      String urlTemplate = evaluateUrlTemplate(parameters);
      //optional URL template to overwrite the default Spring property
      resultUrl = urlTemplate != null ? urlTemplate : resultUrl;

      if (isNullOrEmpty(resultUrl)) {
        return null;
      }

      StoreContext storeContext = (StoreContext) parameters.get(STORE_CONTEXT);
      //compile shopping flow url, if contract ids for preview are stored in storecontext
      if (isContractPreview(storeContext, isStudioPreview)) {
        String redirectUrl = applyParameters(resultUrl, parameters);
        redirectUrl = redirectUrl.startsWith("/") ? redirectUrl.substring(1) : redirectUrl;
        parameters.put(REDIRECT_URL, redirectUrl);
        resultUrl = applyParameters(shoppingFlowUrlForContractPreview, parameters);
        //add contractIds
        resultUrl = UriComponentsBuilder.fromUriString(resultUrl).queryParam(PARAM_CONTRACT_ID_FOR_PREVIEW, storeContext.getContractIdsForPreview()).build().toUriString();
      } else {
        resultUrl = applyParameters(resultUrl, parameters);
      }
    }

    //TODO maybe better no absolute url if fragmentContext available
    //make url absolute
    resultUrl = makeShopUrlAbsolute(resultUrl, isStudioPreview);
    resultUrl = applyParameters(resultUrl, parameters);

    resultUrl = ensureNewSessionForNewStudioTabs(parameters, resultUrl);

    //append catalogId for non default catalog urls
    if (isNonDefaultCatalog(parameters) && !resultUrl.contains(CATALOG_ID + "=")) {
      resultUrl = UriComponentsBuilder.fromUriString(resultUrl).queryParam(CATALOG_ID, parameters.get(CATALOG_ID)).build().toUriString();
    }

    return UriComponentsBuilder.fromUriString(resultUrl).build();
  }

  private String ensureNewSessionForNewStudioTabs(@Nonnull Map<String, Object> parameters, String resultUrl) {
    boolean isInitialStudioRequest = isInitialStudioRequest(parameters);
    if (isInitialStudioRequest) {
      resultUrl = UriComponentsBuilder.fromUriString(resultUrl).queryParam(NEW_PREVIEW_SESSION_VARIABLE, Boolean.TRUE.toString()).build().toUriString();
    }
    return resultUrl;
  }

  @Nullable
  private String evaluateUrlTemplate(@Nonnull Map<String, Object> parameters) {
    String urlTemplate = (String) parameters.get(URL_TEMPLATE);

    if (urlTemplate != null) {
      return urlTemplate;
    }

    String seoSegment = (String) parameters.get(SEO_SEGMENT);
    Map queryParams = (Map) parameters.get(QUERY_PARAMS);

    if (!isNullOrEmpty(seoSegment) || queryParams == null) {
      return null;
    }

    if (queryParams.containsKey(PRODUCT_ID)) {
      return productNonSeoUrl;
    } else if (queryParams.containsKey(CATEGORY_ID)) {
      return categoryNonSeoUrl;
    }
    return null;
  }

  private String applyParameters(@Nullable String url, @Nonnull Map<String, Object> parameters) {
    Map<String, Object> parametersMap = new HashMap<>();
    StoreContext storeContext = (StoreContext) parameters.get(STORE_CONTEXT);

    //optional seo segment
    parametersMap.put(PARAM_SEO_SEGMENT, parameters.get(SEO_SEGMENT));

    //optional search term
    parametersMap.put(PARAM_SEARCH_TERM, parameters.get(SEARCH_TERM));

    //optional redirect url
    parametersMap.put(REDIRECT_URL, parameters.get(REDIRECT_URL));

    //map info from store context
    if (storeContext != null) {

      //catalogId
      String storeId = storeContext.getStoreId();
      String catalogId = storeContext.getCatalogId();
      if (catalogId == null) {
        CatalogAlias catalogAlias = storeContext.getCatalogAlias();
        String siteId = storeContext.getSiteId();
        Optional<CatalogId> catalogIdForAlias = catalogAliasTranslationService.getCatalogIdForAlias(catalogAlias, siteId);
        catalogId = catalogIdForAlias.map(CatalogId::value).orElse(null);
      }

      //storeName
      String storeName = StoreContextHelper.getStoreNameInLowerCase(storeContext);
      String languageId = null;
      parametersMap.put(PARAM_STORE_NAME, storeName);

      //langId
      //the language ID has to be transformed into the format of the commerce system
      Locale locale = StoreContextHelper.getLocale(storeContext);
      if (getCatalogService() instanceof CatalogServiceImpl) {
        languageId = ((CatalogServiceImpl) getCatalogService()).getLanguageId(locale);
        parametersMap.put(PARAM_LANG_ID, languageId);
      }
      parametersMap.put(PARAM_LANGUAGE, locale.getLanguage());

      if (languageId != null) {
        //The catalog id may be defaulted to the store id if the store is not an e-store.
        if (catalogId == null) {
          catalogId = storeId;
        }

        parametersMap.put(PARAM_STORE_ID, storeId);
        parametersMap.put(PARAM_CATALOG_ID, catalogId);
      }

    }

    Object queryParams = parameters.get(QUERY_PARAMS);
    if (queryParams != null) {
      Map queryParamsMap = (Map) queryParams;
      if (!queryParamsMap.isEmpty()) {
        parametersMap.putAll(queryParamsMap);
      }
    }

    String commerceTokensReplacedUrl = CommercePropertyHelper.replaceTokens(url, storeContext);
    return TokenResolverHelper.replaceTokens(commerceTokensReplacedUrl, parametersMap, false, false);
  }

  private String makeShopUrlAbsolute(@Nonnull String url, boolean isStudioPreview) {
    if (url.startsWith("http") || url.startsWith("//")) {
      return url;
    }

    boolean usePreviewStoreFrontUrl = isStudioPreview && getPreviewStoreFrontUrl() != null;
    String prefix = usePreviewStoreFrontUrl ? getPreviewStoreFrontUrl() : getDefaultStoreFrontUrl();

    //avoid "//" in concatenated urls
    if (!prefix.endsWith("/")) {
      prefix += "/";
    }

    String relativeUrlPart = url;
    if (relativeUrlPart.startsWith("/")) {
      relativeUrlPart = relativeUrlPart.substring(1);
    }

    return prefix + relativeUrlPart;
  }

  private static boolean isStudioPreview(@Nonnull Map<String, Object> parameters) {
    return parameters.containsKey(IS_STUDIO_PREVIEW) && (boolean) parameters.get(IS_STUDIO_PREVIEW);
  }

  private static boolean isInitialStudioRequest(@Nonnull Map<String, Object> parameters) {
    return parameters.containsKey(IS_INITIAL_STUDIO_REQUEST) && (boolean) parameters.get(IS_INITIAL_STUDIO_REQUEST);
  }

  private static boolean isNonDefaultCatalog(@Nonnull Map<String, Object> parameters) {
    return parameters.containsKey(CATALOG_ID) && null != parameters.get(CATALOG_ID);
  }

  private static boolean isContractPreview(@Nullable StoreContext storeContext, boolean isStudioPreview) {
    return storeContext != null && isStudioPreview && storeContext.getContractIdsForPreview() != null;
  }

  @Nullable
  public CatalogService getCatalogService() {
    return CurrentCommerceConnection.find()
            .map(CommerceConnection::getCatalogService)
            .orElse(null);
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }
}
