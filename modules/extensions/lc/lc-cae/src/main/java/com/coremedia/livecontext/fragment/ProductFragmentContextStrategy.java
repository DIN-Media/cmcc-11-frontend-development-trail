package com.coremedia.livecontext.fragment;

import com.coremedia.livecontext.context.AbstractResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p>
 * A {@link com.coremedia.livecontext.context.ResolveContextStrategy resolve context strategy} that finds a
 * context for a product identified by its
 * {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalTechId() external technical id} or
 * {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalId()} () external id}.
 * Also see {@link ProductFragmentContextStrategy#setUseStableIds}.
 * </p>
 * <p>
 *   Always remember that the <code>external technical id</code> of a product is not stable. If it is possible try
 *   to use the {@link com.coremedia.livecontext.ecommerce.catalog.Product#getExternalId() id} instead.
 * </p>
 */
public class ProductFragmentContextStrategy extends AbstractResolveContextStrategy {
  private boolean useStableIds = false;

  @Nullable
  @Override
  protected Category findNearestCategoryFor(@Nonnull String id, @Nonnull StoreContext storeContext) {
    checkArgument(isNotBlank(id), "You must provide an external id");

    // CMS-3247: Allow product resolution by stable id
    CommerceConnection connection = getCommerceConnection();
    CommerceIdProvider idProvider = connection.getIdProvider();
    String formattedId = useStableIds ? idProvider.formatProductId(id) : idProvider.formatProductTechId(id);

    Product product = getCatalogService().withStoreContext(storeContext).findProductById(formattedId);
    if (product != null) {
      return product.getCategory();
    }

    throw new IllegalStateException("Could not find a product with id " + id);
  }

  /**
   * Set to true, if you want to use the stable {@link com.coremedia.livecontext.ecommerce.catalog.Product#getExternalId() id}
   * to identify a category. Otherwise the volatile {@link com.coremedia.livecontext.ecommerce.catalog.Product#getExternalTechId()} id}
   * is used.
   * @param useStableIds true for stable catalog ids, false for volatile technical ids. default is false.
   */
  public void setUseStableIds(boolean useStableIds) {
    this.useStableIds = useStableIds;
  }

}
