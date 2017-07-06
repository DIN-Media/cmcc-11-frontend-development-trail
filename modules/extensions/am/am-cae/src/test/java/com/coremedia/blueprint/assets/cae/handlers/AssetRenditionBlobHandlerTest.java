package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.cae.handlers.CapBlobHandler;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionBlobHandlerTest {

  @InjectMocks
  private AssetRenditionBlobHandler assetRenditionBlobHandler;

  @Mock
  private CapBlobHandler capBlobHandler;

  @Mock
  private AMAsset asset;

  @Mock
  private AMAssetRendition amAssetRendition;

  @Mock
  private AMAssetRendition amAssetRenditionWithoutBlob;

  @Mock
  private CapBlobRef capBlobRef;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ModelAndView modelAndView;

  @Before
  public void setUp() {
    when(amAssetRendition.getBlob()).thenReturn(capBlobRef);
    when(capBlobHandler.linkParameters(any(CapBlobRef.class))).thenReturn(Collections.<String, String>emptyMap());
    when(capBlobHandler.handleRequest(any(ContentBean.class), anyString(), anyString(), anyString(), any(WebRequest.class))).thenReturn(modelAndView);
  }


  @Test
  public void handleAssetRenditionRequest_validRendition_delegatedToCapBlobHandler() {
    String requestedRendition = "web";

    when(asset.getPublishedRenditions()).thenReturn(Collections.singletonList(amAssetRendition));
    when(amAssetRendition.getName()).thenReturn(requestedRendition);

    ModelAndView result = assetRenditionBlobHandler.handleAssetRenditionRequest(asset, null, requestedRendition, null, null, response);

    assertNotNull("modelAndView should never be null for a valid request", modelAndView);
    assertEquals("modelAndView should be the expected mock", modelAndView, result);
  }

  @Test
  public void handleAssetRenditionRequest_invalidRendition_returnsNotFound() {
    String requestedRendition = "web";

    when(asset.getPublishedRenditions()).thenReturn(Collections.singletonList(amAssetRendition));
    when(amAssetRendition.getName()).thenReturn("print");

    ModelAndView result = assetRenditionBlobHandler.handleAssetRenditionRequest(asset, null, requestedRendition, null, null, response);

    assertNotNull("modelAndView should never be null", modelAndView);
    assertEquals("modelAndView should be the notFound-ModelAndView", HandlerHelper.notFound().getModel(), result.getModel());
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildLink_validBean_assetWithoutBlob() {
    when(capBlobHandler.linkParameters(any(CapBlobRef.class))).thenReturn(Collections.singletonMap(UriConstants.Segments.SEGMENT_NAME, "asset-name"));
    assetRenditionBlobHandler.buildRenditionLink(amAssetRenditionWithoutBlob);
  }

}