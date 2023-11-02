package com.coremedia.blueprint.cae.config;

import com.coremedia.blueprint.cae.richtext.filter.AppendClassToElementFilter;
import com.coremedia.blueprint.cae.richtext.filter.CMDownloadLinkValidationFilter;
import com.coremedia.blueprint.cae.richtext.filter.ConfigurableRichtextToHtmlFilterFactory;
import com.coremedia.blueprint.cae.richtext.filter.FilterFactory;
import com.coremedia.blueprint.cae.richtext.filter.ImageFilter;
import com.coremedia.blueprint.cae.richtext.filter.ImgCompletionFilter;
import com.coremedia.blueprint.cae.richtext.filter.LangAttributeFilter;
import com.coremedia.blueprint.cae.richtext.filter.LinkEmbedFilter;
import com.coremedia.blueprint.cae.richtext.filter.LinkValidationFilter;
import com.coremedia.blueprint.cae.richtext.filter.ReservedClassToElementConfig;
import com.coremedia.blueprint.cae.richtext.filter.ReservedClassToElementFilter;
import com.coremedia.blueprint.cae.richtext.filter.UnsurroundFilter;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cms.delivery.configuration.DeliveryConfigurationProperties;
import com.coremedia.id.IdProvider;
import com.coremedia.id.IdServicesConfiguration;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.view.config.CaeViewServicesConfiguration;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.springframework.customizer.Customize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@Import({
        IdServicesConfiguration.class,
        CaeViewServicesConfiguration.class,
})
public class BlueprintRichtextFiltersConfiguration {

  /**
   * Predefined programmed view for markup of grammar "coremedia-richtext-1.0"
   */
  @Bean(autowireCandidate = false)
  @Customize(value = "richtextMarkupView.xmlFilterFactory", mode = Customize.Mode.REPLACE)
  @Order(10_000)
  ConfigurableRichtextToHtmlFilterFactory configurableRichtextToHtmlFilterFactory(IdProvider idProvider,
                                                                                  LinkFormatter linkFormatter,
                                                                                  FilterFactory linkValidationFilter,
                                                                                  FilterFactory cmDownloadLinkValidationFilter,
                                                                                  FilterFactory imgCompletionFilter,
                                                                                  FilterFactory linkEmbedFilter,
                                                                                  FilterFactory imageFilter,
                                                                                  FilterFactory reservedClassToElementFilter,
                                                                                  FilterFactory appendClassToElementFilter,
                                                                                  FilterFactory unsurroundFilter,
                                                                                  FilterFactory langAttributeFilter) {
    ConfigurableRichtextToHtmlFilterFactory factory = new ConfigurableRichtextToHtmlFilterFactory();
    factory.setIdProvider(idProvider);
    factory.setLinkFormatter(linkFormatter);
    factory.setXmlFiltersBeforeUriFormatter(List.of(
            linkValidationFilter,
            cmDownloadLinkValidationFilter,
            imgCompletionFilter,
            linkEmbedFilter,
            imageFilter
    ));
    factory.setXmlFilters(List.of(
            reservedClassToElementFilter,
            appendClassToElementFilter,
            langAttributeFilter,
            unsurroundFilter
    ));
    return factory;
  }

  /**
   * Suppresses links to CMDownloads not containing blob data.
   */
  @Bean
  LinkValidationFilter linkValidationFilter(IdProvider idProvider,
                                            DataViewFactory dataViewFactory,
                                            ValidationService<Object> validationService,
                                            DeliveryConfigurationProperties deliveryConfigurationProperties) {
    LinkValidationFilter filter = new LinkValidationFilter();
    filter.setIdProvider(idProvider);
    filter.setDataViewFactory(dataViewFactory);
    filter.setValidationService(validationService);
    filter.setRenderLinkText(false);
    filter.setPreviewMode(deliveryConfigurationProperties.isPreviewMode());
    return filter;
  }

  /**
   * Suppresses links to invalid content.
   */
  @Bean
  CMDownloadLinkValidationFilter cmDownloadLinkValidationFilter(IdProvider idProvider,
                                                                DataViewFactory dataViewFactory,
                                                                ValidationService validationService,
                                                                DeliveryConfigurationProperties deliveryConfigurationProperties) {
    CMDownloadLinkValidationFilter filter = new CMDownloadLinkValidationFilter();
    filter.setIdProvider(idProvider);
    filter.setDataViewFactory(dataViewFactory);
    filter.setValidationService(validationService);
    filter.setPreviewMode(deliveryConfigurationProperties.isPreviewMode());
    return filter;
  }

  /**
   * Amends missing img attributes.
   */
  @Bean
  ImgCompletionFilter imgCompletionFilter(IdProvider idProvider,
                                          ContentBeanFactory contentBeanFactory) {
    ImgCompletionFilter filter = new ImgCompletionFilter();
    filter.setIdProvider(idProvider);
    filter.setContentBeanFactory(contentBeanFactory);
    return filter;
  }

  /**
   * Enables embedding of images by rendering theses documents with the view "asRichtextEmbed".
   */
  @Bean
  ImageFilter imageFilter(ContentRepository contentRepository,
                          ContentBeanFactory contentBeanFactory,
                          DataViewFactory dataViewFactory,
                          ValidationService validationService) {
    ImageFilter filter = new ImageFilter();
    filter.setContentRepository(contentRepository);
    filter.setContentBeanFactory(contentBeanFactory);
    filter.setDataViewFactory(dataViewFactory);
    filter.setValidationService(validationService);
    return filter;
  }

  /**
   * Enables embedding of linked documents by rendering theses documents with the view "asRichttextEmbed".
   */
  @Bean
  LinkEmbedFilter linkEmbedFilter(IdProvider idProvider,
                                  DataViewFactory dataViewFactory) {
    LinkEmbedFilter filter = new LinkEmbedFilter();
    filter.setIdProvider(idProvider);
    filter.setDataViewFactory(dataViewFactory);
    return filter;
  }

  @Bean
  AppendClassToElementFilter appendClassToElementFilter() {
    AppendClassToElementFilter filter = new AppendClassToElementFilter();
    filter.setElementList(Map.of(
            "ul", "rte--list",
            "ol", "rte--list"
    ));
    return filter;
  }

  /**
   * <p>
   * Converts given elements with given marker class to corresponding HTML
   * element (stripping the marker class).
   * </p>
   * <p>
   * <strong>Take care of valid HTML:</strong>
   * Note, that you should ensure that mapped target elements should share the
   * same valid context (parent, children, attributes) as the originating
   * element. Thus, for example, replacing {@code <span class="body">} by
   * {@code <body>} most likely will provide invalid HTML (which browsers may
   * still tolerate, though).
   * </p>
   *
   * @return filter
   */
  @Bean
  ReservedClassToElementFilter reservedClassToElementFilter() {
    return new ReservedClassToElementFilter(List.of(
            // <span class="code"> -> <code>
            ReservedClassToElementConfig.of("span", "code"),
            // <span class="strike"> -> <s>
            ReservedClassToElementConfig.of("span", "strike", "s"),
            // <span class="underline"> -> <u>
            ReservedClassToElementConfig.of("span", "underline", "u"),
            // <td class="td--header"> -> <th>
            ReservedClassToElementConfig.of("td", "td--header", "th"),
            // <p class="p--heading-X"> -> <hX>
            ReservedClassToElementConfig.of("p", "p--heading-1", "h1"),
            ReservedClassToElementConfig.of("p", "p--heading-2", "h2"),
            ReservedClassToElementConfig.of("p", "p--heading-3", "h3"),
            ReservedClassToElementConfig.of("p", "p--heading-4", "h4"),
            ReservedClassToElementConfig.of("p", "p--heading-5", "h5"),
            ReservedClassToElementConfig.of("p", "p--heading-6", "h6"),
            // <p class="p--standard"> -> <p>
            ReservedClassToElementConfig.of("p", "p--standard", "p"),
            // <p class="p--pre"> -> <pre>
            // should be obsolete as CoreMedia Rich Text supports <pre> but mapping exists since ages.
            ReservedClassToElementConfig.of("p", "p--pre", "pre")
    ));
  }

  /**
   * <p>
   * Filter instance for mapping {@code lang} and {@code xml:lang} attributes
   * to their preferred representation as {@code lang}.
   * </p>
   * <p>
   * Filter also applies normalization to the language tags, as, for example,
   * replacing so-called "grandfathered" language tags.
   * </p>
   *
   * @return filter
   */
  @Bean
  LangAttributeFilter langAttributeFilter() {
    return new LangAttributeFilter();
  }

  /**
   * Removes surrounding divs from coremedia-richtext-1.0.
   */
  @Bean
  UnsurroundFilter unsurroundFilter() {
    return new UnsurroundFilter();
  }
}
