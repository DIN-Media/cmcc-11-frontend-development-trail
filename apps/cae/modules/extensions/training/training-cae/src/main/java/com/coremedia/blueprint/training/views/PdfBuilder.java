package com.coremedia.blueprint.training.views;

import com.coremedia.cap.common.Blob;
import com.coremedia.xml.Markup;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A utility class for rendering content as PDF.
 */
public class PdfBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PdfBuilder.class);

  private static Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
  private static Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12);

  private String title;
  private Markup text;
  private Blob picture;

  /**
   * Set the PDF title.
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Set the text for the PDF
   * @param text
   */
  public void setText(Markup text) {
    this.text = text;
  }

  /**
   * Set the picture blob object for the PDF.
   *
   * @param pictureBlob
   */
  public void setPicture(Blob pictureBlob) {
    this.picture = pictureBlob;
  }


  /**
   * Write the PDF onto the given HttpServletResponse.
   *
   * This method also sets the responses content type to "application/pdf".
   * You still need to set the response status to 200 or other after calling this method.
   *
   * Make sure that you added title, text and picture before calling this method.
   *
   * @param response - the HttpServletResponse.
   * @throws DocumentException
   * @throws IOException
   */
  public void writeOn(HttpServletResponse response) throws DocumentException, IOException{
    response.setContentType("application/pdf");
    response.setCharacterEncoding("iso-8859-1"); // BUGFIX: CMS-13338
    this.writeOn(response.getOutputStream());
  }

  /**
   * Write the PDF onto the given output stream.
   *
   * Make sure that you added title, text and picture before calling this method.
   *
   * @param out
   * @throws DocumentException
   * @throws IOException
   */
  public void writeOn(OutputStream out) throws DocumentException, IOException {
    Document document = new Document();
    try {
      PdfWriter pdfWriter = PdfWriter.getInstance(document, out);
      pdfWriter.setPdfVersion(PdfWriter.VERSION_1_6);

      if (title!=null) {
        document.addTitle(title);
      }
      document.open();

      if (title!=null) {
        Paragraph paragraph = new Paragraph(title, TITLE_FONT);
        document.add(paragraph);
      }

      if (picture!=null) {
        Image pdfImage = Image.getInstance(picture.asBytes());
        pdfImage.setAlignment(Image.ALIGN_RIGHT | Image.TEXTWRAP);
        pdfImage.scaleToFit(300, 300);
        document.add(pdfImage);
      }

      if (text!=null) {
        text.writeOn(new RichtTextToPdfHandler(document));
      }

    } catch (Exception e) {
      LOG.error("Failed to write PDF.", e);
    } finally {
      if (document.isOpen()) {
        document.close();
      }
    }
  }

  private static class RichtTextToPdfHandler extends DefaultHandler {

    private static final char NO_BREAK_SPACE = '\u00a0';
    private static final char WHITESPACE = ' ';

    protected Document document;
    protected Paragraph paragraph = null;
    protected List list = null;

    public RichtTextToPdfHandler(Document document) {
      this.document = document;
    }

    @Override
    public void characters(char[] data, int offset, int count)
            throws SAXException {
      if (paragraph != null) {
        String text = String.copyValueOf(data, offset, count);
        text = text.replace(NO_BREAK_SPACE, WHITESPACE); // fixing &nbsp; in text...
        paragraph.add(text);
      }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      if ("p".equals(localName)) {
        String cls = atts.getValue("class");
        if (cls == null) {
          // create a default paragraph...
          openParagraph(TEXT_FONT, 4);
        } else if ("p--heading-1".equals(cls)) {
          openParagraph(TITLE_FONT, 18);
        } else if ("p--heading-2".equals(cls) || cls.startsWith("p--heading")) {
          openParagraph(SUBTITLE_FONT, 12);
        } else {
          // create a default paragraph...
          openParagraph(TEXT_FONT, 6);
        }
      }
      else if ("ul".equals(localName)) {
        list = new List(false);
        list.setListSymbol("\u2022 ");
      }
      else if ("ol".equals(localName)) {
        list = new List(true);
      }
      else if ("li".equals(localName)) {
        paragraph = new ListItem("", TEXT_FONT);
      }

    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
      try {
        if ("p".equals(localName)) {
          closeParagraph();
        }
        else if ("br".equals(localName)) {
          addLineBreak();
        }
        else if ("ul".equals(localName) || "ol".equals(localName)) {
          if (list!=null) {
            document.add(list);
            list = null;
          }
        }
        else if ("li".equals(localName)) {
          if (list!=null && paragraph!=null) {
            list.add(paragraph);
            paragraph = null;
          }
        }
      } catch (DocumentException ex) {
        throw new SAXException(ex);
      }
    }

    private void addLineBreak() throws DocumentException {
      if (paragraph!=null) {
        Font currentFont = paragraph.getFont();
        closeParagraph();
        openParagraph(currentFont, 0);
      }
    }

    private void openParagraph(Font font, float spacingBefore) {
      paragraph = new Paragraph("", font);
      paragraph.setSpacingBefore(spacingBefore);
    }

    private void closeParagraph() throws DocumentException {
      if (paragraph != null) {
        document.add(paragraph);
        paragraph = null;
      }
    }

  }
}
