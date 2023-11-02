package com.coremedia.blueprint.caas.augmentation.adapter;

import com.coremedia.blueprint.base.caas.model.adapter.PageGridAdapter;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdBuilder;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGrid;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.caas.augmentation.CommerceRefHelper;
import com.coremedia.blueprint.caas.augmentation.CommerceSettingsHelper;
import com.coremedia.blueprint.caas.augmentation.model.CommerceRef;
import com.coremedia.blueprint.caas.augmentation.tree.ExternalBreadcrumbContentTreeRelation;
import com.coremedia.blueprint.caas.augmentation.tree.ExternalBreadcrumbContentTreeRelationFactory;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.coremedia.blueprint.caas.augmentation.CommerceIdUtils.extendBreadcrumb;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.CATEGORY;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;

@DefaultAnnotation(NonNull.class)
public class AugmentationPageGridAdapterFactoryCmsOnly {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());
  private static final String CATALOG = "catalog";

  private final ContentBackedPageGridService contentBackedPageGridService;
  private final SitesService sitesService;
  private final String propertyName;
  private final AugmentationService augmentationService;
  private final ExternalBreadcrumbContentTreeRelationFactory externalBreadcrumbContentTreeRelationFactory;
  private final CommerceSettingsHelper commerceSettingsHelper;

  public AugmentationPageGridAdapterFactoryCmsOnly(String propertyName,
                                                   AugmentationService augmentationService,
                                                   ContentBackedPageGridService contentBackedPageGridService,
                                                   SitesService sitesService,
                                                   ExternalBreadcrumbContentTreeRelationFactory externalBreadcrumbContentTreeRelationFactory,
                                                   CommerceSettingsHelper commerceSettingsHelper) {

    this.propertyName = propertyName;
    this.augmentationService = augmentationService;
    this.contentBackedPageGridService = contentBackedPageGridService;
    this.sitesService = sitesService;
    this.externalBreadcrumbContentTreeRelationFactory = externalBreadcrumbContentTreeRelationFactory;
    this.commerceSettingsHelper = commerceSettingsHelper;
  }

  public PageGridAdapter to(CommerceRef commerceRef, DataFetchingEnvironment dataFetchingEnvironment) {
    ContentBackedPageGrid pageGrid = getPageGrid(commerceRef);
    return new PageGridAdapter(pageGrid, dataFetchingEnvironment);
  }

  private ContentBackedPageGrid getPageGrid(CommerceRef commerceRef) {
    var siteId = commerceRef.getSiteId();
    var site = sitesService.findSite(siteId)
            .orElseThrow(() -> new IllegalArgumentException(format("Unable to find site with ID '%s'.", siteId)));
    var vendor = commerceSettingsHelper.getVendor(site);
    var breadcrumb = commerceRef.getBreadcrumb();
    var extendBreadcrumb = extendBreadcrumb(breadcrumb, vendor, commerceRef);
    var treeRelation = externalBreadcrumbContentTreeRelationFactory.create(extendBreadcrumb);
    var commerceId = CommerceRefHelper.toCommerceId(commerceRef, vendor);
    Content augmentingContent = augmentationService.getContentByExternalId(CommerceIdFormatterHelper.format(commerceId), site);
    boolean virtual = augmentingContent == null;
    Content content = getContent(commerceId, breadcrumb, site, treeRelation);
    return contentBackedPageGridService.getContentBackedPageGrid(content, propertyName, treeRelation, virtual);
  }

  @SuppressWarnings("OverlyComplexMethod")
  private Content getContent(CommerceId id, List<String> breadcrumb, Site site, ExternalBreadcrumbContentTreeRelation treeRelation) {
    var formattedCommerceId = CommerceIdFormatterHelper.format(id);
    Content content = augmentationService.getContentByExternalId(formattedCommerceId, site);
    if (content != null) {
      return content;
    }

    //Raise Exception, if commerce bean type is not supported (e.g. SKU)
    if (!isCommerceBeanTypSupported(id.getCommerceBeanType())) {
      throw new IllegalArgumentException(format("Wrong bean type. PageGrid lookup not supported for %s", id));
    }

    //Category-Fallback for non augmented products
    if (!id.getCommerceBeanType().equals(CATEGORY)) {
      if (!breadcrumb.isEmpty()) {
        String lastCategoryExternalId = breadcrumb.get(breadcrumb.size() - 1);
        id = CommerceIdBuilder.builder(id.getVendor(), CATALOG, CATEGORY)
                .withExternalId(lastCategoryExternalId)
                .withCatalogAlias(id.getCatalogAlias())
                .build();
      }
    }

    content = treeRelation.getNearestContentForCategory(id, site);
    if (content != null) {
      return content;
    }

    //if no parent available, fallback to site root
    content = site.getSiteRootDocument();
    if (content != null) {
      LOG.debug("Falling back to page grid of site root for '{}'.", formattedCommerceId);
      return content;
    }

    throw new IllegalArgumentException("cannot find content for " + id);
  }

  private static boolean isCommerceBeanTypSupported(CommerceBeanType commerceBeanType) {
    return commerceBeanType.equals(PRODUCT) || commerceBeanType.equals(CATEGORY);
  }
}
