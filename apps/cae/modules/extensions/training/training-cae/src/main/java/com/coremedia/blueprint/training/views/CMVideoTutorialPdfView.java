package com.coremedia.blueprint.training.views;

import com.coremedia.blueprint.training.contentbeans.CMVideoTutorial;
import com.coremedia.objectserver.view.ServletView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CMVideoTutorialPdfView implements ServletView<CMVideoTutorial> {

  private static Logger LOG = LoggerFactory.getLogger(CMVideoTutorialPdfView.class);

  @Override
  public void render(CMVideoTutorial self, String view, HttpServletRequest request, HttpServletResponse response) {
    PdfBuilder pdfBuilder = new PdfBuilder();

    pdfBuilder.setTitle(self.getTitle());
    pdfBuilder.setText(self.getDetailText());
    if (self.getPicture()!=null) {
      pdfBuilder.setPicture(self.getPicture().getData());
    }

    try {
      pdfBuilder.writeOn(response);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception ex) {
      LOG.error(String.format("Failed to render CMVideoTutorial %d as PDF", self.getContentId()), ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
}
