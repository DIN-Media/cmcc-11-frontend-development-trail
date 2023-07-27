import ContentInitializer from "@coremedia-blueprint/studio-client.main.blueprint-forms/util/ContentInitializer";
import Store from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Store";
import ECommerceStudioPlugin_properties from "@coremedia-blueprint/studio-client.main.ec-studio/ECommerceStudioPlugin_properties";
import catalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/catalogHelper";
import CatalogPreferencesBase from "@coremedia-blueprint/studio-client.main.ec-studio/components/preferences/CatalogPreferencesBase";
import CatalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/helper/CatalogHelper";
import StoreUtil from "@coremedia-blueprint/studio-client.main.ec-studio/helper/StoreUtil";
import contentTreeRelationRegistry from "@coremedia/studio-client.cap-base-models/content/contentTreeRelationRegistry";
import UserUtil from "@coremedia/studio-client.cap-base-models/util/UserUtil";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ContentType from "@coremedia/studio-client.cap-rest-client/content/ContentType";
import ContentTypeNames from "@coremedia/studio-client.cap-rest-client/content/ContentTypeNames";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import PreferenceWindow from "@coremedia/studio-client.ext.frame-components/preferences/PreferenceWindow";
import MessageBoxUtilInternal from "@coremedia/studio-client.ext.ui-components/messagebox/MessageBoxUtilInternal";
import SearchState from "@coremedia/studio-client.library-services-api/SearchState";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import StudioPlugin from "@coremedia/studio-client.main.editor-components/configuration/StudioPlugin";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import IEditorContext from "@coremedia/studio-client.main.editor-components/sdk/IEditorContext";
import CollectionViewConstants from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionViewConstants";
import CollectionViewManagerInternal from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionViewManagerInternal";
import RepositoryTreeDragDropModel from "@coremedia/studio-client.main.editor-components/sdk/collectionview/tree/RepositoryTreeDragDropModel";
import StudioPreferenceWindow from "@coremedia/studio-client.main.editor-components/sdk/desktop/StudioPreferenceWindow";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import ReferrerListPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/ReferrerListPanel";
import MetaStyleService from "@coremedia/studio-client.main.editor-components/sdk/util/MetaStyleService";
import Site from "@coremedia/studio-client.multi-site-models/Site";
import Ext from "@jangaroo/ext-ts";
import { as, cast, is } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import { AnyFunction } from "@jangaroo/runtime/types";
import CatalogStudioPlugin from "./CatalogStudioPlugin";
import CatalogStudioPlugin_properties from "./CatalogStudioPlugin_properties";
import CatalogCollectionViewExtension from "./library/CatalogCollectionViewExtension";
import CatalogTreeRelation from "./library/CatalogTreeRelation";
import RepositoryCatalogTreeModel from "./library/RepositoryCatalogTreeModel";

interface CatalogStudioPluginBaseConfig extends Config<StudioPlugin> {
}

class CatalogStudioPluginBase extends StudioPlugin {
  declare Config: CatalogStudioPluginBaseConfig;

  constructor(config: Config<CatalogStudioPlugin> = null) {
    super(config);
  }

  override init(editorContext: IEditorContext): void {
    super.init(editorContext);

    //apply defaults
    editorContext.registerContentInitializer(CatalogTreeRelation.CONTENT_TYPE_CATEGORY, ContentInitializer.initChannel);

    CatalogStudioPluginBase.#addCatalogTreeModel();

    this.#initCatalogPreferences();

    // Colorful Studio styles
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_TURQUOISE, [CatalogTreeRelation.CONTENT_TYPE_PRODUCT]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_PURPLE, [CatalogTreeRelation.CONTENT_TYPE_CATEGORY]);
  }

  /**
   * Registers the catalog tree model and its dnd model.
   */
  static #addCatalogTreeModel(): void {
    const collectionViewManagerInternal =
            (as((editorContext._.getCollectionViewManager()), CollectionViewManagerInternal));

    const treeModel = new RepositoryCatalogTreeModel();
    collectionViewManagerInternal.addTreeModel(treeModel, new RepositoryTreeDragDropModel(treeModel));

    //add extension for custom search content types
    const catalogTreeRelation = new CatalogTreeRelation();
    const isApplicable: AnyFunction = (model: any): boolean => {
      const isCmStore = CatalogHelper.getInstance().isActiveCoreMediaStore();
      if (!isCmStore) {
        return false;
      }

      const content = as(model, Content);
      if (!content) {
        return false;
      }

      const contentType = content.getType();
      if (!contentType) {
        return undefined;
      }

      const contentTypeName = contentType.getName();
      if (!contentTypeName) {
        return undefined;
      }

      return contentTypeName === catalogTreeRelation.folderNodeType()
              || contentTypeName === catalogTreeRelation.leafNodeType();
    };
    contentTreeRelationRegistry._.addExtension(catalogTreeRelation, isApplicable, 799);
    editorContext._.getCollectionViewExtender().addExtension(new CatalogCollectionViewExtension(), isApplicable, 799);
  }

  /**
   * We have to force a reload if the catalog view settings are changed.
   * Maybe this is possible without a Studio reload in the future, but this is the easiest way to apply the setting.
   */
  #initCatalogPreferences(): void {
    //load the catalog view settings and apply it to the tree model
    let showCatalogContentPref: boolean = editorContext._.getPreferences().get(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY);
    if (showCatalogContentPref === undefined) {
      showCatalogContentPref = false;
    }

    CatalogStudioPluginBase.#applySearchSettings(showCatalogContentPref);

    //add change listener to the catalog view settings
    const preferencesVE = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext._.getPreferences());
    preferencesVE.addChangeListener((ve: ValueExpression): void => {
      const doShow: boolean = ve.getValue() || false;
      CatalogStudioPluginBase.#applySearchSettings(doShow);

      //re-initialize the selection to update the search filter combo, etc.
      const home = UserUtil.getHome();
      const cmInternal = as(editorContext._.getCollectionViewManager(), CollectionViewManagerInternal);
      const selection: Content = cmInternal.getCollectionView().getSelectedFolderValueExpression().getValue();
      cmInternal.getCollectionView().getSelectedFolderValueExpression().setValue(home);
      cmInternal.getCollectionView().getSelectedFolderValueExpression().setValue(selection);
    });
  }

  static #applySearchSettings(showCatalogContent: boolean): void {
    if (!showCatalogContent) {
      //remove the corporate catalog doctypes from the search result by default
      CatalogStudioPluginBase.#addToSearch(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      CatalogStudioPluginBase.#addToSearch(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);

      //remove the corporate catalog doctypes from the search filter by default
      CatalogStudioPluginBase.#addToSearchResult(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      CatalogStudioPluginBase.#addToSearchResult(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);
    } else {
      CatalogStudioPluginBase.#removeFromSearchResult(CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
      CatalogStudioPluginBase.#removeFromSearchResult(CatalogTreeRelation.CONTENT_TYPE_PRODUCT);
    }
  }

  static #addToSearch(contentTypeName: string): void {
    for (let i = 0; i < editorContext._.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext._.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        return;
      }
    }
    editorContext._.getContentTypesExcludedFromSearch().push(contentTypeName);
  }

  static #addToSearchResult(contentTypeName: string): void {
    for (let i = 0; i < editorContext._.getContentTypesExcludedFromSearchResult().length; i++) {
      if (editorContext._.getContentTypesExcludedFromSearchResult()[i] === contentTypeName) {
        return;
      }
    }
    editorContext._.getContentTypesExcludedFromSearchResult().push(contentTypeName);
  }

  static #removeFromSearchResult(contentTypeName: string): void {
    for (let i = 0; i < editorContext._.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext._.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        editorContext._.getContentTypesExcludedFromSearch().splice(i, 1);
        break;
      }
    }

    for (let j = 0; j < editorContext._.getContentTypesExcludedFromSearchResult().length; j++) {
      if (editorContext._.getContentTypesExcludedFromSearchResult()[j] === contentTypeName) {
        editorContext._.getContentTypesExcludedFromSearchResult().splice(j, 1);
        break;
      }
    }
  }

  static getShopExpression(config: Config<ReferrerListPanel>): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): boolean => {
      const store = cast(Store, CatalogHelper.getInstance().getStoreForContentExpression(config.bindTo).getValue());
      return store && store.getName() && CatalogHelper.getInstance().isCoreMediaStore(store);
    });
  }

  /**
   * Custom search handler for catalog link lists.
   */
  static openCatalogSearch(linkListTargetType: ContentType, sourceContent: Content): void {
    const preferencesVE = ValueExpressionFactory.create<boolean>(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext._.getPreferences());
    const showCatalogContent: boolean = preferencesVE.getValue();
    const preferredSiteId = editorContext._.getSitesService().getPreferredSiteId();
    const contentSiteId = editorContext._.getSitesService().getSiteIdFor(sourceContent);
    const contentSite = editorContext._.getSitesService().getSiteFor(sourceContent);

    let searchType = linkListTargetType.getName();
    //default supertype to all documents since we are searching inside the catalog
    if (searchType === "CMLinkable") {
      searchType = ContentTypeNames.DOCUMENT;
    }

    //open the regular catalog search if the sites are matching
    if (preferredSiteId === contentSiteId) {
      CatalogStudioPluginBase.#openSearch(contentSite, searchType, true);
    } else if (showCatalogContent) {
      CatalogStudioPluginBase.#openSearch(contentSite, searchType, false);
    } else {
      const msg = CatalogStudioPlugin_properties.Catalog_show_search_fails_for_Content;
      const buttons: Record<string, any> = {
        no: ECommerceStudioPlugin_properties.Catalog_show_preferences_button_text,
        yes: ECommerceStudioPlugin_properties.Catalog_show_switch_site_button_text,
        cancel: Editor_properties.dialog_defaultCancelButton_text,
      };

      MessageBoxUtilInternal.show(ECommerceStudioPlugin_properties.Catalog_show_in_tree_fails_title, msg, null, buttons, CatalogStudioPluginBase.#getButtonCallback(searchType, contentSite));
    }
  }

  /**
   * Somehow the open in search mode for the CollectionView does not work properly
   * when it has never been opened before. The only working solution is to set the search state and search
   * afterwards.
   * //TODO fix the library search and change this to 'editorContext._.getCollectionViewManager().openSearchForType(searchType, null, catalogRoot);'
   * @param contentSite
   * @param searchType
   * @param catalogSearch
   */
  static #openSearch(contentSite: Site, searchType: string, catalogSearch: boolean): void {
    ValueExpressionFactory.createFromFunction((): Content =>
      CatalogStudioPluginBase.getCatalogRootForSite(contentSite),
    ).loadValue((catalogRoot: Content): void => {
      const collectionViewModel = cast(EditorContextImpl, editorContext._).getCollectionViewModel();
      const state = new SearchState();
      state.contentType = searchType;

      if (catalogSearch) {
        state.folder = catalogRoot;
        collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, catalogRoot);
        editorContext._.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
      } else {
        state.folder = catalogRoot.getParent();
        collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, catalogRoot);
        editorContext._.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
      }
    });
  }

  static #getButtonCallback(linkListType: string, contentSite: Site): AnyFunction {
    return (btn: string): void => {
      if (btn === "yes") {
        CatalogStudioPluginBase.switchSite(contentSite, (cRoot: Content): void => {
          const state = new SearchState();
          state.folder = cRoot;
          state.contentType = linkListType;

          const collectionViewModel = cast(EditorContextImpl, editorContext._).getCollectionViewModel();
          collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, cRoot);

          editorContext._.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
        });
      } else if (btn === "no") {
        //show preferences
        const prefWindow: PreferenceWindow = Ext.create(StudioPreferenceWindow, { selectedTabItemId: "contentCatalogPreferences" });
        prefWindow.show();
        //open the content in library if the user enable the show as content contentCatalogPreferences
        prefWindow.on("close", (): void => {
          const preferencesVE = ValueExpressionFactory.create<boolean>(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext._.getPreferences());
          const showCatalogContent: boolean = preferencesVE.getValue();
          if (showCatalogContent) {
            CatalogStudioPluginBase.#openSearch(contentSite, linkListType, false);
          }
        });
      }
    };
  }

  /**
   * Why this hell of a method? When the studio is opened with a catalog item
   * of another site, the library has not been opened yet and the user clicks on a linklist to open
   * the catalog search for another site (assuming the user has clicked the 'Switch Site' option), then
   * the whole library has be be initialized first.
   * This means that catalog settings may not have been loaded yet.
   * @param site the new preferred site
   * @param callback the callback called when the site initialization has been finished.
   */
  static switchSite(site: Site, callback: AnyFunction): void {
    //switch site
    editorContext._.getSitesService().getPreferredSiteIdExpression().setValue(site.getId());

    //load all mandatory content before switching the view afterwards to repo or search mode
    EventUtil.invokeLater((): void =>
      ValueExpressionFactory.createFromFunction((): Content => {
        const catalogRoot = CatalogStudioPluginBase.getCatalogRootForSite(site);
        if (!catalogRoot) {
          return catalogRoot;
        }
        if (!catalogRoot.getPath()) {
          return undefined;
        }
        return catalogRoot;
      }).loadValue(callback),
    );
  }

  static getCatalogRootForSite(site: Site): Content {
    const storeForContentExpression = StoreUtil.getStoreForSiteExpression(site);
    return CatalogStudioPluginBase.getCatalogRootForStore(storeForContentExpression);
  }

  static getCatalogRootForStore(storeExpression: ValueExpression): Content {
    const activeStore: Store = storeExpression.getValue();
    if (undefined === activeStore) {
      return undefined;
    }
    if (is(activeStore, Store)) {
      const rootCategory = activeStore.getRootCategory();
      if (undefined === rootCategory) {
        return undefined;
      }
      if (catalogHelper.isCoreMediaStore(activeStore)) {
        const externalTechId = rootCategory.getExternalTechId();
        if (undefined === externalTechId) {
          return undefined;
        }
        return as(beanFactory._.getRemoteBean("content/" + externalTechId), Content);
      }
    }
    return null;
  }

  static findCoreMediaStores(): Array<any> {
    const sites = editorContext._.getSitesService().getSites();
    const result = [];
    for (const site of sites as Site[]) {
      const store: Store = StoreUtil.getStoreForSiteExpression(site).getValue();
      const isCoreMediaStore = catalogHelper.isCoreMediaStore(store);
      if (undefined === isCoreMediaStore) {
        return undefined;
      }
      if (isCoreMediaStore) {
        result.push(store);
      }
    }
    return result;
  }

  mayCreate(selection: Content, contentType: ContentType): boolean {
    if (!selection.getType().getName()) {
      return undefined;
    }

    const extension = editorContext._.getCollectionViewExtender().getExtension(selection);
    if (extension === undefined) {
      return undefined;
    }

    if (is(extension, CatalogCollectionViewExtension)) {
      return true;
    }

    const site = editorContext._.getSitesService().getSiteFor(selection);
    if (!site) {
      return false;
    }

    const store = CatalogStudioPluginBase.getStoreForSite(site);

    const cmCatalog = CatalogHelper.getInstance().isCoreMediaStore(store);
    if (!cmCatalog) {
      return false;
    }

    return CatalogStudioPluginBase.showCatalogAsContent();
  }

  static getStoreForSite(site: Site): Store {
    return StoreUtil.getStoreForSiteExpression(site).getValue();
  }

  static showCatalogAsContent(): boolean {
    const preferencesVE = ValueExpressionFactory.create<boolean>(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext._.getPreferences());
    const showCatalogContent: boolean = preferencesVE.getValue();
    return showCatalogContent;
  }
}

export default CatalogStudioPluginBase;
