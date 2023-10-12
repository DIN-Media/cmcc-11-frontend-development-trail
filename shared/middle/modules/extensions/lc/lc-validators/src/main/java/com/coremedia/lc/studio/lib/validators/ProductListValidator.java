package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.InvalidCatalogException;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchQuery;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import com.coremedia.rest.cap.validation.AbstractContentTypeValidator;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.coremedia.rest.validation.Severity.INFO;
import static com.coremedia.rest.validation.Severity.WARN;
import static java.lang.invoke.MethodHandles.lookup;

/**
 * Checks the validity of the stored search facet of product lists.
 */
public class ProductListValidator extends AbstractContentTypeValidator {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final SitesService sitesService;
  private final String structPropertyName;
  private final String externalIdPropertyName;

  private static final String CODE_ISSUE_LEGACY_VALUE = "legacy_value";
  private static final String CODE_ISSUE_INVALID_MULTI_FACET = "invalid_multi_facet";
  private static final String CODE_ISSUE_INVALID_MULTI_FACET_QUERY = "invalid_multi_facet_query";
  private static final String CODE_ISSUE_ID_INVALID = "InvalidId";
  private static final String CODE_ISSUE_CONTEXT_INVALID = "InvalidStoreContext";
  private static final String CODE_ISSUE_CATALOG_NOT_FOUND = "CatalogNotFoundError";

  private static final String PROPERTY_PRODUCT_LIST = "productList";
  private static final String PROPERTY_FILTER_FACETS = "filterFacets";
  private static final String PROPERTY_QUERIES_STRING_LIST = "queries";
  private static final String PROPERTY_SELECTED_LEGACY_FACET_NAME = "selectedFacet";
  private static final String PROPERTY_SELECTED_LEGACY_FACET_VALUE = "selectedFacetValue";

  public ProductListValidator(ContentType contentTypeName, CommerceConnectionSupplier commerceConnectionSupplier, SitesService sitesService, String structPropertyName, String externalIdPropertyName) {
    super(contentTypeName, false);
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.sitesService = sitesService;
    this.structPropertyName = structPropertyName;
    this.externalIdPropertyName = externalIdPropertyName;
  }

  @Override
  public void validate(Content content, Issues issues) {
    if (content == null || !content.isInProduction()) {
      return;
    }

    String categoryId = content.getString(externalIdPropertyName);
    if (StringUtils.isEmpty(categoryId)) {
      return;
    }

    //check if there is any data to validate
    Struct localSettings = content.getStruct(structPropertyName);
    if (localSettings == null || !localSettings.toNestedMaps().containsKey(PROPERTY_PRODUCT_LIST)) {
      return;
    }

    //check if the content belongs to a livecontext site
    sitesService.getContentSiteAspect(content).findSite()
            .flatMap(commerceConnectionSupplier::findConnection)
            .ifPresent(connection -> validate(issues, categoryId, localSettings, connection));
  }

  private void validate(Issues issues, String categoryId, Struct localSettings, CommerceConnection connection) {
    CatalogService catalogService = connection.getCatalogService();
    StoreContext storeContext = connection.getInitialStoreContext();
    try {
      CommerceIdParserHelper.parseCommerceId(categoryId)
              .map(id -> catalogService.findCategoryById(id, storeContext))
              .ifPresent(category -> validate(issues, localSettings, catalogService, storeContext, category));
    } catch (InvalidContextException e) {
      issues.addIssue(getCategories(), INFO, null, CODE_ISSUE_CONTEXT_INVALID, categoryId);
    } catch (InvalidIdException e) {
      String storeName = storeContext.getStoreName();
      issues.addIssue(getCategories(), WARN, null, CODE_ISSUE_ID_INVALID, categoryId, storeName);
    } catch (InvalidCatalogException e) {
      CatalogAlias catalogAlias = storeContext.getCatalogAlias();
      issues.addIssue(getCategories(), WARN, null, CODE_ISSUE_CATALOG_NOT_FOUND, catalogAlias.toString(), categoryId);
    } catch (CommerceException e) {
      // do not add an issue for general commerce connectivity errors, just log
      LOG.debug("Catalog could not be accessed: {}", categoryId, e);
    }
  }

  private void validate(Issues issues, Struct localSettings, CatalogService catalogService, StoreContext storeContext, Category category) {
    SearchQuery query = SearchQuery.builder("*", BaseCommerceBeanType.PRODUCT)
            .setCategoryId(category.getId())
            .setLimit(0)
            .setIncludeResultFacets(true)
            .build();
    Struct productListStruct = localSettings.getStruct(PROPERTY_PRODUCT_LIST);

    //if we have multi facet, we check the legacy format and the new multi-facet format
    List<SearchResult.Facet> resultFacets = catalogService.search(query, storeContext).getResultFacets();
    if (resultFacets != null && !resultFacets.isEmpty()) {
      validateLegacyValues(issues, productListStruct);
      validateValuesWithMultiFacets(issues, productListStruct, resultFacets);
    }
  }

  /**
   * Checks if there is still a legacy format stored in the struct and no new struct format has been created.
   * In this case, an error issue is added with a hint of the stored format values.
   *
   * @param issues            the list of issues
   * @param productListStruct the product list content
   */
  private void validateLegacyValues(Issues issues, Struct productListStruct) {
    Map<String, Object> properties = productListStruct.toNestedMaps();
    if (properties.containsKey(PROPERTY_SELECTED_LEGACY_FACET_VALUE) && !properties.containsKey(PROPERTY_FILTER_FACETS)) {
      String facetName = (String) properties.get(PROPERTY_SELECTED_LEGACY_FACET_NAME);
      String facetValue = (String) properties.get(PROPERTY_SELECTED_LEGACY_FACET_VALUE);
      if (!StringUtils.isEmpty(facetValue)) {
        issues.addIssue(getCategories(), Severity.ERROR, structPropertyName + "." + PROPERTY_PRODUCT_LIST + "." + PROPERTY_SELECTED_LEGACY_FACET_VALUE, getContentType() + '_' + CODE_ISSUE_LEGACY_VALUE, facetValue, facetName);
      }
    }
  }

  /**
   * Validates the multi facet struct values against the multi facet API.
   *
   * @param issues            the list of issues
   * @param productListStruct the product list content
   * @param resultFacets      the multi facets to validate against
   */
  private void validateValuesWithMultiFacets(Issues issues, Struct productListStruct, List<SearchResult.Facet> resultFacets) {
    if (!productListStruct.toNestedMaps().containsKey(PROPERTY_FILTER_FACETS)) {
      return;
    }

    Struct filterFacetsStruct = productListStruct.getStruct(PROPERTY_FILTER_FACETS);
    Map<String, Object> properties = filterFacetsStruct.toNestedMaps();
    Set<Map.Entry<String, Object>> entries = properties.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      //we can't have a dot notation stored, see also Facet.as
      String facetId = entry.getKey().replaceAll("\\.", "_");

      //validate if the struct itself has a valid facet id
      Optional<SearchResult.Facet> facetValue = resultFacets.stream().filter(f -> f.getKey().replaceAll("\\.", "_").equals(facetId)).findFirst();
      if (facetValue.isEmpty()) {
        issues.addIssue(getCategories(), Severity.ERROR, structPropertyName + "." + PROPERTY_PRODUCT_LIST, getContentType() + '_' + CODE_ISSUE_INVALID_MULTI_FACET, facetId);
        continue;
      }

      //check if there are queries at all
      Struct filterFacetStruct = filterFacetsStruct.getStruct(facetId);
      if (!filterFacetStruct.toNestedMaps().containsKey(PROPERTY_QUERIES_STRING_LIST)) {
        continue;
      }

      //validate all query values that are stored for the facet
      SearchResult.Facet facet = facetValue.get();
      List<String> queries = filterFacetStruct.getStrings(PROPERTY_QUERIES_STRING_LIST);
      for (String query : queries) {
        Optional<SearchResult.FacetValue> result = facet.getValues().stream()
                .filter(f -> f.getQuery().equals(query))
                .findFirst();
        if (result.isEmpty()) {
          issues.addIssue(getCategories(), Severity.ERROR, structPropertyName + "." + PROPERTY_PRODUCT_LIST + "." + PROPERTY_FILTER_FACETS + "." + facetId, getContentType() + '_' + CODE_ISSUE_INVALID_MULTI_FACET_QUERY, query, facet.getLabel());
        }
      }
    }
  }
}
