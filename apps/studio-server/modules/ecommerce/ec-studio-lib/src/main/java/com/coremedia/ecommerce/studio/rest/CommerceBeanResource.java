package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.content.ContentRepositoryResource;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * An abstract catalog object as a RESTful resource.
 */
public abstract class CommerceBeanResource<Entity extends CommerceBean> extends AbstractCatalogResource<Entity> {

  private static final String ID_AND_SITE_PARAM = "{0}&site={1}";

  private AugmentationService augmentationService;
  private SitesService sitesService;

  @Autowired
  private ContentRepositoryResource contentRepositoryResource;

  public CommerceBeanResource(CatalogAliasTranslationService catalogAliasTranslationService) {
    super(catalogAliasTranslationService);
  }

  @GetMapping
  public AbstractCatalogRepresentation get(@PathVariable(PATH_SITE_ID) final String siteId,
                                           @PathVariable(PATH_CATALOG_ALIAS) final String catalogAlias,
                                           @PathVariable(PATH_ID) final String id) {
    return getRepresentation(Map.of(
            PATH_ID, id,
            PATH_CATALOG_ALIAS, catalogAlias,
            PATH_SITE_ID, siteId));
  }

  protected void fillRepresentation(@NonNull Map<String, String> params,
                                    @NonNull CommerceBean commerceBean,
                                    @NonNull CommerceBeanRepresentation representation) {
    representation.setId(CommerceIdFormatterHelper.format(commerceBean.getId()));
    representation.setExternalId(commerceBean.getExternalId());
    representation.setExternalTechId(commerceBean.getExternalTechId());
    representation.setCustomAttributes(commerceBean.getCustomAttributes());

    // set preview url
    representation.setPreviewUrl(computePreviewUrl(params));

    //multi preview support
    representation.setPreviews(new CommerceBeanPreviews(commerceBean));

    setVisuals(representation, commerceBean);
  }

  @NonNull
  String computePreviewUrl(@NonNull Map<String, String> params) {
    String previewControllerUriPattern = getContentRepositoryResource().getPreviewControllerUrlPattern();
    String encodedEntityId = URLEncoder.encode(CommerceIdFormatterHelper.format(getEntity(params).getId()));

    return formatPreviewUrl(previewControllerUriPattern, encodedEntityId, params.get(PATH_SITE_ID));
  }

  @NonNull
  public static String formatPreviewUrl(@NonNull String previewControllerUriPattern, String id, String siteId) {
    // position 0 is reserved for formatted IDs, position 1 is reserved for numeric content IDs
    // the site param is appended to the formatted ID
    String idAndSiteParam = format(ID_AND_SITE_PARAM, id, siteId);
    return format(previewControllerUriPattern, idAndSiteParam);
  }

  public ContentRepositoryResource getContentRepositoryResource() {
    return contentRepositoryResource;
  }

  protected void setVisuals(CommerceBeanRepresentation representation, CommerceBean entity) {
    // get visuals directly via AssetService to avoid fallback to default picture
    entity.getContext()
            .getConnection()
            .getAssetService()
            .map(assetService -> assetService.findVisuals(entity.getReference(), false, entity.getContext().getSiteId()))
            .ifPresent(representation::setVisuals);
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Entity entity) {
    StoreContext context = entity.getContext();
    Map<String, String> params = new HashMap<>();
    params.put(PATH_ID, entity.getExternalId());
    params.put(PATH_CATALOG_ALIAS, entity.getId().getCatalogAlias().value());
    params.put(PATH_SITE_ID, context.getSiteId());
    return params;
  }

  /**
   * @return the augmenting content which links to this commerce resource
   */
  @Nullable
  protected Content getContent(Entity entity) {
    if (augmentationService == null) {
      return null;
    }

    String siteId = entity.getContext().getSiteId();

    Site site = sitesService.getSite(siteId);
    if (site == null) {
      return null;
    }

    return augmentationService.getContent(entity);
  }

  /**
   * Set augmentation service in case the commerce bean can be augmented.
   *
   * @param augmentationService the augmentation service matching the concrete resource type
   */
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }
}
