package com.coremedia.blueprint.caas.augmentation.adapter;

import com.coremedia.blueprint.base.caas.model.adapter.PageGridAdapter;
import com.coremedia.blueprint.base.caas.model.adapter.PageGridAdapterFactory;
import com.coremedia.blueprint.base.livecontext.augmentation.tree.ExternalChannelContentTreeRelation;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.caas.augmentation.CommerceConnectionHelper;
import com.coremedia.blueprint.caas.augmentation.CommerceRefHelper;
import com.coremedia.blueprint.caas.augmentation.model.Augmentation;
import com.coremedia.blueprint.caas.augmentation.model.CommerceRef;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * @deprecated use {@link AugmentationPageGridAdapterFactoryCmsOnly} instead
 */
@DefaultAnnotation(NonNull.class)
@Deprecated(since = "2304")
public class AugmentationPageGridAdapterFactory extends PageGridAdapterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());
  public static final String PDP_PAGEGRID_PROPERTY_NAME = "pdpPagegrid";

  private final ExternalChannelContentTreeRelation externalChannelContentTreeRelation;
  private final SitesService sitesService;
  private final String propertyName;
  private final AugmentationService augmentationService;
  private final CommerceConnectionHelper commerceConnectionHelper;

  public AugmentationPageGridAdapterFactory(String propertyName,
                                            AugmentationService augmentationService,
                                            ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
                                            ContentBackedPageGridService contentBackedPageGridService,
                                            SitesService sitesService,
                                            CommerceConnectionHelper commerceConnectionHelper) {
    super(contentBackedPageGridService);
    this.propertyName = propertyName;
    this.augmentationService = augmentationService;
    this.externalChannelContentTreeRelation = externalChannelContentTreeRelation;
    this.sitesService = sitesService;
    this.commerceConnectionHelper = commerceConnectionHelper;
  }

  /**
   *
   * @throws IllegalStateException if the page grid could not be loaded
   */
  @Deprecated(forRemoval = true, since = "2107")
  public PageGridAdapter to(CommerceBean commerceBean, DataFetchingEnvironment dataFetchingEnvironment) {
    return to(getContent(commerceBean), propertyName, dataFetchingEnvironment);
  }

  public PageGridAdapter to(CommerceRef commerceRef, DataFetchingEnvironment dataFetchingEnvironment) {
    return to(getContent(getCommerceBean(commerceRef)), propertyName, dataFetchingEnvironment);
  }

  public PageGridAdapter to(Augmentation augmentation, DataFetchingEnvironment dataFetchingEnvironment) {
    CommerceRef commerceRef = augmentation.getCommerceRef();
    return to(getContent(getCommerceBean(commerceRef)), propertyName, dataFetchingEnvironment);
  }

  private CommerceBean getCommerceBean(CommerceRef commerceRef) {
    return sitesService.findSite(commerceRef.getSiteId())
            .map(commerceConnectionHelper::getCommerceConnection)
            .map(connection -> toCommerceBean(commerceRef, connection))
            .orElse(null);
  }

  private static CommerceBean toCommerceBean(CommerceRef commerceRef, CommerceConnection connection) {
    var commerceId = CommerceRefHelper.toCommerceId(commerceRef, connection.getVendor());
    StoreContext storeContext = connection.getInitialStoreContext();
    return connection.getCommerceBeanFactory().createBeanFor(commerceId, storeContext);
  }

  private static String getSiteId(CommerceBean commerceBean) {
    return commerceBean.getContext().getSiteId();
  }

  private Site getSite(CommerceBean commerceBean) {
    return sitesService.getSite(getSiteId(commerceBean));
  }

  @SuppressWarnings("OverlyComplexMethod")
  private Content getContent(CommerceBean commerceBean) {
    Content content = augmentationService.getContent(commerceBean);
    if (content != null) {
      return content;
    }
    if (commerceBean instanceof ProductVariant) {
      content = augmentationService.getContent(((ProductVariant) commerceBean).getParent());
    }
    if (content != null) {
      return content;
    }
    Category category = commerceBean instanceof Category
            ? (Category) commerceBean
            : commerceBean instanceof Product
            ? ((Product) commerceBean).getCategory()
            : illegalState(commerceBean);

    Site site = getSite(commerceBean);
    content = externalChannelContentTreeRelation.getNearestContentForCategory(category, site);
    if (content != null) {
      return content;
    }

    //if no parent available, fallback to site root
    content = site.getSiteRootDocument();
    if (content != null) {
      LOG.debug("Falling back to page grid of site root for {}.", commerceBean.getId());
      return content;
    }

    return illegalState("cannot find content for " + commerceBean);
  }

  private <T> T illegalState(CommerceBean commerceBean) {
    return illegalState("commerce bean must be instanceof Product or Category, but is " + commerceBean.getClass().getName());
  }

  private <T> T illegalState(String msg) {
    throw new IllegalArgumentException(msg);
  }
}
