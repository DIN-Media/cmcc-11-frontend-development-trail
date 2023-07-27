package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.MappedCatalogsProvider;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.InterceptService;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNull;

/**
 * A REST service to augment a category.
 */
@Service
public class CategoryAugmentationHelper extends AugmentationHelperBase<Category> {
  private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());
  public static final String CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY = "pdpPagegrid";
  private static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";
  static final String CATEGORY_PAGEGRID_STRUCT_PROPERTY = PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
  static final String TITLE = "title";
  static final String SEGMENT = "segment";

  private final MappedCatalogsProvider mappedCatalogsProvider;
  private final ContentBackedPageGridService contentBackedPageGridService;

  CategoryAugmentationHelper(@NonNull @Qualifier("categoryAugmentationService") AugmentationService categoryAugmentationService,
                             @NonNull ContentRepository contentRepository,
                             @NonNull InterceptService interceptService,
                             @NonNull SitesService sitesService,
                             @NonNull MappedCatalogsProvider mappedCatalogsProvider,
                             @NonNull ContentBackedPageGridService contentBackedPageGridService,
                             @NonNull @Value("${livecontext.augmentation.path:" + DEFAULT_BASE_FOLDER_NAME + "}") String baseFolderName) {
    super(categoryAugmentationService, contentRepository, interceptService, sitesService, baseFolderName);
    this.mappedCatalogsProvider = mappedCatalogsProvider;
    this.contentBackedPageGridService = contentBackedPageGridService;
  }

  @Override
  @Nullable
  Content augment(@NonNull Category category) {
    Site site = getSite(category);
    if (site == null) {
      return null;
    }

    // create folder hierarchy for category
    Content categoryFolder = getContentRepository().createSubfolders(computerFolderPath(category, site, getBaseFolderName(),
            (CommerceBean bean) -> getCatalog(category)));

    if (categoryFolder == null) {
      return null;
    }

    Map<String, Object> properties = buildCategoryContentDocumentProperties(category);
    initializeLayoutSettings(category, properties);

    return createContent(CM_EXTERNAL_CHANNEL, categoryFolder, computeDocumentName(category), properties);
  }


  @NonNull
  public static String computeDocumentName(@NonNull Category category) {
    return shortenContentNameIfNeeded(getEscapedDisplayName(category) + " (" + category.getExternalId() + ")")
            .replace('/', '_');
  }

  @VisibleForTesting
  void initializeLayoutSettings(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Category rootCategory = getRootCategory(category);
    Content rootCategoryContent = getCategoryContent(rootCategory);

    if (rootCategoryContent == null) {
      if (!category.isRoot()) {
        //throw ecxeption if current category is not a root category
        // and if root category of current catalog is not augmented yet
        String msg = "Root category is not augmented (requested category is ' " + category.getId() +
                "') , cannot set default layouts.";
        LOGGER.warn(msg);
        throw new CommerceAugmentationException(msg);
      } else {
        //initialize root category content for current catalog
        initializeRootCategoryContent(category, properties);
      }
    } else {
      //initialize with current catalogs augmented root category
      initializeCategoryLayout(rootCategoryContent, rootCategory, category, properties);
      initializeProductLayout(rootCategoryContent, rootCategory, category, properties);
    }
  }

  @VisibleForTesting
  void initializeRootCategoryContent(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Optional<Content> otherAugmentedRootCategory = lookupAugmentedRootCategoryInOtherCatalogs(category);
    if (otherAugmentedRootCategory.isPresent()) {
      initializeCategoryLayout(otherAugmentedRootCategory.get(), category, category, properties);
      initializeProductLayout(otherAugmentedRootCategory.get(), category, category, properties);
    } else {
      initializeLayoutSettingsWithSiteRoot(category, properties);
    }
  }

  private void initializeLayoutSettingsWithSiteRoot(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Site site = requireNonNull(getSitesService().getSite(category.getContext().getSiteId()));
    Content siteRootDocument = site.getSiteRootDocument();
    if (siteRootDocument != null) {
      Content layout = contentBackedPageGridService.getLayout(siteRootDocument, PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY);
      LOGGER.info("Getting default layout from site root document '{}' results to '{}'", siteRootDocument, layout);
      Struct structWithLayoutLink = createStructWithLayoutLink(layout);
      properties.put(CATEGORY_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
      properties.put(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
    }
  }

  @VisibleForTesting
  Optional<Content> lookupAugmentedRootCategoryInOtherCatalogs(@NonNull Category category) {
    //lookup in other catalogs
    LOGGER.debug("Root category of '{}' is not augmented. " +
            "Trying to find augmented root category in neigbour catalogs", category.getId());


    // try to find another root category, maybe in another catalog
    StoreContext context = category.getContext();
    List<Category> configuredRootCategories =
            mappedCatalogsProvider.getConfiguredRootCategories(context);

    return configuredRootCategories.stream()
            .map(getCategoryAugmentationService()::getContent)
            .filter(Objects::nonNull)
            .findFirst();
  }

  private void initializeCategoryLayout(@NonNull Content rootCategoryContent, @NonNull Category rootCategory,
                                        @NonNull CommerceBean commerceBean, @NonNull Map<String, Object> properties) {
    Content defaultCategoryLayoutSettings = getLayoutSettings(rootCategoryContent, CATEGORY_PAGEGRID_STRUCT_PROPERTY);

    if (defaultCategoryLayoutSettings == null) {
      LOGGER.warn("No default category page layout found for root category '{}', "
                      + "cannot initialize category page layout for augmented category '{}'.",
              rootCategory.getId(), commerceBean.getId());
      return;
    }

    Struct structWithLayoutLink = createStructWithLayoutLink(defaultCategoryLayoutSettings);
    properties.put(CATEGORY_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
  }

  private void initializeProductLayout(@NonNull Content rootCategoryContent, @NonNull Category rootCategory,
                                       @NonNull CommerceBean commerceBean, @NonNull Map<String, Object> properties) {
    Content defaultProductLayoutSettings = getLayoutSettings(rootCategoryContent, CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);

    if (defaultProductLayoutSettings == null) {
      LOGGER.warn("No default product page layout found for root category '{}', "
                      + "cannot initialize product page layout for augmented category '{}'.",
              rootCategory.getId(), commerceBean.getId());
      return;
    }

    Struct structWithLayoutLink = createStructWithLayoutLink(defaultProductLayoutSettings);
    properties.put(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
  }

  /**
   * Builds properties for an <code>CMExternalChannel</code> document.
   */
  private static Map<String, Object> buildCategoryContentDocumentProperties(@NonNull Category category) {
    Map<String, Object> properties = new HashMap<>();

    properties.put(EXTERNAL_ID, format(category.getId()));

    // Initialize title and segment with the display name instead of relying on
    // `ContentInitializer.initChannel` as the latter will initialize the title
    // with the escaped display name of the content.
    properties.put(TITLE, category.getDisplayName());
    properties.put(SEGMENT, category.getDisplayName());

    return properties;
  }

}
