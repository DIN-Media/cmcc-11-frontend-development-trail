package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.exception.BlobTransformationException;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentObject;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.request.RequestUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.SecureHashCodeGeneratorStrategy;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.transform.TransformedBeanBlob;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_EXTENSION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_NAME;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_RESOURCE;

/**
 * Controller and LinkScheme for transformed blobs
 *
 * @see com.coremedia.transform.TransformedBeanBlob
 */
@Link
@RequestMapping
public class TransformedBlobHandler extends HandlerBase {

  private static final String URI_PREFIX = "image";
  private static final String TRANSFORMATION_SEGMENT = "transformationName";
  private static final String DIGEST_SEGMENT = "digest";
  private static final String SECHASH_SEGMENT = "secHash";
  public static final String WIDTH_SEGMENT = "width";
  public static final String HEIGHT_SEGMENT = "height";
  public static final String TARGET_MIME_TYPE = "targetMimeType";


  /**
   * URI Pattern for transformed blobs.
   * e.g. /image/4302/landscape_ratio4x3/590/442/969e0a0b2eb79df86df7ffecd1375115/eg/london.jpg
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_RESOURCE +
          "/" + URI_PREFIX +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
                  "/{" + TRANSFORMATION_SEGMENT + "}" +
                  "/{" + WIDTH_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + HEIGHT_SEGMENT + ":" + PATTERN_NUMBER + "}" +
                  "/{" + DIGEST_SEGMENT + "}" +
                  "/{" + SECHASH_SEGMENT + "}" +
                  "/{" + SEGMENT_NAME + "}" +
                  ".{" + SEGMENT_EXTENSION + ":" + PATTERN_EXTENSION + "}";

  private ValidationService<ContentBean> validationService;

  private SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy;
  private TransformImageService transformImageService;

  public void setValidationService(ValidationService<ContentBean> validationService) {
    this.validationService = validationService;
  }

  public void setSecureHashCodeGeneratorStrategy(SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy) {
    this.secureHashCodeGeneratorStrategy = secureHashCodeGeneratorStrategy;
  }

  public void setTransformImageService(TransformImageService transformImageService) {
    this.transformImageService = transformImageService;
  }

  @Override
  protected void initialize() {
    super.initialize();
    if (secureHashCodeGeneratorStrategy == null) {
      throw new IllegalStateException("Required property not set: secureHashCodeGeneratorStrategy");
    }
    if (transformImageService == null) {
      throw new IllegalStateException("Required property not set: transformImageService");
    }
    if (validationService == null) {
      throw new IllegalStateException("Required property not set: validationService");
    }
  }

  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@Nullable @PathVariable(SEGMENT_ID) ContentBean contentBean,
                                    @PathVariable(TRANSFORMATION_SEGMENT) String transformationName,
                                    @PathVariable(WIDTH_SEGMENT) Integer width,
                                    @PathVariable(HEIGHT_SEGMENT) Integer height,
                                    @PathVariable(DIGEST_SEGMENT) String digest,
                                    @PathVariable(SECHASH_SEGMENT) String secHash,
                                    @PathVariable(SEGMENT_NAME) String name,
                                    @PathVariable(SEGMENT_EXTENSION) String extension,
                                    WebRequest webRequest,
                                    HttpServletResponse response) {
    if (!(contentBean instanceof CMMedia)) {
      return HandlerHelper.notFound();
    }
    if (!validationService.validate(contentBean)) {
      return HandlerHelper.notFound("The content item you are trying to preview is invalid and cannot be viewed.");
    }

    CMMedia media = getDataViewFactory().loadCached((CMMedia) contentBean, null);
    // URL validation: segment must match and hash value must be correct
    String segment = removeSpecialCharacters(media.getContent().getName());
    if (name.equals(segment)) {
      //name matches, make sure that secHash matches given URL
      Map<String, Object> parameters = Map.of(
              SEGMENT_ID, ((CMMedia) contentBean).getContentId(),
              TRANSFORMATION_SEGMENT, transformationName,
              WIDTH_SEGMENT, width,
              HEIGHT_SEGMENT, height,
              DIGEST_SEGMENT, digest,
              SEGMENT_NAME, name,
              SEGMENT_EXTENSION, extension);
      if (secureHashCodeGeneratorStrategy.matches(parameters, secHash)) {
        //request is valid, resolve blob and return model
        Blob transformedBlob = transformedData(media, transformationName, extension, width, height);
        String blobETag = transformedBlob.getETag();
        if (webRequest.checkNotModified(blobETag)) {
          // shortcut exit - no further processing necessary
          return null;
        }

        // note that links are generated for the variants without width and height
        Blob variant = media.getTransformedData(transformationName);
        boolean isUpToDate = digest.equals(variant.getETag());
        if (!isUpToDate) {
          // the handler detected that the link wasn't generated for this CAE's latest version of the given bean
          if (isSingleNode()) {
            // redirect to latest version
            Map<String, Integer> params = Map.of(HEIGHT_SEGMENT, height, WIDTH_SEGMENT, width);
            return redirectIfPossible(webRequest, variant, params);
          } else {
            // serve current bean but prevent caching
            applyCacheSeconds(response, 0);
          }
        }

        return HandlerHelper.createModel(transformedBlob);
      }
    }

    return HandlerHelper.notFound();
  }

  private ModelAndView redirectIfPossible(WebRequest webRequest, Blob transformedBlob, Map<String, Integer> params) {
    if (!(transformedBlob instanceof TransformedBeanBlob)) {
      // not suitable for link building - transformation disabled?
      return HandlerHelper.createModel(transformedBlob);
    }
    webRequest.setAttribute(RequestUtils.PARAMETERS, params, RequestAttributes.SCOPE_REQUEST);
    return HandlerHelper.redirectTo(transformedBlob, null, HttpStatus.SEE_OTHER);
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  @Link(type = TransformedBeanBlob.class, parameter = {HEIGHT_SEGMENT, WIDTH_SEGMENT}, uri = URI_PATTERN)
  public Map<String, ?> buildLink(TransformedBeanBlob bean, Map<String, String> linkParameters) {
    if (!(bean.getBean() instanceof CMMedia)) {
      return null;
    }

    CapBlobRef original = (CapBlobRef) bean.getOriginal();
    int contentId = IdHelper.parseContentId(original.getCapObject().getId());

    int height = Integer.parseInt(linkParameters.get(HEIGHT_SEGMENT));
    int width = Integer.parseInt(linkParameters.get(WIDTH_SEGMENT));
    // Use targetMimeType if available in parameters (determined via setting "linkMimeTypeMapping" in BlueprintFreemarkerFacade)
    String targetMimeType = linkParameters.get(TARGET_MIME_TYPE);

    /*
     * create parameters map. This is more flexible than calling URI_TEMPLATE#expand with the parameters
     * since this way the parameter's sequence is not relevant and the URI_PATTERN can be changed easier
     */
    Map<String, Object> parameters = Map.of(
            SEGMENT_ID, contentId,
            TRANSFORMATION_SEGMENT, bean.getTransformName(),
            WIDTH_SEGMENT, width,
            HEIGHT_SEGMENT, height,
            DIGEST_SEGMENT, bean.getETag(),
            SEGMENT_NAME, getName(original),
            SEGMENT_EXTENSION, getExtension(getMimeType(targetMimeType, original), CapBlobHandler.BLOB_DEFAULT_EXTENSION));

    //generate secure hash from all parameters and add to map
    String secHash = secureHashCodeGeneratorStrategy.generateSecureHashCode(parameters);

    Map<String, Object> result = new HashMap<>(parameters);
    result.put(SECHASH_SEGMENT, secHash);
    return Collections.unmodifiableMap(result);
  }

  /**
   * Return the transformed blob contained in the given CMMedia object, including all additional delivery transformations.
   * The returned blob may then be used to render information of the transformed blob in addition to the link, such as
   * the download size.
   *
   * <p>Note that accessing any of the methods {@link com.coremedia.cap.common.Blob#getContentType()}
   * or {@link com.coremedia.cap.common.Blob#getSize()} will trigger the transformation of the blob. While the
   * transformation result is cached, this is generally much more costly than merely generating a link to the
   * transformed blob, esp. if transformed blobs are cached by a CDN.
   */
  public Blob getTransformedBlob(CMMedia media, String transformName, String extension, Integer width, Integer height) {
    Optional<Blob> data = transformImageService.transformWithDimensions(media.getContent(), CMMedia.DATA, transformName, width, height, extension);
    return data.orElse(null);
  }


  // === internal ======================================================================================================
  /**
   * Returns the transformed "data" blob of the media bean.
   *
   * @throws BlobTransformationException in case of any problems.
   */
  @NonNull
  private Blob transformedData(CMMedia media, String transformName, String extension, Integer width, Integer height) {
    try {
      Blob blob = getTransformedBlob(media, transformName, extension, width, height);
      if (blob != null) {
        return blob;
      } else {
        // Due to the TransformImageService#transformWithDimensions implementation,
        // null may denote a non-transformable blob, but also an obfuscated IOException.
        // We cannot distinguish this here.
        throw new BlobTransformationException("Transformed blob of " + media.getContent().getId() + "#" + CMMedia.DATA + ", transformation " + transformName + ", width " + width + ", height " + height + " is not available");
      }
    } catch (Exception e) {
      throw new BlobTransformationException("Transformation of blob " + media.getContent().getId() + "#" + CMMedia.DATA + ", transformation " + transformName + ", width " + width + ", height " + height + " failed", e);
    }
  }

  private String getName(CapBlobRef o) {
    if (o.getCapObject().isContentObject() && ((ContentObject) o.getCapObject()).isContent()) {
      String contentName = ((Content) o.getCapObject()).getName();
      return removeSpecialCharacters(contentName);
    }
    throw new IllegalArgumentException("Not a Content Blob: " + o);
  }

  /**
   * Determines the MimeType to be used for creating links.
   * <p>
   * Use targetMimeType if it is a valid MimeType, otherwise use contentType of given original blob.
   * @param targetMimeType the target MIME type
   * @param original the original blob
   * @return the MimeType to be used
   */
  private static MimeType getMimeType(String targetMimeType, CapBlobRef original) {
    MimeType contentType = null;
    if (targetMimeType != null) {
      try {
        contentType = new MimeType(targetMimeType);
      } catch (MimeTypeParseException e) {
        LOG.warn("Invalid MimeType: {} (using original Blob MimeType '{}', please check configured MimeType mapping in setting 'linkMimeTypeMapping')",
                targetMimeType, original.getContentType());
      }
    }
    // If targetMimeType is not set, use content type of original blob, not of the transformed blob which may be different.
    // Requesting the transformed blob's content type forces the transformation to be performed, which is too
    // costly for link generation.
    if (contentType == null) {
      contentType = original.getContentType();
    }
    return contentType;
  }
}
