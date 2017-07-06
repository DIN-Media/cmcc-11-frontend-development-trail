package com.coremedia.livecontext.pagegrid;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.DefaultConnection;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridPlacement;
import com.coremedia.blueprint.base.pagegrid.impl.ContentBackedPageGridServiceImpl;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * PageGridService merges content backed pageGrids along an external category hierarchy.
 */
public class ContentAugmentedPageGridServiceImpl extends ContentBackedPageGridServiceImpl<Content> {

  private static final Logger LOG = LoggerFactory.getLogger(ContentAugmentedPageGridServiceImpl.class);

  static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";

  private AugmentationService augmentationService;

  @Nonnull
  @Override
  protected Map<String, ContentBackedPageGridPlacement> getMergedPageGridPlacements(
          @Nonnull Content navigation, @Nonnull String pageGridName,
          @Nonnull Collection<? extends Content> layoutSections) {
    return getMergedHierarchicalPageGridPlacements(navigation, pageGridName, layoutSections);
  }

  /**
   * Make #getMergedHierarchicalPageGridPlacements available for
   * {@link ContentAugmentedProductPageGridServiceImpl#getMergedPageGridPlacements}
   */
  @Nonnull
  Map<String, ContentBackedPageGridPlacement> getMergedHierarchicalPageGridPlacements(
          @Nonnull Content navigation, @Nonnull String pageGridName,
          @Nonnull Collection<? extends Content> layoutSections) {
    return super.getMergedPageGridPlacements(navigation, pageGridName, layoutSections);
  }

  @Nullable
  @Override
  protected Content getParentOf(@Nullable Content content) {
    if (content == null || !content.getType().isSubtypeOf(CM_EXTERNAL_CHANNEL)) {
      return null;
    }

    return getTreeRelation().getParentOf(content);
  }

  @Nullable
  @Override
  public Content getLayout(@Nonnull Content content, @Nonnull String pageGridName) {
    Content style = styleSettingsDocument(content, pageGridName);
    if (style == null) {
      Content rootCategoryContent = getRootCategoryContent(content);
      if (rootCategoryContent != null) {
        style = styleSettingsDocument(rootCategoryContent, pageGridName);
      }
    }

    return style != null ? style : getDefaultLayout(content);
  }

  @Nullable
  private Content getRootCategoryContent(@Nullable Content content) {
    try {
      CommerceConnection commerceConnection = requireNonNull(DefaultConnection.get(), "no commerce connection available");
      StoreContext storeContext = commerceConnection.getStoreContextProvider().findContextByContent(content);
      Category rootCategory = commerceConnection.getCatalogService().withStoreContext(storeContext).findRootCategory();
      return augmentationService.getContent(rootCategory);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve root category for Content {}.", content, e);
      return null;
    }
  }

  @Autowired
  @Qualifier("categoryAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }
}
