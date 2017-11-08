package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceService;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coremedia.blueprint.base.livecontext.util.CommerceServiceHelper.getServiceProxyForStoreContext;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.WORKSPACE;
import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class WorkspaceServiceImpl implements WorkspaceService {

  private WcWorkspaceWrapperService workspaceWrapperService;
  private CommerceBeanFactory commerceBeanFactory;
  private CommerceCache commerceCache;

  @Nonnull
  @Override
  public List<Workspace> findAllWorkspaces(@Nonnull StoreContext storeContext) {
    UserContext userContext = UserContextHelper.getCurrentContext();

    WorkspacesCacheKey cacheKey = new WorkspacesCacheKey(storeContext, userContext, workspaceWrapperService,
            commerceCache);

    Map map = commerceCache.get(cacheKey);
    if (map == null) {
      return emptyList();
    }

    Object workspacesObj = map.get("workspaces");
    if (!(workspacesObj instanceof List)) {
      return emptyList();
    }

    return createWorkspaceBeansFor((List<Map>) workspacesObj, storeContext);
  }

  @Nonnull
  private List<Workspace> createWorkspaceBeansFor(@Nonnull List<Map> list, @Nonnull StoreContext context) {
    return list.stream()
            .filter(Objects::nonNull)
            .map(workspaceMap -> createWorkspaceBeanFor(workspaceMap, context))
            .filter(Objects::nonNull)
            .collect(toList());
  }

  @Nullable
  private Workspace createWorkspaceBeanFor(@Nonnull Map map, @Nonnull StoreContext context) {
    CommerceId commerceId = commerceId(WORKSPACE).withExternalId((String) map.get("id")).build();
    Workspace workspace = (Workspace) commerceBeanFactory.createBeanFor(commerceId, context);
    ((AbstractIbmCommerceBean) workspace).setDelegate(map);
    return workspace;
  }

  @Nonnull
  @Override
  public WorkspaceService withStoreContext(StoreContext storeContext) {
    return getServiceProxyForStoreContext(storeContext, this, WorkspaceService.class);
  }

  @Required
  public void setWorkspaceWrapperService(WcWorkspaceWrapperService workspaceWrapperService) {
    this.workspaceWrapperService = workspaceWrapperService;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
    this.commerceBeanFactory = commerceBeanFactory;
  }
}
