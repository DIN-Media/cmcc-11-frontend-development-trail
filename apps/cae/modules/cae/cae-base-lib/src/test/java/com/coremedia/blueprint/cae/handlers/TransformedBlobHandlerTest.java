package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.configuration.CaeConfigurationProperties;
import com.coremedia.objectserver.request.RequestUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.transform.TransformedBeanBlob;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.cae.web.links.NavigationLinkSupport.ATTR_NAME_CMNAVIGATION;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link TransformedBlobHandler}
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TransformedBlobHandlerTestConfiguration.class)
public class TransformedBlobHandlerTest {

  private TransformedBeanBlob transformedBlob;

  @MockBean
  private TransformImageService transformImageService;

  @MockBean
  private ValidationService<ContentBean> validationService;

  @Inject
  private MockMvc mockMvc;
  @Inject
  private LinkFormatter linkFormatter;
  @Inject
  private ContentTestHelper contentTestHelper;
  @Inject
  private CaeConfigurationProperties properties;

  //----
  private static final String DIGEST = "digest";
  private static final String TRANSFORM_NAME = "transformName";
  private static final String WIDTH = "100";
  private static final String HEIGHT = "100";

  @Before
  public void setUp() {
    properties.setSingleNode(false);
    transformedBlob = mock(TransformedBeanBlob.class);
    when(transformedBlob.getETag()).thenReturn(DIGEST);
    when(transformedBlob.getTransformName()).thenReturn(TRANSFORM_NAME);
    when(transformImageService.getTransformationOperations(any(Content.class), anyString()))
            .thenReturn(Map.of(TRANSFORM_NAME, TRANSFORM_NAME));

    when(transformImageService.transformWithDimensions(any(Content.class), anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(Optional.of(transformedBlob));
    when(validationService.validate(any())).thenReturn(true);
  }

  /**
   * Test bean resolution and pattern matching:
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testBean() throws Exception {
    assertModel(handleRequest("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"), transformedBlob);
  }

  @Test
  public void acceptUrlWithOriginalExtensionInsteadOfTransformedExtension() throws Exception {
    // in this test case, the original JPEG is transformed to a PNG
    when(transformedBlob.getContentType()).thenReturn(new MimeType("image/png"));

    assertModel(handleRequest("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"), transformedBlob);
  }

  @Test
  public void acceptUrlWithWrongDigest() throws Exception {
    performMockRequest("/resource/image/16/transformName/100/100/dunno/TM/nae-me-jpg.jpg")
            .andExpect(
                    mvcResult -> {
                      assertModel(mvcResult.getModelAndView(), transformedBlob);
                      assertThat(mvcResult.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
                    }
            );
  }

  @Test
  public void acceptUrlWithWrongDigestSingleNode() throws Exception {
    properties.setSingleNode(true);

    Blob ratioBlob = contentTestHelper.<CMPicture>getContentBean(16)
            .getTransformedData(TRANSFORM_NAME);

    performMockRequest("/resource/image/16/transformName/100/100/dunno/TM/nae-me-jpg.jpg")
            .andExpect(
                    mvcResult -> {
                      assertThat(mvcResult.getModelAndView()).returns("redirect:DEFAULT", ModelAndView::getViewName);
                      // link building does not take the final transformed blob into account
                      assertModel(mvcResult.getModelAndView(), ratioBlob);
                      assertThat(mvcResult.getResponse().getHeader("Cache-Control")).isNull();
                    }
            );
  }

  /**
   * Test bean resolution with a Japanese segment name (BARBUDA-2637).
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testJapaneseSegmentName() throws Exception {
    // Java literals use UTF-16 code points (requiring 2 bytes per character), whereas in the URL,
    // the segment will be encoded in UTF-8, requiring three bytes per character.
    // The UTF-8, URL encoded segment equivalent to these four characters, is "%E8%A9%A6%E9%A8%93%E7%94%BB%E5%83%8F".
    String japaneseName = "\u8A66\u9A13\u753B\u50CF-jpg";
    String url = "/resource/image/20/transformName/100/100/91d18d69d55180b2649f48647c7000ed/pu/" + japaneseName + ".jpg";

    CMPicture picture = contentTestHelper.getContentBean(20);
    when(transformedBlob.getBean()).thenReturn(picture);
    when(transformedBlob.getOriginal()).thenReturn(picture.getContent().getBlobRef("data"));

    ModelAndView modelAndView = handleRequest(url);
    assertModel(modelAndView, transformedBlob);
  }

  /**
   * Test "not found" when one of the hash-protected URL segments is modified:
   * {@link TransformedBlobHandler#handleRequest}
   */
  @Test
  public void testMessingWithProtectedURLParts() throws Exception {
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));
    assertNotFound("extension", handleRequest("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.png"));
    assertNotFound("name", handleRequest("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg-broken.jpg"));
    assertNotFound("digest", handleRequest("/resource/image/16/transformName/100/100/digest/xxxxx/nae-me-jpg.jpg"));
    assertNotFound("width", handleRequest("/resource/image/16/transformName/101/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"));
    assertNotFound("height", handleRequest("/resource/image/16/transformName/100/101/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"));
    assertNotFound("transform", handleRequest("/resource/image/16/invalid/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"));
    assertNotFound("id", handleRequest("/resource/image/18/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg"));
    assertNotFound("hash", handleRequest("/resource/image/16/transformName/100/100/digest/XXX/nae-me-jpg.jpg"));

    verify(transformImageService, never()).transformWithDimensions(any(Content.class), anyString(), anyString(), anyInt(), anyInt(), anyString());
  }

  /**
   * Test URL generation:
   * {@link TransformedBlobHandler#buildLink(com.coremedia.transform.TransformedBeanBlob, java.util.Map)}
   */
  @Test
  public void testGenerateLink() {
    CMPicture picture = contentTestHelper.getContentBean(16);
    Blob transformedData = picture.getTransformedData(TRANSFORM_NAME);
    when(transformedBlob.getBean()).thenReturn(picture);
    when(transformedBlob.getOriginal()).thenReturn(picture.getContent().getBlobRef("data"));

    String link = formatLink(transformedData, null, false, Map.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT
    ));
    assertThat(link).isEqualTo("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/uY/nae-me-jpg.jpg");
  }

  /**
   * Test URL generation with target MIME type:
   * {@link TransformedBlobHandler#buildLink(com.coremedia.transform.TransformedBeanBlob, java.util.Map)}
   */
  @Test
  public void testGenerateLinkWithTargetMimeType() {
    CMPicture picture = contentTestHelper.getContentBean(16);
    Blob transformedData = picture.getTransformedData(TRANSFORM_NAME);
    when(transformedBlob.getBean()).thenReturn(picture);
    when(transformedBlob.getOriginal()).thenReturn(picture.getContent().getBlobRef("data"));

    String link = formatLink(transformedData, null, false, Map.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT,
            TransformedBlobHandler.TARGET_MIME_TYPE, "image/png"
    ));
    assertThat(link).isEqualTo("/resource/image/16/transformName/100/100/4d1b8ae7b7c0102fbdd80b5ea41b45c5/FQ/nae-me-jpg.png");
  }

  /**
   * BARBUDA-2590: to fix this bug, the link generator will use the original extension instead of the
   * correct extension of the transformed blob, because generating the link should not actually require the transformation
   * to be executed.
   */
  @Test
  public void useOriginalExtensionWhenTransformationChangesExtension() throws MimeTypeParseException {
    // in this test case, the original JPEG is transformed to a PNG
    when(transformedBlob.getContentType()).thenReturn(new MimeType("image/png"));
    when(transformedBlob.getBean()).thenReturn(contentTestHelper.getContentBean(16));
    when(transformedBlob.getOriginal()).thenReturn(contentTestHelper.getContent(16).getBlobRef("data"));

    // expect extension .jpg, even though the transformed blob has a different extension
    assertThat(formatLink(transformedBlob, null, false, Map.of(
            TransformedBlobHandler.WIDTH_SEGMENT, WIDTH,
            TransformedBlobHandler.HEIGHT_SEGMENT, HEIGHT
    ))).isEqualTo("/resource/image/16/transformName/100/100/digest/tw/nae-me-jpg.jpg");
  }

  /**
   * BARBUDA-2590: generating a link for a transformed blob must not access its size, contents, or content type,
   * as this would trigger the costly transformation to be performed. Link generation must be inexpensive.
   */
  @Test
  public void linkGenerationDoesNotTriggerTransformation() throws IOException {
    testGenerateLink();

    // make sure the "expensive" methods triggering the transformation are never called
    verify(transformedBlob, never()).asBytes();
    verify(transformedBlob, never()).getInputStream();
    verify(transformedBlob, never()).writeOn(any(OutputStream.class));
    verify(transformedBlob, never()).getContentType();
    verify(transformedBlob, never()).getSize();
  }

  // --- internal ---------------------------------------------------

  private ModelAndView handleRequest(String path) throws Exception {
    return performMockRequest(path)
            .andExpect(mvcResult -> assertThat(mvcResult.getResponse().getHeader("Cache-Control")).isNull())
            .andReturn().getModelAndView();
  }

  private ResultActions performMockRequest(String path) throws Exception {
    MockHttpServletRequestBuilder req = MockMvcRequestBuilders
            .get(path)
            .requestAttr(ATTR_NAME_CMNAVIGATION, contentTestHelper.getContentBean(4))
            .characterEncoding("UTF-8");
    return mockMvc.perform(req);
  }

  private void assertNotFound(@NonNull String message, ModelAndView modelAndView) {
    assertThat(modelAndView)
            .as(message)
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOfSatisfying(HttpError.class,
                    e -> assertThat(e)
                            .returns(HttpServletResponse.SC_NOT_FOUND, from(HttpError::getErrorCode)));
  }

  private void assertModel(ModelAndView modelAndView, Object bean) {
    assertThat(modelAndView)
            .extracting(HandlerHelper::getRootModel)
            .isEqualTo(bean);
  }

  protected String formatLink(Object bean, String viewName, boolean forRedirect, Map<String, Object> parameters) {
    MockHttpServletRequest request = newRequest(emptyMap());
    request.setAttribute(RequestUtils.PARAMETERS, new HashMap<>(parameters));
    return linkFormatter.formatLink(bean, viewName, request, new MockHttpServletResponse(), forRedirect);
  }

  private MockHttpServletRequest newRequest(Map<String, String> parameters) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    request.setParameters(parameters);
    request.setCharacterEncoding("UTF-8");
    return request;
  }
}
