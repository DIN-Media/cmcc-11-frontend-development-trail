package com.coremedia.ecommerce.studio.rest;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_CATALOG_ALIAS;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_ID;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.PATH_SITE_ID;
import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.QUERY_ID;

/**
 * Handler used for commerce bean resource requests with "/" in the external id.
 * Id is passed as query parameter instead of path parameter.
 * The handler delegates to the original {@link CategoryResource}, {@link ProductResource} or {@link ProductVariantResource}.
 * {@see com.coremedia.ecommerce.studio.rest.filter.CatalogResourceEncodingFilter}
 */
@RestController
@DefaultAnnotation(NonNull.class)
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CommerceBeanResourceWithEncodedId {
  private static final String PATH_RESOURCE_TYPE = "resourceType";
  static final String URI_PATH
          = "livecontext/{" + PATH_RESOURCE_TYPE + "}/{" + PATH_SITE_ID + "}/{" + PATH_CATALOG_ALIAS + "}";

  private final Map<String, CommerceBeanResource> resourceMap;

  public CommerceBeanResourceWithEncodedId(CategoryResource categoryResource, ProductResource productResource,
                                           ProductVariantResource productVariantResource) {
    this.resourceMap = Map.of(
            CategoryResource.PATH_TYPE, categoryResource,
            ProductResource.PATH_TYPE, productResource,
            ProductVariantResource.PATH_TYPE, productVariantResource
    );
  }

  @GetMapping(value = CommerceBeanResourceWithEncodedId.URI_PATH, params = QUERY_ID)
  public AbstractCatalogRepresentation get(@PathVariable(PATH_RESOURCE_TYPE) String resourceType,
                                           @PathVariable(PATH_SITE_ID) String siteId,
                                           @PathVariable(PATH_CATALOG_ALIAS) String catalogAlias,
                                           @RequestParam(PATH_ID) String id) {
    CommerceBeanResource resource = lookupResource(resourceType)
            .orElseThrow(IllegalArgumentException::new);

    return resource.getRepresentation(Map.of(
            PATH_RESOURCE_TYPE, resourceType,
            PATH_SITE_ID, siteId,
            PATH_CATALOG_ALIAS, catalogAlias,
            PATH_ID, id));
  }

  private Optional<CommerceBeanResource> lookupResource(String lookupKey){
    return Optional.ofNullable(resourceMap.get(lookupKey));
  }
}
