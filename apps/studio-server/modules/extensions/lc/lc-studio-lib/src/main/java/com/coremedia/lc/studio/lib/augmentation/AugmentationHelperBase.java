package com.coremedia.lc.studio.lib.augmentation;

import com.coremedia.blueprint.base.livecontext.util.CommerceBeanUtils;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.common.CapException;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.common.DuplicateNameException;
import com.coremedia.cap.common.descriptors.StringPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.InterceptService;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.coremedia.cap.struct.StructBuilderMode.LOOSE;
import static java.lang.invoke.MethodHandles.lookup;

abstract class AugmentationHelperBase<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());
  private static final String OTHER_CATALOGS_FOLDER_NAME = "_other_catalogs";
  public static final String DEFAULT_BASE_FOLDER_NAME = "Augmentation";
  static final String EXTERNAL_ID = "externalId";
  public static final int MAX_CONTENT_NAME_LENGTH = 233;

  private final AugmentationService categoryAugmentationService;
  private final ContentRepository contentRepository;
  private final InterceptService interceptService;
  private final SitesService sitesService;
  private final String baseFolderName;

  AugmentationHelperBase(@NonNull AugmentationService categoryAugmentationService,
                         @NonNull ContentRepository contentRepository,
                         @NonNull InterceptService interceptService,
                         @NonNull SitesService sitesService,
                         @NonNull String baseFolderName) {
    this.categoryAugmentationService = categoryAugmentationService;
    this.contentRepository = contentRepository;
    this.interceptService = interceptService;
    this.sitesService = sitesService;
    this.baseFolderName = baseFolderName;
  }

  protected ContentRepository getContentRepository() {
    return contentRepository;
  }

  protected SitesService getSitesService() {
    return sitesService;
  }

  public AugmentationService getCategoryAugmentationService() {
    return categoryAugmentationService;
  }

  @Nullable
  abstract Content augment(@NonNull T type);

  @SuppressWarnings("OverlyComplexMethod")
  @Nullable
  protected Content createContent(@NonNull String type, @NonNull Content parent, @NonNull String name, @NonNull Map<String, Object> properties) {
    // Create content (taking possible interceptors into consideration)
    ContentWriteRequest writeRequest = null;
    ContentType contentType = contentRepository.getContentType(type);
    if (contentType == null) {
      LOGGER.error("Content type '{}' is not available.", type);
      return null;
    }

    Map<String, Object> shortenedProperties = shortenPropertyValuesIfNeeded(properties, contentType, List.of(EXTERNAL_ID));

    writeRequest = interceptService.interceptCreate(parent, name, contentType, shortenedProperties);
    interceptService.handleErrorIssues(writeRequest);

    Content content = parent.getChild(name);
    if (content != null) {
      return content;
    }

    try {
      Map<String, Object> myProperties = writeRequest != null ? writeRequest.getProperties() : shortenedProperties;
      content = contentType.create(parent, name.trim(), myProperties);
    } catch (DuplicateNameException e) {
      LOGGER.debug("Ignored concurrent (redundant) augmentation request", e);
      content = parent.getChild(name);
    } catch (CapException e) {
      LOGGER.error("An error occured while augmenting category", e);
      throw e;
    }

    // will most likely be non-null but maybe we're facing a concurrent content deletion
    if (content == null) {
      return null;
    }

    interceptService.postProcess(content, null);

    return content;
  }

  /**
   * Checks if property values are too long for the underlying StringProperty fields and shorten items
   * to the maximum number of characters automatically.
   *
   * @param properties the input parameters
   * @param contentType the target content type
   * @param excludedProperties property names, which shall not be shortened automatically
   * @return a copy of the map with shortened values if needed
   */
  @VisibleForTesting
  @NonNull
  protected static Map<String, Object> shortenPropertyValuesIfNeeded(@NonNull Map<String, Object> properties, @NonNull CapType contentType, @NonNull Collection<String> excludedProperties) {
    Map<String, Object> shortenedProperties = new HashMap<>(properties);

    contentType.getDescriptors().stream()
            .filter(StringPropertyDescriptor.class::isInstance)
            .filter(descriptor -> !excludedProperties.contains(descriptor.getName()))
            .map(StringPropertyDescriptor.class::cast)
            .forEach(descriptor -> {
              var propertyName = descriptor.getName();
              var propertyValue = shortenedProperties.get(propertyName);
              if (propertyValue instanceof String) {
                var string = (String) propertyValue;
                var length = descriptor.getLength();
                if (string.length() > length) {
                  shortenedProperties.put(propertyName, string.substring(0, length - 1));
                  LOGGER.info("Problem while augmenting catalog object. Properties: '{}'. " +
                          "Predefined property value for '{}' too long. " +
                          "Value is shortened automatically to '{}' chars. ", properties, propertyName, length);
                }
              }
            });

    return shortenedProperties;
  }

  @NonNull
  public static String computeFolderPath(@NonNull CommerceBean commerceBean, @NonNull Site site, @NonNull String baseFolderName) {
    return computerFolderPath(commerceBean, site, baseFolderName, CommerceBeanUtils::getCatalog);
  }

  @NonNull
  public static String computerFolderPath(@NonNull CommerceBean commerceBean, @NonNull Site site, @NonNull String baseFolderName,
                                          Function<CommerceBean, Optional<Catalog>> catalogExtractor) {
    Category category = getCategoryForCommerceBean(commerceBean);

    String rootPath = site.getSiteRootFolder().getPath();

    Collection<String> subPathsToJoin = new ArrayList<>(List.of(rootPath, baseFolderName));

    //Each catalog needs a separate folder. If not default catalog use the catalog alias as the basefolder.
    Optional<Catalog> catalog = catalogExtractor.apply(category);

    //when catalog is empty then we assume that there is only the default catalog
    boolean isDefaultCatalog = catalog.map(Catalog::isDefaultCatalog).orElse(true);
    if (!isDefaultCatalog) {
      CatalogAlias catalogAlias = category.getId().getCatalogAlias();
      subPathsToJoin.add(OTHER_CATALOGS_FOLDER_NAME);
      subPathsToJoin.add(catalogAlias.value());
    }

    for (Category breadcrumbCategory : category.getBreadcrumb()) {
      subPathsToJoin.add(shortenContentNameIfNeeded(getEscapedDisplayName(breadcrumbCategory)));
    }

    return String.join("/", subPathsToJoin);
  }

  @NonNull
  static String shortenContentNameIfNeeded(@NonNull String name) {
    int maxLength = Math.min(name.length(), MAX_CONTENT_NAME_LENGTH);
    return name.substring(0, maxLength);
  }

  @NonNull
  Optional<Catalog> getCatalog(@NonNull CommerceBean category) {
    return CommerceBeanUtils.getCatalog(category);
  }

  @NonNull
  public static String getEscapedDisplayName(@NonNull Category category) {
    // External ids of category can contain '/'. See CMS-5075
    return category.getDisplayName().replace('/', '_').trim();
  }

  protected Content getCategoryContent(@NonNull Category category) {
    return categoryAugmentationService.getContent(category);
  }

  @NonNull
  protected static Category getRootCategory(@NonNull CommerceBean commerceBean) {
    Category currentCategory = getCategoryForCommerceBean(commerceBean);

    // Checkmarx complains about "Unchecked input for loop condition" here.
    // That is a false positive.
    // The CommerceBean used here is created through a REST entity mapping, therefore already checked before used.
    while (!currentCategory.isRoot()) {
      Category parent = currentCategory.getParent();
      if (parent == null) {
        LOGGER.warn("Root category '{}' is not properly recognized.", currentCategory.getId());
        return currentCategory;
      }

      currentCategory = parent;
    }

    return currentCategory;
  }

  @Nullable
  protected Site getSite(CommerceBean category) {
    return this.sitesService.getSite(category.getContext().getSiteId());
  }

  @NonNull
  private static Category getCategoryForCommerceBean(@NonNull CommerceBean commerceBean) {
    if (commerceBean instanceof Category) {
      return (Category) commerceBean;
    } else if (commerceBean instanceof Product) {
      return ((Product) commerceBean).getCategory();
    } else {
      throw new IllegalArgumentException("Unsupported commerce bean type.");
    }
  }

  @Nullable
  protected static Content getLayoutSettings(@SuppressWarnings("TypeMayBeWeakened") @NonNull Content content, @NonNull String pageGridName) {
    Struct placementStruct = CapStructHelper.getStruct(content, pageGridName);
    if (placementStruct == null) {
      return null;
    }

    Struct placements2Struct = CapStructHelper.getStruct(placementStruct, PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    if (placements2Struct == null) {
      return null;
    }

    return (Content) placements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
  }

  private Struct createEmptyStruct() {
    return contentRepository.getConnection().getStructService().createStructBuilder().build();
  }

  protected Struct createStructWithLayoutLink(Content layoutSettings) {
    Struct pageGridStruct = createEmptyStruct();

    StructBuilder builder = pageGridStruct.builder().mode(LOOSE);
    builder.set(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME, createEmptyStruct());
    builder.enter(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    builder.declareLink(PageGridContentKeywords.LAYOUT_PROPERTY_NAME,
            contentRepository.getContentType("Document_"), layoutSettings);
    builder.add(PageGridContentKeywords.PLACEMENTS_PLACEMENTS_PROPERTY_NAME, createEmptyStruct());
    return builder.build();
  }

  public String getBaseFolderName() {
    return baseFolderName;
  }
}
