package com.coremedia.livecontext.pagegrid;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGrid;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridPlacement;
import com.coremedia.blueprint.base.pagegrid.impl.ContentBackedPageGridServiceImpl;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * This ContentBackedPageGridService merges content backed pageGrids for augmented products.
 */
public class ContentAugmentedProductPageGridServiceImpl extends ContentBackedPageGridServiceImpl<Content> {

  private static final Logger LOG = LoggerFactory.getLogger(ContentAugmentedProductPageGridServiceImpl.class);

  private static final String CM_EXTERNAL_PRODUCT = "CMExternalProduct";
  private static final String EXTERNAL_ID = "externalId";

  private ContentAugmentedPageGridServiceImpl augmentedCategoryPageGridService;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Nonnull
  @Override
  public ContentBackedPageGrid getContentBackedPageGrid(@Nonnull Content content, @Nonnull String pageGridName) {
    return super.getContentBackedPageGrid(content, pageGridName);
  }

  @Nonnull
  @Override
  protected Map<String, ContentBackedPageGridPlacement> getMergedPageGridPlacements(
          @Nonnull Content content, @Nonnull String pageGridName,
          @Nonnull Collection<? extends Content> layoutSections) {
    if (content.getType().isSubtypeOf(ContentAugmentedPageGridServiceImpl.CM_EXTERNAL_CHANNEL)) {
      return augmentedCategoryPageGridService.getMergedHierarchicalPageGridPlacements(content, pageGridName,
              layoutSections);
    }

    // CMExternalProduct
    Map<String, ContentBackedPageGridPlacement> result = getPlacements(content, pageGridName, layoutSections);

    // parental merge
    Content parentNavigation = getParentOf(content);
    Map<String, ContentBackedPageGridPlacement> parentPlacements = augmentedCategoryPageGridService
            .getMergedHierarchicalPageGridPlacements(parentNavigation, "pdpPagegrid", layoutSections);
    result = merge(result, parentPlacements);

    addMissingPlacementsFromLayout(result, layoutSections);
    return result;
  }

  @Nullable
  @Override
  protected Content getParentOf(@Nullable Content content) {
    if (content == null || !content.getType().isSubtypeOf(CM_EXTERNAL_PRODUCT)) {
      return null;
    }

    return getParentExternalChannelContent(content);
  }

  @Nullable
  @Override
  public Content getLayout(@Nonnull Content content, @Nonnull String pageGridName) {
    Content style = styleSettingsDocument(content, pageGridName);

    if (style == null) {
      Content parentExternalChannelContent = getParentExternalChannelContent(content);
      return augmentedCategoryPageGridService.getLayout(parentExternalChannelContent, pageGridName);
    }

    return style;
  }

  @Nullable
  private Content getParentExternalChannelContent(@Nonnull Content content) {
    Site site = getSitesService().getContentSiteAspect(content).getSite();
    String productId = content.getString(EXTERNAL_ID);

    if (site == null || StringUtils.isEmpty(productId)) {
      return null;
    }

    Optional<CommerceConnection> commerceConnectionOpt = commerceConnectionInitializer.findConnectionForSite(site);

    if (!commerceConnectionOpt.isPresent()) {
      LOG.debug("Commerce connection is not available for site '{}'; not looking up parent content.", site.getName());
      return null;
    }

    CommerceConnection commerceConnection = commerceConnectionOpt.get();

    StoreContext storeContext = commerceConnection.getStoreContext();
    Product product = (Product) commerceConnection.getCommerceBeanFactory().createBeanFor(productId, storeContext);

    if (product == null) {
      return null;
    }

    ExternalChannelContentTreeRelation treeRelation = (ExternalChannelContentTreeRelation) getTreeRelation();
    return treeRelation.getNearestContentForCategory(product.getCategory(), site);
  }

  @Autowired
  @Qualifier("pdpContentBackedPageGridService")
  public void setAugmentedCategoryPageGridService(ContentAugmentedPageGridServiceImpl categoryAugmentedPageGridService) {
    this.augmentedCategoryPageGridService = categoryAugmentedPageGridService;
  }

  @Autowired
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }
}
