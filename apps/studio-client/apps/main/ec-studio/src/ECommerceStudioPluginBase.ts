import Catalog from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Catalog";
import CatalogObjectPropertyNames
  from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogObjectPropertyNames";
import Store from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Store";
import UserUtil from "@coremedia/studio-client.cap-base-models/util/UserUtil";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import PageGridUtil from "@coremedia/studio-client.main.bpbase-pagegrid-studio-plugin/pagegrid/PageGridUtil";
import StudioPlugin from "@coremedia/studio-client.main.editor-components/configuration/StudioPlugin";
import IEditorContext from "@coremedia/studio-client.main.editor-components/sdk/IEditorContext";
import CollectionViewManagerInternal
  from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionViewManagerInternal";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import { as, cast } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import { AnyFunction } from "@jangaroo/runtime/types";
import ECommerceStudioPlugin from "./ECommerceStudioPlugin";
import augmentationService from "./augmentation/augmentationService";
import CatalogPreferencesBase from "./components/preferences/CatalogPreferencesBase";
import CatalogTreeDragDropModel from "./components/tree/impl/CatalogTreeDragDropModel";
import CatalogTreeModel from "./components/tree/impl/CatalogTreeModel";
import CatalogHelper from "./helper/CatalogHelper";
import CommerceCategoryCollectionViewStateInterceptor from "./library/CommerceCategoryCollectionViewStateInterceptor";
import CommerceCollectionViewStateInterceptor from "./library/CommerceCollectionViewStateInterceptor";
import ECommerceCollectionViewExtension from "./library/ECommerceCollectionViewExtension";
import quickSearchService from "@coremedia/studio-client.quicksearch-models/quickSearchService";
import QuickSearchServiceImpl from "@coremedia/studio-client.quicksearch-models/QuickSearchServiceImpl";
import CatalogObject from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogObject";
import QuickSearchResultListConfiguration
  from "@coremedia/studio-client.quicksearch-models/QuickSearchResultListConfiguration";
import QuickSearchCommerceResults from "./quicksearch/QuickSearchCommerceResults";

interface ECommerceStudioPluginBaseConfig extends Config<StudioPlugin> {
}

class ECommerceStudioPluginBase extends StudioPlugin {
  declare Config: ECommerceStudioPluginBaseConfig;

  static readonly #EXTERNAL_CHANNEL_TYPE: string = "CMExternalChannel";

  constructor(config: Config<ECommerceStudioPlugin> = null) {
    super(config);
  }

  override init(editorContext: IEditorContext): void {
    super.init(editorContext);

    const service = cast(QuickSearchServiceImpl, quickSearchService._);
    service.getResultLists().push(new QuickSearchResultListConfiguration(CatalogObject.name, QuickSearchCommerceResults.xtype));

    const isApplicable: AnyFunction = (): boolean => false;
    editorContext.getCollectionViewExtender().addExtension(new ECommerceCollectionViewExtension(), isApplicable);
    editorContext.registerCollectionViewStateInterceptor(new CommerceCategoryCollectionViewStateInterceptor());
    editorContext.registerCollectionViewStateInterceptor(new CommerceCollectionViewStateInterceptor());

    const collectionViewManagerInternal =
      (as((editorContext.getCollectionViewManager()), CollectionViewManagerInternal));

    const catalogTreeModel = new CatalogTreeModel();
    collectionViewManagerInternal.addTreeModel(catalogTreeModel,
      new CatalogTreeDragDropModel(catalogTreeModel));

    this.#initCatalogPreferences();

    // customize the initialization of page layouts
    PageGridUtil.defaultLayoutResolver = ECommerceStudioPluginBase.#getDefaultLayoutFromCatalogRoot;
  }

  getShopExpression(): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): string => {
      const store = cast(Store, CatalogHelper.getInstance().getActiveStoreExpression().getValue());
      return store && store.getName();
    });
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

    ECommerceStudioPluginBase.#applySearchSettings(showCatalogContentPref);

    //add change listener to the catalog view settings
    const preferencesVE = ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext._.getPreferences());
    preferencesVE.addChangeListener((ve: ValueExpression): void => {
      const doShow: boolean = ve.getValue() || false;
      ECommerceStudioPluginBase.#applySearchSettings(doShow);

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
      // remove the commerce doctypes from the search result by default
      ECommerceStudioPluginBase.#excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      ECommerceStudioPluginBase.#excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      ECommerceStudioPluginBase.#excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      ECommerceStudioPluginBase.#excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      ECommerceStudioPluginBase.#excludeFromSearch(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);

      //remove the commerce doctypes from the search filter by default
      ECommerceStudioPluginBase.#excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      ECommerceStudioPluginBase.#excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      ECommerceStudioPluginBase.#excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      ECommerceStudioPluginBase.#excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      ECommerceStudioPluginBase.#excludeFromSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);
    } else {
      ECommerceStudioPluginBase.#addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_CATEGORY);
      ECommerceStudioPluginBase.#addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_PRODUCT);
      ECommerceStudioPluginBase.#addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_ABSTRACT_CATEGORY);
      ECommerceStudioPluginBase.#addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_CHANNEL);
      ECommerceStudioPluginBase.#addToSearchResult(CatalogHelper.CONTENT_TYPE_CM_EXTERNAL_PRODUCT);
    }
  }

  static #excludeFromSearch(contentTypeName: string): void {
    for (let i = 0; i < editorContext._.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext._.getContentTypesExcludedFromSearch()[i] === contentTypeName) {
        return;
      }
    }
    editorContext._.getContentTypesExcludedFromSearch().push(contentTypeName);
  }

  static #excludeFromSearchResult(contentTypeName: string): void {
    for (let i = 0; i < editorContext._.getContentTypesExcludedFromSearch().length; i++) {
      if (editorContext._.getContentTypesExcludedFromSearchResult()[i] === contentTypeName) {
        return;
      }
    }
    editorContext._.getContentTypesExcludedFromSearchResult().push(contentTypeName);
  }

  static #addToSearchResult(contentTypeName: string): void {
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

  /**
   * A default layout resolver that can read the initial layout of a new created augmented category
   * from the current settings in catalog root. If the page is not an augmented category it returns
   * null. That means the default method is called.
   */
  static #getDefaultLayoutFromCatalogRoot(content: Content, pagegridProperty: string): Content {
    // only content of CMExternalChannel (Augmented Categories) is affected
    if (!content) {
      return undefined;
    }
    const ct = content.getType();
    if (ct === undefined) {
      return undefined;
    }
    if (ct.isSubtypeOf(ECommerceStudioPluginBase.#EXTERNAL_CHANNEL_TYPE)) {
      const category = augmentationService.getCategory(content);
      if (!category) {
        return undefined;
      }

      const catalog: Catalog = category.get(CatalogObjectPropertyNames.CATALOG);
      if (!catalog) {
        return undefined;
      }

      const rootCategory = catalog.getRootCategory();
      if (rootCategory === undefined) {
        return undefined;
      }
      if (rootCategory === null) {
        console.warn("Root category not found for catalog: " + catalog);
        return null;
      }

      const rootCategoryContent = augmentationService.getContent(rootCategory);
      if (rootCategoryContent === undefined) {
        return undefined;
      }
      if (rootCategoryContent === null) {
        console.warn("Root category is not augmented: " + rootCategory);
        return null;
      }

      const layout = PageGridUtil.getLayoutWithoutDefault(rootCategoryContent, pagegridProperty);
      if (layout === undefined) {
        return undefined;
      }
      if (layout === null) {
        console.warn("Layout not found for augmented root category: " + rootCategoryContent);
      }
      return layout;
    }
    return null;
  }

}

export default ECommerceStudioPluginBase;
