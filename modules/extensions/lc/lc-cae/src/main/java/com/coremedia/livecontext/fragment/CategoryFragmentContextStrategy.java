package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.DefaultConnection;
import com.coremedia.livecontext.context.AbstractResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p>
 * A {@link com.coremedia.livecontext.context.ResolveContextStrategy resolve context strategy} that finds a
 * context for a category identified by its
 * {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalTechId() external technical id} or
 * {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalId()} () external id}.
 * Also see {@link CategoryFragmentContextStrategy#setUseStableIds}.
 * </p>
 * <p>
 *   Always remember that the <code>external technical id</code> of a category is not stable. If it is possible try
 *   to use the {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalId() id} instead.
 * </p>
 */
public class CategoryFragmentContextStrategy extends AbstractResolveContextStrategy {
  private boolean useStableIds = false;

  @Nullable
  @Override
  protected Category findNearestCategoryFor(@Nonnull String id, @Nonnull StoreContext storeContext) {
    checkArgument(isNotBlank(id), "You must provide an external id");

    // CMS-3247: Allow category resolution by stable id
    CommerceConnection connection = requireNonNull(DefaultConnection.get(), "no commerce connection available");
    CommerceIdProvider idProvider = connection.getIdProvider();
    String formattedId = useStableIds ? idProvider.formatCategoryId(id) :
            idProvider.formatCategoryTechId(id);

    CatalogService catalogService = requireNonNull(connection.getCatalogService(), "no catalog service available");
    return catalogService.withStoreContext(storeContext).findCategoryById(formattedId);
  }

  /**
   * Set to true, if you want to use the stable {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalId() id}
   * to identify a category. Otherwise the volatile {@link com.coremedia.livecontext.ecommerce.catalog.Category#getExternalTechId()} id}
   * is used.
   * @param useStableIds true for stable catalog ids, false for volatile technical ids. default is false.
   */
  public void setUseStableIds(boolean useStableIds) {
    this.useStableIds = useStableIds;
  }

}
