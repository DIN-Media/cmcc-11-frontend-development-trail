package com.coremedia.lc.studio.lib.interceptor;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.lc.studio.lib.augmentation.ProductAugmentationHelper;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * {@link com.coremedia.rest.cap.intercept.ContentWriteInterceptor}
 */
public class ChangeProductWriteInterceptor extends ChangeCommerceReferenceInterceptor {

  public ChangeProductWriteInterceptor(ContentType contentType,
                                       CommerceConnectionSupplier commerceConnectionSupplier,
                                       AugmentationService augmentationService,
                                       SitesService sitesService,
                                       ContentRepository contentRepository) {
    super(contentType, commerceConnectionSupplier, augmentationService, sitesService, contentRepository);
  }

  @Nullable
  protected CommerceBean getCommerceBeanById(@NonNull CommerceConnection commerceConnection,
                                           @NonNull CommerceId commerceId,
                                           @NonNull Issues issues) {
    var storeContext = commerceConnection.getInitialStoreContext();

    var product = commerceConnection.getCatalogService().findProductById(commerceId, storeContext);
    if (product == null) {
      issues.addIssue(Severity.ERROR, EXTERNAL_ID, "Commerce product could not be found.");
      return null;
    }

    return product;
  }


  @Override
  @NonNull
  protected String computeNewAugmentedCommerceBeanName(@NonNull CommerceBean bean) {
    return ProductAugmentationHelper.computeDocumentName((Product) bean);
  }

  @NonNull
  @Override
  protected String computeNewAugmentedDestinationPath(@NonNull CommerceBean commerceBean, @NonNull Site site) {
    return ProductAugmentationHelper.computeFolderPath(commerceBean, site, ProductAugmentationHelper.DEFAULT_BASE_FOLDER_NAME);
  }
}
