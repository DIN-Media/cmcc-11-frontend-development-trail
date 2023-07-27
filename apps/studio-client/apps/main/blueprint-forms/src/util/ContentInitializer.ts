import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import Struct from "@coremedia/studio-client.cap-rest-client/struct/Struct";
import TeaserOverlayManager from "@coremedia/studio-client.main.teaser-overlay-components/TeaserOverlayManager";
import Actions_properties from "@coremedia/studio-client.main.editor-components/sdk/actions/Actions_properties";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Site from "@coremedia/studio-client.multi-site-models/Site";
import Ext from "@jangaroo/ext-ts";
import { as, cast, is } from "@jangaroo/runtime";
import int from "@jangaroo/runtime/int";
import joo from "@jangaroo/runtime/joo";
import TeaserOverlayConstants from "../TeaserOverlayConstants";
import StringPropertyDescriptor
  from "@coremedia/studio-client.cap-rest-client/common/descriptors/StringPropertyDescriptor";

/**
 * Initializer settings for the blueprint project.
 */
class ContentInitializer {

  /**
   * Feature flag to enable teaser overlay by default.
   */
  static readonly #ENABLE_TEASER_OVERLAY_BY_DEFAULT: boolean = false;

  /**
   * The registration of the initializers for the corresponding content types.
   */
  static applyInitializers(): void {
    editorContext._.registerContentInitializer("CMArticle", ContentInitializer.#initArticle);
    editorContext._.registerContentInitializer("CMAudio", ContentInitializer.#initAudio);
    editorContext._.registerContentInitializer("CMChannel", ContentInitializer.initChannel);
    editorContext._.registerContentInitializer("CMCollection", ContentInitializer.#initCollection);
    editorContext._.registerContentInitializer("CMDownload", ContentInitializer.#initTeasable);
    editorContext._.registerContentInitializer("CMImageMap", ContentInitializer.#initCMImageMap);
    editorContext._.registerContentInitializer("CMLinkable", ContentInitializer.initCMLinkable);
    editorContext._.registerContentInitializer("CMLocalized", ContentInitializer.initCMLocalized);
    editorContext._.registerContentInitializer("CMTaxonomy", ContentInitializer.#initTaxonomy);
    editorContext._.registerContentInitializer("CMLocTaxonomy", ContentInitializer.#initTaxonomy);
    editorContext._.registerContentInitializer("CMMedia", ContentInitializer.#initTeasable);
    editorContext._.registerContentInitializer("CMPicture", ContentInitializer.#initPicture);
    editorContext._.registerContentInitializer("CMQueryList", ContentInitializer.#initQueryList);
    editorContext._.registerContentInitializer("CMTeasable", ContentInitializer.#initTeaser);
    editorContext._.registerContentInitializer("CMViewtype", ContentInitializer.#initViewType);
    editorContext._.registerContentInitializer("CMVideo", ContentInitializer.#initVideo);
    editorContext._.registerContentInitializer("CMSpinner", ContentInitializer.#initSpinner);
    editorContext._.registerContentInitializer("CMFolderProperties", ContentInitializer.#initFolderProperties);
    editorContext._.registerContentInitializer("CMTheme", Ext.emptyFn);
  }

  static #initFolderProperties(content: Content): void {
    content.rename("_folderProperties");
    ContentInitializer.initCMLocalized(content);
  }

  static #initSpinner(content: Content): void {
    const localSettings: Struct = content.getProperties().get("localSettings");
    localSettings.getType().addStructProperty("commerce");
    const commerceStruct: Struct = localSettings.get("commerce");
    commerceStruct.getType().addBooleanProperty("inherit", true);
    ContentInitializer.initCMLinkable(content);
  }

  static #initViewType(content: Content): void {
    ContentInitializer.initCMLocalized(content);
    ContentInitializer.initializePropertyWithName(content, "layout");
  }

  static #initQueryList(content: Content): void {
    const localSettings: Struct = content.getProperties().get("localSettings");
    localSettings.getType().addIntegerProperty("limit", 10);

    //should be part of the query editor, but since the content types are set for each condition we've added the default init here
    const documentTypes = ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"];
    const struct: Struct = content.getProperties().get("localSettings");
    struct.getType().addStructProperty("fq");
    const fq: Struct = struct.get("fq");
    fq.getType().addStringProperty("documenttype", int.MAX_VALUE, documentTypes.join(","));

    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  static #initTeaser(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "teaserTitle");
    ContentInitializer.#initializeTeaserOverlay(content);
    ContentInitializer.initCMLinkable(content);
  }

  static #initPicture(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "title");
    ContentInitializer.initializePropertyWithName(content, "alt");
    ContentInitializer.initCMLinkable(content);
  }

  static #initAudio(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "title");
    ContentInitializer.initCMLinkable(content);
  }

  static #initVideo(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "title");
    ContentInitializer.initCMLinkable(content);
  }

  static #initTeasable(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "title");
    ContentInitializer.#initializeTeaserOverlay(content);
    ContentInitializer.initCMLinkable(content);
  }

  static #initCollection(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "teaserTitle");
    ContentInitializer.initCMLinkable(content);
  }

  static #initTaxonomy(content: Content): void {
    ContentInitializer.#initTeasable(content);
    ContentInitializer.initializePropertyWithName(content, "value");
  }

  static #initArticle(content: Content): void {
    if (as(content.getProperties().get("title"), String).length < 1) {
      ContentInitializer.initializePropertyWithName(content, "title");
    }
    ContentInitializer.#initializeTeaserOverlay(content);
    ContentInitializer.initCMLinkable(content);
  }

  static initChannel(content: Content): void {
    ContentInitializer.initializePropertyWithName(content, "title");
    ContentInitializer.initializePropertyWithName(content, "segment");
    ContentInitializer.initCMLinkable(content);
  }

  static initCMLocalized(content: Content): void {
    const sitesService = editorContext._.getSitesService();
    const site: Site = sitesService.getSiteFor(content) || sitesService.getPreferredSite();
    let locale: string;
    if (site) {
      locale = site.getLocale().getLanguageTag();
    } else {
      locale = joo.localeSupport.getLocale();
    }
    ContentInitializer.setProperty(content, "locale", locale);
  }

  static initCMLinkable(content: Content): void {
    ContentInitializer.initCMLocalized(content);
  }

  static #initCMImageMap(content: Content): void {
    ContentInitializer.initCMLinkable(content);
    const localSettings: Struct = content.getProperties().get("localSettings");
    localSettings.getType().addStructProperty("overlay");
    const overlay = localSettings.get("overlay");
    overlay.set("displayTitle", true);
  }

  static initializePropertyWithName(content: Content, property: string): void {
    //Only initialize if the name of the content is not "New content item"
    let value = content.getName();
    if (value != Actions_properties.Action_newContent_newDocumentName_text) {
      const descriptor = content.getType().getDescriptor(property);
      if(is(descriptor, StringPropertyDescriptor) && value.length > as(descriptor, StringPropertyDescriptor).length) {
        value = value.substring(0, as(descriptor, StringPropertyDescriptor).length);
      }
      ContentInitializer.setProperty(content, property, value);
    }
  }

  static #initializeTeaserOverlay(content: Content): void {
    // check feature flag
    if (!ContentInitializer.#ENABLE_TEASER_OVERLAY_BY_DEFAULT) {
      return;
    }
    const manager = TeaserOverlayManager.getInstance();
    manager.initializeTeaserOverlay(
      content,
      TeaserOverlayConstants.DEFAULT_SETTINGS_PATH,
      TeaserOverlayConstants.DEFAULT_STYLE_DESCRIPTOR_FOLDER_PATHS,
      TeaserOverlayConstants.DEFAULT_STYLE_NAME,
    );
  }

  static setProperty(content: Content, property: string, value: any): void {
    const properties = content.getProperties();
    properties.set(property, value);
    content.flush();
  }
}

export default ContentInitializer;
