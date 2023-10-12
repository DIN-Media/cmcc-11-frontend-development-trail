package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions;
import com.coremedia.blueprint.base.cae.web.taglib.UniqueIdGenerator;
import com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNamesFreemarker;
import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.taxonomies.TaxonomyLocalizationStrategy;
import com.coremedia.blueprint.cae.action.webflow.BlueprintFlowUrlHandler;
import com.coremedia.blueprint.cae.web.FreemarkerEnvironment;
import com.coremedia.blueprint.cae.web.links.ThemeResourceLinkBuilder;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.blueprint.common.contentbeans.AbstractPage;
import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMImageMap;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMLocalized;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.Container;
import com.coremedia.blueprint.common.layout.DynamicContainerStrategy;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.util.ContainerFlattener;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.cap.transform.Transformation;
import com.coremedia.common.util.WordAbbreviator;
import com.coremedia.image.ImageDimensionsExtractor;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.taglib.MetadataTagSupport;
import com.coremedia.transform.TransformedBlob;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * A Facade for utility functions used by FreeMarker templates.
 */
public class BlueprintFreemarkerFacade extends MetadataTagSupport {

  private static final Logger LOG = LoggerFactory.getLogger(BlueprintFreemarkerFacade.class);
  private static final String RESPONSIVE_SETTINGS_KEY = "responsiveImageSettings";
  private static final String LINK_MIME_TYPE_MAPPING_KEY = "linkMimeTypeMapping";
  private static final String FRAGMENT_PREVIEW_KEY = "fragmentPreview";

  public static final String DEFAULT_DIRECTION = "ltr";
  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private static final int DISPLAYABLE_IMAGE_MAX_SIZE = 10485760; // 10 MB
  private static final int DISPLAYABLE_VIDEO_MAX_SIZE = 1073741824; // 1 GB
  private static final String DISPLAYABLE_IMAGE_PRIMARY_MIMETYPE = "image";
  private static final String DISPLAYABLE_VIDEO_PRIMARY_MIMETYPE = "video";
  private static final List<String> DISPLAYABLE_IMAGE_SUB_MIMETYPES = List.of("jpg", "jpeg", "png");

  static final String HAS_ITEMS = "hasItems";
  static final String PLACEMENT_NAME = "placementName";
  static final String IS_IN_LAYOUT = "isInLayout";

  private SettingsService settingsService;
  private ImageDimensionsExtractor imageDimensionsExtractor;
  private ThemeResourceLinkBuilder themeResourceLinkBuilder;
  private TransformImageService transformImageService;
  private WordAbbreviator abbreviator;
  private MimeTypeService mimeTypeService;
  private ThemeService themeService;
  private ContextHelper contextHelper;
  private DynamicContainerStrategy dynamicContainerStrategy;
  private TaxonomyLocalizationStrategy taxonomyLocalizationStrategy;

  private final ViewHookEventNamesFreemarker viewHookEventNames = new ViewHookEventNamesFreemarker();

  // --- spring config -------------------------------------------------------------------------------------------------

  @Autowired
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Autowired
  public void setImageDimensionsExtractor(ImageDimensionsExtractor imageDimensionsExtractor) {
    this.imageDimensionsExtractor = imageDimensionsExtractor;
  }

  @Autowired
  public void setStringAbbreviator(WordAbbreviator abbreviator) {
    this.abbreviator = abbreviator;
  }

  @Autowired
  public void setMimeTypeService(MimeTypeService mimeTypeService) {
    this.mimeTypeService = mimeTypeService;
  }

  @Autowired
  public void setTransformImageService(TransformImageService transformImageService) {
    this.transformImageService = transformImageService;
  }

  @Autowired
  public void setTaxonomyLocalizationStrategy(TaxonomyLocalizationStrategy taxonomyLocalizationStrategy) {
    this.taxonomyLocalizationStrategy = taxonomyLocalizationStrategy;
  }

  @Autowired
  public void setThemeResourceLinkBuilder(ThemeResourceLinkBuilder themeResourceLinkBuilder) {
    this.themeResourceLinkBuilder = themeResourceLinkBuilder;
  }

  @Autowired
  public void setThemeService(ThemeService themeService) {
    this.themeService = themeService;
  }

  @Autowired
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  @Autowired(required = false)
  public void setDynamicContainerStrategy(DynamicContainerStrategy dynamicContainerStrategy) {
    this.dynamicContainerStrategy = dynamicContainerStrategy;
  }

  // --- functionality -------------------------------------------------------------------------------------------------

  public List<Transformation> getTransformations(Content content) {
    return transformImageService.getTransformations(content);
  }

  public Object setting(Object self, String key) {
    return setting(self, key, null);
  }

  public Object setting(Object self, String key, Object defaultValue) {
    return setting(self, key, defaultValue, null);
  }

  public Object setting(Object self, String key, Object defaultValue, Page defaultPage) {
    CMNavigation context = retrieveContextFor(self);
    CMTheme theme = null;
    if (context == null && defaultPage != null) {
      context = defaultPage.getContext();
    }
    if (context != null) {
      theme = findThemeFor(context);
    }
    return settingsService.settingWithDefault(key, Object.class, defaultValue, self, context, theme);
  }

  public Boolean isActiveNavigation(Object navigation, List<Object> navigationPathList) {
    return navigationPathList.contains(navigation);
  }

  public String generateId(String prefix) {
    return UniqueIdGenerator.generateId(prefix, FreemarkerEnvironment.getCurrentRequest());
  }

  public String getStackTraceAsString(Exception e) {
    //print stackTrace the Java way here so that we automatically get all causes, messages etc.
    StringWriter stringWriter = new StringWriter(); //NOSONAR
    e.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  /**
   * Returns a String representation of an array of JSON objects with a list of aspect ratios with image links for different sizes.
   *
   * @param picture      the image
   * @param page         the root page
   * @param aspectRatios list of aspect ratios to use for this image
   * @return Json Object with a list of aspect ratios with image links for different sizes
   */
  public List<TransformationLinks> responsiveImageLinksData(CMPicture picture, Page page, List<String> aspectRatios) {
    if (picture == null) {
      throw new IllegalArgumentException("Error creating responsive image links: picture must not be null");
    }

    Map<String, Map> responsiveImageSettings = getResponsiveImageSettings(page);
    Map<String, String> linkMimeTypeMappingSettings = getLinkMimeTypeMapping(page);

    List<String> aspectRatiosToUse = aspectRatios;
    // use list of given aspect ratios if set, otherwise use all
    if (isEmpty(aspectRatiosToUse)) {
      aspectRatiosToUse = new ArrayList<>(responsiveImageSettings.keySet());
    }

    HttpServletRequest currentRequest = FreemarkerEnvironment.getCurrentRequest();
    HttpServletResponse currentResponse = FreemarkerEnvironment.getCurrentResponse();
    List<TransformationLinks> result = new ArrayList<>();
    for (String aspectRatioName : aspectRatiosToUse) {
      @SuppressWarnings("unchecked")
      Map<String, Map> aspectRatioSizes = responsiveImageSettings.get(aspectRatioName);
      if (aspectRatioSizes != null) {
        Blob blob = picture.getTransformedData(aspectRatioName);
        String targetMimeType = getMappedMimeType(blob, linkMimeTypeMappingSettings);
        Map<Integer, String> links = ImageFunctions.getImageLinksForAspectRatios(blob, aspectRatioName, aspectRatioSizes,
                false, targetMimeType, currentRequest, currentResponse);

        if (!isEmpty(links)) {
          // only the "TransformImageService" holds the actual crop ratio in proper values
          Transformation transformation = transformImageService.getTransformation(picture.getContent(), aspectRatioName);
          if (transformation == null) {
            throw new IllegalArgumentException("Could not find image variant for name " + aspectRatioName);
          }
          TransformationLinks transformationLinks = new TransformationLinks(
                  aspectRatioName, transformation.getWidthRatio(), transformation.getHeightRatio(), links
          );
          result.add(transformationLinks);

        } else {
          LOG.info("No responsive image links found for CMPicture {} with transformationName {}", picture, aspectRatioName);
        }
      }

    }

    if (isEmpty(result)) {
      LOG.warn("No responsive image links found for CMPicture {}", picture);
    }

    return result;
  }

  public String transformedImageUrl(CMPicture picture, String aspectRatio, int width, int height) {
    if (picture == null) {
      throw new IllegalArgumentException("Error creating image link: picture must not be null");
    }
    if (aspectRatio == null) {
      throw new IllegalArgumentException("Error creating image link: aspect ratio must not be null");
    }

    Blob blob = picture.getTransformedData(aspectRatio);
    if (blob == null) {
      LOG.warn("Transformation not applicable: {} -> {}, width {}, height {}", picture, aspectRatio, width, height);
      return "";
    }

    String link = ImageFunctions.getImageLinkForAspectRatio(blob,
            aspectRatio,
            Map.of(ImageFunctions.WIDTH, width, ImageFunctions.HEIGHT, height),
            false,
            FreemarkerEnvironment.getCurrentRequest(),
            FreemarkerEnvironment.getCurrentResponse());
    return link == null ? "" : link;
  }

  public String getLinkForBiggestImageWithRatio(CMPicture picture, Page page, String aspectRatio) {
    if (picture == null) {
      throw new IllegalArgumentException("Error creating responsive image links: picture must not be null");
    }
    if (aspectRatio == null) {
      throw new IllegalArgumentException("Error creating responsive image links: aspect ratio must not be null");
    }

    Map<String, Map> responsiveImageSettings = getResponsiveImageSettings(page);
    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> aspectRatioSizes = responsiveImageSettings.get(aspectRatio);
    if (aspectRatioSizes == null || aspectRatioSizes.isEmpty()) {
      throw new IllegalArgumentException(String.format("Error creating responsive image links: aspect ratio '%s' not defined", aspectRatio));
    }
    @SuppressWarnings("unchecked")
    Map<String, ?> biggestSize = ImageFunctions.getBiggestSize(aspectRatioSizes);
    if (biggestSize != null) {
      Blob transformedData = picture.getTransformedData(aspectRatio);
      if (transformedData != null) {
        String targetMimeType = getMappedMimeType(transformedData, getLinkMimeTypeMapping(page));
        return ImageFunctions.getImageLinkForAspectRatio(transformedData,
                aspectRatio, biggestSize,
                false,
                targetMimeType,
                FreemarkerEnvironment.getCurrentRequest(),
                FreemarkerEnvironment.getCurrentResponse());
      }
    }
    return "";
  }

  private Map<String, Map> getResponsiveImageSettings(Page page) {
    CMTheme theme = getTheme(page);
    // get responsive image settings
    Map<String, Map> responsiveImageSettings = settingsService.settingAsMap(RESPONSIVE_SETTINGS_KEY, String.class, Map.class, page, theme);
    if (isEmpty(responsiveImageSettings)) {
      throw new IllegalArgumentException("Error creating responsive image links: No responsive image settings found");
    }
    return responsiveImageSettings;
  }

  private Map<String, String> getLinkMimeTypeMapping(Page page) {
    CMTheme theme = getTheme(page);
    return settingsService.settingAsMap(LINK_MIME_TYPE_MAPPING_KEY, String.class, String.class, page, theme);
  }

  // Return the mapped MimeType if it is configured in linkMimeTypeMapping setting, else null.
  private String getMappedMimeType(Blob blob, Map<String, String> linkMimeTypeMapping) {
    String originalMimeType = ((blob instanceof TransformedBlob)
            ? ((TransformedBlob) blob).getOriginal().getContentType()
            : blob.getContentType()).toString();
    String targetMimeType = linkMimeTypeMapping.get(originalMimeType);
    if (LOG.isDebugEnabled() && targetMimeType != null) {
      LOG.debug("Using MimeType '{}' instead of '{}' (mapping configured in setting '{}')", targetMimeType, originalMimeType, LINK_MIME_TYPE_MAPPING_KEY);
    }
    return targetMimeType;
  }

  private CMTheme getTheme(Page page) {
    if (page == null) {
      throw new IllegalArgumentException("Error creating responsive image links: page must not be null");
    }
   return findThemeFor(page.getContext());
  }

  /**
   * Retrieves the preview views of an object based on its hierarchy
   *
   * @param self                 the object to preview
   * @param page                 the page used to find the setting named "fragmentPreview
   * @param defaultFragmentViews a Map defining defaults
   * @return a List of maps of Strings defining which views should be rendered
   */
  public List<Map<String, Object>> getPreviewViews(CMObject self, Page page, List<Map<String, Object>> defaultFragmentViews) {
    CMTheme theme = page != null ? findThemeFor(page.getContext()) : null;
    ContentType contentType = self.getContent().getType();
    List<Map<String, Object>> result = defaultFragmentViews;
    while (contentType != null) {
      List<Map<String, Object>> retrievedSettings = settingsService.nestedSetting(Arrays.asList(FRAGMENT_PREVIEW_KEY, contentType.toString()), List.class, page, theme);
      if (retrievedSettings != null) {
        result = retrievedSettings;
        break;
      }
      contentType = contentType.getParent();
    }
    return result;
  }

  public CMContext getPageContext(Page page) {
    return page.getContext();
  }

  public String getPlacementPropertyName(PageGridPlacement placement) {
    return placement != null ? placement.getPropertyName() : "";
  }

  /**
   * Utility function to allow rendering of containers with custom items, e.g. partial containers with a subset of
   * the items the original container had.
   *
   * @param items The items to be put inside the new container
   * @return a new container
   */
  public Container getContainer(final List<Object> items) {
    return new Container() {
      @Override
      public List getItems() {
        return items;
      }

      @Override
      public List getFlattenedItems() {
        return ContainerFlattener.flatten(this, Object.class);
      }
    };
  }

  /**
   * Utility function to allow rendering of containers with custom items, e.g. partial containers with a subset of
   * the items the original container had.
   *
   * @param baseContainer The base container the new container shall be created from
   * @param items         The items to be put inside the new container
   * @return a new container based on the given base container
   */
  public Container getContainer(Container baseContainer, List<Object> items) {
    return new ContainerWithViewTypeName(baseContainer, items);
  }

  /**
   * Utility function to allow rendering of a dynamizable container.
   * Such container must be persistent. That means the container can be recreated and the items list
   * can be reestablished.
   *
   * @param object the object that can be used to call the getter
   * @param propertyPath the propertyPath for retrieving the container's items. Must adhere to the contract of {@link PropertyAccessor#getPropertyValue(java.lang.String)}
   * @return a new container
   * @see PropertyAccessor#getPropertyValue(java.lang.String)
   */
  public Container getDynamizableContainer(@NonNull Object object, @NonNull String propertyPath) {
    if (!(object instanceof CMTeasable)) {
      throw new IllegalArgumentException("Only CMTeasable type supported");
    }
    CMTeasable teasable = (CMTeasable) object;

    if (dynamicContainerStrategy != null) {
      return new DynamizableCMTeasableContainerWithDynamicStrategy(teasable, propertyPath, dynamicContainerStrategy);
    } else {
      return new DynamizableCMTeasableContainerNonDynamic(teasable, propertyPath);
    }
  }

  public boolean isWebflowRequest() {
    HttpServletRequest currentRequest = FreemarkerEnvironment.getCurrentRequest();
    return currentRequest.getRequestURL().toString().contains(UriConstants.Segments.PREFIX_DYNAMIC)
            && currentRequest.getParameterMap().containsKey(BlueprintFlowUrlHandler.FLOW_EXECUTION_KEY_PARAMETER);
  }

  /**
   * @param size   in bytes
   * @param locale for file size format
   * @return a human-readable file size
   */
  public String getDisplayFileSize(int size, Locale locale) {
    if (locale == null) {
      locale = DEFAULT_LOCALE;
    }
    int unit = 1024;
    if (size < unit) {
      return size + " Bytes";
    }
    int exp = (int) (Math.log(size) / Math.log(unit));
    char pre = "KMGTPE".charAt(exp - 1);
    return String.format(locale, "%.1f %sB", size / Math.pow(unit, exp), pre);
  }

  public String getDisplayFileFormat(String mimeType) {
    return mimeTypeService.getExtensionForMimeType(mimeType);
  }

  public boolean isDisplayableImage(Blob blob) {
    if (blob.getSize() > DISPLAYABLE_IMAGE_MAX_SIZE) {
      return false;
    }
    MimeType mimeType = blob.getContentType();
    if (!DISPLAYABLE_IMAGE_PRIMARY_MIMETYPE.equals(mimeType.getPrimaryType())) {
      return false;
    }
    return DISPLAYABLE_IMAGE_SUB_MIMETYPES.contains(mimeType.getSubType());
  }

  public boolean isDisplayableVideo(Blob blob) {
    if (blob.getSize() > DISPLAYABLE_VIDEO_MAX_SIZE) {
      return false;
    }
    MimeType mimeType = blob.getContentType();
    return DISPLAYABLE_VIDEO_PRIMARY_MIMETYPE.equals(mimeType.getPrimaryType());
  }

  public List<Map<String, Object>> responsiveImageMapAreas(CMImageMap imageMap, List<String> transformationNames) {

    List<Map<String, Object>> result = Collections.emptyList();
    final CMPicture picture = imageMap.getPicture();

    if (picture != null) {
      // determine which transformations to apply
      final Map<String, String> transformMap = picture.getTransformMap();
      final List<Map<String, Object>> imageMapAreas = imageMap.getImageMapAreas();

      result = ImageFunctions.responsiveImageMapAreas(picture.getData(), picture.getDisableCropping(), imageMapAreas, transformMap, imageDimensionsExtractor, transformationNames);
    }

    return result;
  }

  public Map<String, Object> responsiveImageMapAreaData(Map<String, Object> coords) {
    return ImageFunctions.responsiveImageMapAreaData(coords);
  }

  public int getImageTransformationBaseWidth() {
    return ImageFunctions.getImageTransformationBaseWidth();
  }

  public ViewHookEventNamesFreemarker getViewHookEventNames() {
    return viewHookEventNames;
  }

  /**
   * @return given String truncated to given length, based on words.
   */
  public String truncateText(Object text, int maxLength) {

    String toTruncate = "";

    if (text != null) {
      if (text instanceof Markup) {
        toTruncate = MarkupUtil.asPlainText((Markup) text);
      } else if (text instanceof String) {
        toTruncate = (String) text;
      } else {
        throw new UnsupportedOperationException("Cannot abbreviate value " + text + " of Type" + text.getClass().getName());
      }
    }

    return abbreviator.abbreviateString(toTruncate, maxLength);
  }

  /**
   * Calls truncate text with given parameters and closes the last bold tag at the end.
   * This method is kind of stupid: It just adds the bold tag at the end and does not know
   * about where to add it. It's hard to determine where the bold end tag should be added (after which word?
   * truncate text adds three dots...) but it's easy to say that after the truncated text no bold tag should be open.
   *
   * @param text      the highlighted text
   * @param maxLength the length the text will be truncated to
   * @return truncated text with closed bold tag
   */
  public String truncateHighlightedText(Object text, int maxLength) {
    String startTag = "<b>";
    String endTag = "</b>";
    return truncateHighlightedText(text, maxLength, startTag, endTag);
  }

  /**
   * More generic version of {@link #truncateHighlightedText(Object, int)}.
   * Instead of closing a bold tag this method gets start and end tag as input and closes the given start tag with the
   * given end tag. The tags must be
   *
   * @param text      the highlighted text
   * @param maxLength the length the text will be truncated to
   * @param startTag  start tag which should be closed if it's not closed at the end
   * @param endTag    end tag - used to close the start tag if it's not closed at the end
   * @return truncated text with closed bold tag
   */
  public String truncateHighlightedText(Object text, int maxLength, String startTag, String endTag) {
    String truncatedText = truncateText(text, maxLength);

    //if the tag is smaller, everything is fine if none of those tags exist -1 < -1 is false, but we shouldn't add an end tag
    boolean lastStartTagHasBeenClosed = truncatedText.lastIndexOf(startTag) <= truncatedText.lastIndexOf(endTag);

    if (lastStartTagHasBeenClosed) {
      return truncatedText;
    }

    return truncatedText + endTag;
  }

  /**
   * Checks if the given richtext is empty without the richtext grammar.
   *
   * @param richtext the richtext to check
   * @return true if the given richtext is empty, otherwise false.
   */
  public boolean isEmptyRichtext(Markup richtext) {
    return MarkupUtil.isEmptyRichtext(richtext, true);
  }

  /**
   * Retrieves the URL path that belongs to a theme resource (image, webfont, etc.) defined by its path within the
   * theme folder. The path must not contain any <strong>..</strong>
   * descending path segments.
   *
   * @param pathToResource path to the resource within the theme folder
   * @return the URL path that belongs to a theme resource or an empty link
   */
  public String getLinkToThemeResource(String pathToResource) {
    return themeResourceLinkBuilder.getLinkToThemeResource(pathToResource,
                                                           FreemarkerEnvironment.getCurrentRequest(),
                                                           FreemarkerEnvironment.getCurrentResponse());
  }

  /**
   * <p>
   * Returns the ISO 639 language code for the given object.
   * </p>
   * <dl>
   * <dt><strong>Note:</strong></dt>
   * <dd>
   * ISO 639 is not a stable standard and most likely not the code you want to use in your HTML code as
   * W3C specifies to use IETF BCP 47 language tags. To retrieve a IETF BCP 47 language tag use
   * {@link #getLanguageTag(Object)}.
   * </dd>
   * </dl>
   *
   * @param object object to determine the locale from
   * @return ISO 639 language code
   * @see #getLanguageTag(Object)
   */
  public static String getLanguage(Object object) {
    return getLocale(object).getLanguage();
  }

  /**
   * <p>
   * Returns the IETF BCP 47 language code for the given object. IETF BCP 47 is the specified standard for
   * the {@code lang} attribute of HTML elements.
   * </p>
   *
   * @param object object to determine the locale from
   * @return IETF BCP 47 language code
   * @see <a href="https://www.w3.org/International/questions/qa-html-language-declarations" target="_blank">w3.org: Declaring language in HTML</a>
   * @see #getLanguage(Object)
   */
  public static String getLanguageTag(Object object) {
    return getLocale(object).toLanguageTag();
  }

  @NonNull
  private static Locale getLocale(Object object) {
    Locale locale = DEFAULT_LOCALE;
    if (object instanceof AbstractPage) {
      AbstractPage abstractPage = (AbstractPage) object;
      locale = abstractPage.getLocale();
    }
    if (object instanceof CMLocalized) {
      CMLocalized localized = (CMLocalized) object;
      locale = localized.getLocale();
    }
    return locale;
  }

  /**
   * @param object object to determine the locale direction from
   * @return 'ltr' or 'rtl'
   * @see <a href="https://www.w3.org/International/questions/qa-html-dir#documentlevel" target="_blank">w3.org: Structural markup and right-to-left text in HTML</a>
   */
  public static String getDirection(Object object) {
    if (object instanceof AbstractPage) {
      AbstractPage abstractPage = (AbstractPage) object;
      return abstractPage.getDirection();
    }
    return DEFAULT_DIRECTION;
  }

  /**
   * This method returns a {@link Map} which contains information about the state of the placement.<br>
   * The map contains the following keys: {@link #PLACEMENT_NAME}, {@link #IS_IN_LAYOUT} and {@link #HAS_ITEMS}.<br>
   * The values of those keys are of type boolean.
   *
   * @param placementObject a PageGridPlacement
   * @return a map containing information for placement highlighting
   * @throws IOException
   */
  @NonNull
  public Map<String, Object> getPlacementHighlightingMetaData(@NonNull Object placementObject) throws IOException {
    if (placementObject instanceof ContainerWithViewTypeName) {
      return getPlacementHighlightingMetaData(((ContainerWithViewTypeName) placementObject).getBaseContainer());
    }
    if (placementObject instanceof CMCollection) {
      return Collections.emptyMap();
    }

    PageGridPlacement placement = asPageGridPlacement(placementObject);
    String placementName = placement != null ? placement.getName() : asPageGridPlacementName(placementObject);

    if (placementName == null || !isMetadataEnabled()) {
      return Collections.emptyMap();
    }

    return getPlacementHighlightingMetaDataInternal(placement, placementName);
  }

  private Map<String, Object> getPlacementHighlightingMetaDataInternal(PageGridPlacement placement, String placementName) throws IOException {
    boolean isInLayout = placement != null;
    boolean hasItems = hasItems(placement, isInLayout);

    List<Object> metaDataList = new LinkedList<>();
    metaDataList.add(Map.of(
            IS_IN_LAYOUT, isInLayout,
            HAS_ITEMS, hasItems,
            PLACEMENT_NAME, placementName));
    return Collections.singletonMap("placementRequest", metaDataList);
  }

  private static PageGridPlacement asPageGridPlacement(Object object) {
    if (object instanceof PageGridPlacement) {
      return (PageGridPlacement) object;
    }
    return null;
  }

  private static String asPageGridPlacementName(Object object) {
    if (object instanceof String) {
      return (String) object;
    }
    return null;
  }

  private static boolean hasItems(PageGridPlacement placement, boolean isInLayout) {
    return isInLayout && !placement.getItems().isEmpty();
  }

  /**
   * Returns the value of a translated property
   * looking it up in the localSettings.translations of the given {@link CMLinkable}.
   *
   * @param linkable     the linkable
   * @param locale       target locale
   * @param fallback     fallback value that will be returned if no translated value exists
   * @return
   */
  public Object getTranslatedPropertyValue(CMLinkable linkable, Locale locale, Object fallback) {
    Object value = fallback;

    if(linkable.getContent().getType().isSubtypeOf(CMTaxonomy.NAME)) {
      value = taxonomyLocalizationStrategy.getDisplayName(linkable.getContent(), locale);
    }

    return value;
  }

  private CMTheme findThemeFor(CMNavigation self) {
    return self.getTheme(UserVariantHelper.getUser(FreemarkerEnvironment.getCurrentRequest()));
  }

  private CMNavigation retrieveContextFor(Object self) {
    CMNavigation context = null;
    if (self instanceof Page) {
      context = ((Page) self).getContext();
    } else if (self instanceof CMNavigation) {
      context = (CMNavigation) self;
    } else if (self instanceof CMLinkable) {
      context = contextHelper.contextFor((CMLinkable) self);
    }
    return context;
  }

}
