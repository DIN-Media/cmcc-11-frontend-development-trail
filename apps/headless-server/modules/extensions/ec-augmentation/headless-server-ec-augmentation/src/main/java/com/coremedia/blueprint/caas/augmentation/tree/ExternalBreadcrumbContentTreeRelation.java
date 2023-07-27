package com.coremedia.blueprint.caas.augmentation.tree;

import com.coremedia.blueprint.base.livecontext.augmentation.tree.ExternalChannelContentTreeRelation;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;

@DefaultAnnotation(NonNull.class)
public class ExternalBreadcrumbContentTreeRelation implements ExternalChannelContentTreeRelation {
  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  private static final String EXTERNAL_ID = "externalId";
  private static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";

  private final AugmentationService augmentationService;
  private final ExternalBreadcrumbTreeRelation breadcrumbTreeRelation;
  private final SitesService sitesService;

  public ExternalBreadcrumbContentTreeRelation(AugmentationService augmentationService,
                                               ExternalBreadcrumbTreeRelation externalBreadcrumbTreeRelation,
                                               SitesService sitesService) {
    this.augmentationService = augmentationService;
    this.breadcrumbTreeRelation = externalBreadcrumbTreeRelation;
    this.sitesService = sitesService;
  }

  public AugmentationService getAugmentationService() {
    return augmentationService;
  }

  public ExternalBreadcrumbTreeRelation getBreadcrumbTreeRelation() {
    return breadcrumbTreeRelation;
  }

  public SitesService getSitesService() {
    return sitesService;
  }

  @Override
  public Content getParentOf(Content child) {
    if (!isApplicable(child)) {
      return null;
    }

    Site site = sitesService.getContentSiteAspect(child).getSite();
    if (site == null) {
      LOG.warn("Content '{}' has no site, cannot determine parent content.", child.getPath());
      return null;
    }

    return getCommerceIdFrom(child)
            .map(breadcrumbTreeRelation::getParentOf)
            .map(parenCategoryId -> getNearestContentForCategory(parenCategoryId, site))
            .orElseGet(site::getSiteRootDocument);
  }

  @Nullable
  @Override
  public Content getNearestContentForCategory(@Nullable Category category, @Nullable Site site) {
    if (category == null) {
      return null;
    }
    return getNearestContentForCategory(category.getReference(), site);
  }

  @Nullable
  public Content getNearestContentForCategory(@Nullable CommerceId categoryId, @Nullable Site site) {
    if (categoryId == null || site == null) {
      return null;
    }

    Content augmentingContent = augmentationService.getContentByExternalId(CommerceIdFormatterHelper.format(categoryId), site);

    if (null != augmentingContent) {
      return augmentingContent;
    }

    var parentCategoryId = breadcrumbTreeRelation.getParentOf(categoryId);
    return getNearestContentForCategory(parentCategoryId, site);
  }

  @Override
  public boolean isApplicable(Content item) {
    // quite similar to super#isApplicable but without loading of the linked commerce item
    return item != null && item.getType().isSubtypeOf(CM_EXTERNAL_CHANNEL) && isLinkedCategoryValid(item);
  }

  private static boolean isLinkedCategoryValid(@NonNull Content item) {
    return getCommerceIdFrom(item).isPresent();
  }

  @NonNull
  private static Optional<CommerceId> getCommerceIdFrom(@NonNull Content content) {
    String reference = content.getString(EXTERNAL_ID);
    return CommerceIdParserHelper.parseCommerceId(reference);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExternalBreadcrumbContentTreeRelation)) {
      return false;
    }
    ExternalBreadcrumbContentTreeRelation that = (ExternalBreadcrumbContentTreeRelation) o;
    return augmentationService.equals(that.augmentationService) && breadcrumbTreeRelation.equals(that.breadcrumbTreeRelation) && sitesService.equals(that.sitesService);
  }

  @Override
  public int hashCode() {
    return Objects.hash(augmentationService, breadcrumbTreeRelation, sitesService);
  }
}
