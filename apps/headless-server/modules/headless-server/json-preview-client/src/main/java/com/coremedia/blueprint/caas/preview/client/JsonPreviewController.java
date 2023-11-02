package com.coremedia.blueprint.caas.preview.client;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RestController
@OpenAPIDefinition(tags = {@Tag(name = "Json Preview Client", description = "Json Preview Controller")})
@Tag(name = "Json Preview Client", description = "Json Preview Controller")
@DefaultAnnotation(NonNull.class)
public class JsonPreviewController {
  static final String ERROR_MSG_NO_QUERY_DEFINITION = "No json preview query definition available for selected document type.";
  static final String ERROR_MSG_NO_ENTITY = "No response entity available.";

  static final String REQUEST_NOT_SUCCESSFUL = "Request not successful, check configuration of previewclient.caasserver-endpoint";

  private static final Logger LOG = LoggerFactory.getLogger(JsonPreviewController.class);
  private static final DateTimeFormatter STUDIO_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm VV");

  public static final String PREVIEW_PATH = "/preview";
  private static final String PARAM_NUMERIC_ID = "numericId";
  private static final String PARAM_ID = "id";
  private static final String PARAM_TYPE = "type";
  private static final String PARAM_SITE_ID = "siteId";
  private static final String PARAM_COMMERCE_ID = "commerceId";

  private static final String TEMPLATE_PATH = "html/previewJson.html";
  private static final String TEMPLATE_VAR_PREVIEW_JSON = "previewJson";
  private static final String TEMPLATE_VAR_ERROR = "error";
  private static final String QUERY_CONTENT = "content";
  private static final String QUERY_COMMERCE = "commerce";

  private final HttpClient httpClient;
  private final ITemplateEngine templateEngine;

  private final JsonPreviewConfigurationProperties jsonPreviewConfigurationProperties;

  private final PersistentPreviewQueries persistentPreviewQueries;
  private final ContentRepository contentRepository;
  private final SitesService sitesService;

  public JsonPreviewController(HttpClient httpClient,
                               @Qualifier("htmlTemplateEngine") TemplateEngine templateEngine, ContentRepository contentRepository,
                               SitesService sitesService,
                               JsonPreviewConfigurationProperties jsonPreviewConfigurationProperties) {
    this.httpClient = httpClient;
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
    this.templateEngine = templateEngine;
    this.jsonPreviewConfigurationProperties = jsonPreviewConfigurationProperties;
    this.persistentPreviewQueries = new PersistentPreviewQueries("/previewclient/graphql/", Stream.of(QUERY_CONTENT, QUERY_COMMERCE));
  }

  /**
   * @param request     the request
   *                    <p>
   *                    <i>Reflected XSS</i>:
   *                    Not vulnerable, the request parameter value is not passed from the user, but a reference to the request of spring.
   * @param numericId   the numeric id of the item
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable:<p>
   *                    - the numericId parameter value is checked, if is either a valid uuid or a valid
   *                    contentId, before creating an object of it, that is used for further processing.<p>
   *                    - the numericId parameter value is Json serialized and passed to a post request entity.
   * @param type        the type
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable: the type parameter value is Json serialized and passed to a post request entity.
   * @param previewDate the preview date
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable: the previewDate is transformed into a ZonedDateTime via ZonedDateTime#parse and passed to a
   *                    post request header value.
   */
  @GetMapping(PREVIEW_PATH + "/{" + PARAM_NUMERIC_ID + "}/{" + PARAM_TYPE + "}")
  @Timed
  public ResponseEntity<String> previewContent(HttpServletRequest request,
                                               @Parameter(description = "The id of the item", required = true) @PathVariable String numericId,
                                               @Parameter(description = "The type of the item", required = true) @PathVariable String type,
                                               @Parameter(description = "The preview date") @RequestParam(required = false, name = "previewDate") String previewDate) {

    Optional<AugmentedObject> augmentedObject = AugmentedObject.of(numericId, contentRepository, sitesService);
    if (augmentedObject.isPresent()) {
      // it's an augmented commerce object - serve the commerce preview
      AugmentedObject commerceObject = augmentedObject.get();
      return previewCommerceBean(request, commerceObject.getExternalId(), commerceObject.getSiteId(), previewDate);
    }

    // serve the content preview
    Map<String, String> previewRequestParams = Map.of(PARAM_ID, numericId, PARAM_TYPE, type);
    return executePreviewRequest(request, previewDate, previewRequestParams, QUERY_CONTENT);
  }

  /**
   * @param request     the request
   *                    <p>
   *                    <i>Reflected XSS</i>:
   *                    Not vulnerable, the request parameter value is not passed from the user, but a reference to the request of spring.
   * @param commerceId  the commerce id
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable, the commerceId parameter value is Json serialized and passed to a post request entity.
   * @param siteId      the site id
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable, the siteId parameter value is Json serialized and passed to a post request entity.
   * @param previewDate the preview date
   *                    <p>
   *                    <i>Reflected XSS, SSRF</i>:
   *                    Not vulnerable: the previewDate is transformed into a ZonedDateTime via ZonedDateTime#parse and passed to a
   *                    post request header value.
   */
  @GetMapping(PREVIEW_PATH)
  @Timed
  public ResponseEntity<String> previewCommerceBean(HttpServletRequest request,
                                                    @RequestParam(value = PARAM_COMMERCE_ID, required = true) String commerceId,
                                                    @RequestParam(value = PARAM_SITE_ID, required = true) String siteId,
                                                    @Parameter(description = "The preview date") @RequestParam(required = false, name = "previewDate") String previewDate) {
    Map<String, String> previewRequestParams = Map.of(PARAM_SITE_ID, siteId, PARAM_COMMERCE_ID, commerceId);
    return executePreviewRequest(request, previewDate, previewRequestParams, QUERY_COMMERCE);
  }

  private ResponseEntity<String> executePreviewRequest(HttpServletRequest request, String previewDate, Map<String, String> previewRequestParams, String queryName) {
    Context ctx = new Context();

    Optional<String> persistedQuery = persistentPreviewQueries.getPreviewQuery(queryName);
    if (persistedQuery.isEmpty()) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, ERROR_MSG_NO_QUERY_DEFINITION);
    }
    String query = persistedQuery.get();

    HttpResponse response;
    try {
      response = executePostRequest(request, jsonPreviewConfigurationProperties.getForwardHeaderNames(),
              jsonPreviewConfigurationProperties.isForwardCookies(),
              jsonPreviewConfigurationProperties.getCaasserverEndpoint(),
              previewRequestParams, query, previewDate, httpClient);

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    if (response.getEntity() == null) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, ERROR_MSG_NO_ENTITY);
    }

    try (
            InputStream is = response.getEntity().getContent();
            JsonParser parser = Json.createParser(is)) {
      parser.next();
      JsonObject jsonObject = parser.getObject();
      return getResponseEntity(ctx, TEMPLATE_VAR_PREVIEW_JSON, jsonObject.toString());
    } catch (IOException | IllegalStateException | JsonException | NoSuchElementException e) {
      return getResponseEntity(ctx, TEMPLATE_VAR_ERROR, e.getMessage());
    }
  }

  private ResponseEntity<String> getResponseEntity(Context ctx, String templateVar, Object content) {
    ctx.setVariable(templateVar, content);
    String htmlContent = processTemplate(ctx);
    return ResponseEntity.ok().body(htmlContent);
  }

  private String processTemplate(Context ctx) {
    return templateEngine.process(TEMPLATE_PATH, ctx);
  }

  /**
   * <i>Reflected XSS, SSRF</i>:
   * Not vulnerable<p>
   * - the header value is passed to a post request header.<p>
   * - the cookie value is passed to a post request header.
   */
  private static HttpResponse executePostRequest(HttpServletRequest request, List<String> forwardHeaderNames,
                                                 boolean forwardCookies, String caasServerEndpoint,
                                                 Map<String, String> variables, String query, String previewDate,
                                                 HttpClient httpClient) throws IOException {
    URI uri = getCaasServerUri(caasServerEndpoint);

    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    Map<String, Object> variableMap = variables.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
    ));
    objectBuilder = objectBuilder.add("variables", Json.createObjectBuilder(variableMap).build());
    objectBuilder = objectBuilder.add("query", query);

    StringEntity entity = new StringEntity(objectBuilder.build().toString());

    HttpPost httpPost = new HttpPost(uri);
    httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    if (StringUtils.isNotBlank(previewDate)) {
      try {
        ZonedDateTime parsedDate = ZonedDateTime.parse(previewDate, STUDIO_DATE_FORMATTER);
        String formattedDate = DateUtils.formatDate(Date.from(parsedDate.toInstant()), DateUtils.PATTERN_RFC1123);
        httpPost.addHeader("X-Preview-Date", formattedDate);
      } catch (DateTimeParseException e) {
        LOG.warn("Invalid previewDate: {}", e.getMessage());
      }
    }

    if (forwardHeaderNames != null && !forwardHeaderNames.isEmpty()) {
      forwardHeaderNames.forEach(s -> {
        String headerName = s.trim();
        String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
          httpPost.addHeader(headerName, headerValue);
          LOG.debug("Forwarding http-header {}", headerName);
        }
      });
    }

    if (forwardCookies) {
      String rawCookies = request.getHeader(HttpHeaders.COOKIE);
      if (rawCookies != null) {
        httpPost.addHeader(HttpHeaders.COOKIE, rawCookies);
        LOG.debug("Forwarding http cookies");
      }
    }

    httpPost.setEntity(entity);

    HttpResponse response = httpClient.execute(httpPost);
    if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
      throw new IOException(REQUEST_NOT_SUCCESSFUL + ": " + uri + " " + response.getStatusLine().getReasonPhrase());
    }
    return response;
  }

  private static URI getCaasServerUri(String caasServerEndpoint) {
    return UriComponentsBuilder.fromHttpUrl(caasServerEndpoint).build().toUri();
  }
}
