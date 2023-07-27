import TimeZones_properties from "@coremedia/studio-client.base-models/TimeZones_properties";
import ContentTypes_properties from "@coremedia/studio-client.cap-base-models/content/ContentTypes_properties";
import session from "@coremedia/studio-client.cap-rest-client/common/session";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import Validators_properties
  from "@coremedia/studio-client.ext.errors-validation-components/validation/Validators_properties";
import LocalComboBox from "@coremedia/studio-client.ext.ui-components/components/LocalComboBox";
import AddArrayItemsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/AddArrayItemsPlugin";
import NestedRulesPlugin from "@coremedia/studio-client.ext.ui-components/plugins/NestedRulesPlugin";
import SearchState from "@coremedia/studio-client.library-services-api/SearchState";
import PageGridLayouts_properties
  from "@coremedia/studio-client.main.bpbase-pagegrid-studio-plugin/pagegrid/PageGridLayouts_properties";
import PlacementLinkListPropertyField
  from "@coremedia/studio-client.main.bpbase-pagegrid-studio-plugin/pagegrid/PlacementLinkListPropertyField";
import PictureUtilsPlugin from "@coremedia/studio-client.main.bpbase-studio-components/pictures/PictureUtilsPlugin";
import AddPreviewDateSelectorButtonPlugin
  from "@coremedia/studio-client.main.bpbase-studio-components/previewdate/AddPreviewDateSelectorButtonPlugin";
import AddPlacementHighlightButtonPlugin
  from "@coremedia/studio-client.main.bpbase-studio-components/previewhighlighting/AddPlacementHighlightButtonPlugin";
import Viewtypes_properties
  from "@coremedia/studio-client.main.bpbase-studio-components/viewtypes/Viewtypes_properties";
import DeviceTypes_properties from "@coremedia/studio-client.main.editor-components/DeviceTypes_properties";
import AddDependingParameterPreviewUrlTransformer
  from "@coremedia/studio-client.main.editor-components/configuration/AddDependingParameterPreviewUrlTransformer";
import AddParameterPreviewUrlTransformer
  from "@coremedia/studio-client.main.editor-components/configuration/AddParameterPreviewUrlTransformer";
import ConfigureDefaultRichTextImageDocumentType
  from "@coremedia/studio-client.main.editor-components/configuration/ConfigureDefaultRichTextImageDocumentType";
import ConfigureDocumentTypes
  from "@coremedia/studio-client.main.editor-components/configuration/ConfigureDocumentTypes";
import CopyResourceBundleProperties
  from "@coremedia/studio-client.main.editor-components/configuration/CopyResourceBundleProperties";
import RegisterLibraryTreeFilter
  from "@coremedia/studio-client.main.editor-components/configuration/RegisterLibraryTreeFilter";
import Actions_properties from "@coremedia/studio-client.main.editor-components/sdk/actions/Actions_properties";
import CollectionView from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionView";
import IssueCategories_properties
  from "@coremedia/studio-client.main.editor-components/sdk/collectionview/search/IssueCategories_properties";
import ConfigureDashboardPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/ConfigureDashboardPlugin";
import FixedSearchWidgetType
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/search/FixedSearchWidgetType";
import SimpleSearchWidgetType
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/search/SimpleSearchWidgetType";

import TranslationStatusWidgetType
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/translate/TranslationStatusWidgetType";
import EditorMainView from "@coremedia/studio-client.main.editor-components/sdk/desktop/EditorMainView";
import EditorStartup from "@coremedia/studio-client.main.editor-components/sdk/desktop/EditorStartup";
import ResetInactiveDocumentFormsPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/desktop/ResetInactiveDocumentFormsPlugin";
import FavoritesButton from "@coremedia/studio-client.main.editor-components/sdk/desktop/maintoolbar/FavoritesButton";
import ReusableDocumentFormTabsPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/desktop/reusability/ReusableDocumentFormTabsPlugin";
import AddDefaultFolderChooserEntry
  from "@coremedia/studio-client.main.editor-components/sdk/folderchooser/AddDefaultFolderChooserEntry";
import AddTabbedDocumentFormsPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/plugins/AddTabbedDocumentFormsPlugin";
import ContentUtilConfigurationPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/plugins/ContentUtilConfigurationPlugin";
import EnableStateBasedPublicationButtonsPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/plugins/EnableStateBasedPublicationButtonsPlugin";
import SyncComboBoxValueWithStorePlugin
  from "@coremedia/studio-client.main.editor-components/sdk/plugins/SyncComboBoxValueWithStorePlugin";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import TabbedDocumentFormDispatcher
  from "@coremedia/studio-client.main.editor-components/sdk/premular/TabbedDocumentFormDispatcher";
import LinkListPropertyField
  from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import PreviewIFrameToolbar from "@coremedia/studio-client.main.editor-components/sdk/preview/PreviewIFrameToolbar";
import PreviewPanel from "@coremedia/studio-client.main.editor-components/sdk/preview/PreviewPanel";
import AddQuickCreateLinklistMenuPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/AddQuickCreateLinklistMenuPlugin";
import QuickCreateSettings_properties
  from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateSettings_properties";
import AddShortcutsPlugin from "@coremedia/studio-client.main.editor-components/sdk/shortcuts/AddShortcutsPlugin";
import ImageEditor_properties from "@coremedia/studio-client.main.image-editor-components/ImageEditor_properties";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import resourceManager from "@jangaroo/runtime/l10n/resourceManager";
import BlueprintDeviceTypes_properties from "./BlueprintDeviceTypes_properties";
import BlueprintDocumentTypes_properties from "./BlueprintDocumentTypes_properties";
import BlueprintFormsStudioPluginBase from "./BlueprintFormsStudioPluginBase";
import BlueprintImageEditor_properties from "./BlueprintImageEditor_properties";
import BlueprintIssueCategories_properties from "./BlueprintIssueCategories_properties";
import BlueprintPageGridLayouts_properties from "./BlueprintPageGridLayouts_properties";
import BlueprintStudio_properties from "./BlueprintStudio_properties";
import BlueprintViewtypes_properties from "./BlueprintViewtypes_properties";
import ConfigureCollectionViewColumnsPlugin from "./ConfigureCollectionViewColumnsPlugin";
import CustomLabels_properties from "./CustomLabels_properties";
import CustomTimeZones_properties from "./CustomTimeZones_properties";
import NewContentSettingsStudioPlugin_properties from "./NewContentSettingsStudioPlugin_properties";
import Placements_properties from "./Placements_properties";
import Validation_properties from "./Validation_properties";
import CMActionForm from "./forms/CMActionForm";
import CMArticleForm from "./forms/CMArticleForm";
import CMAudioForm from "./forms/CMAudioForm";
import CMCSSForm from "./forms/CMCSSForm";
import CMChannelForm from "./forms/CMChannelForm";
import CMCollectionForm from "./forms/CMCollectionForm";
import CMDownloadForm from "./forms/CMDownloadForm";
import CMExternalLinkForm from "./forms/CMExternalLinkForm";
import CMFolderPropertiesForm from "./forms/CMFolderPropertiesForm";
import CMGalleryForm from "./forms/CMGalleryForm";
import CMHTMLForm from "./forms/CMHTMLForm";
import CMImageForm from "./forms/CMImageForm";
import CMImageMapForm from "./forms/CMImageMapForm";
import CMInteractiveForm from "./forms/CMInteractiveForm";
import CMJavaScriptForm from "./forms/CMJavaScriptForm";
import CMLocTaxonomyForm from "./forms/CMLocTaxonomyForm";
import CMPersonForm from "./forms/CMPersonForm";
import CMPictureForm from "./forms/CMPictureForm";
import CMPlaceholderForm from "./forms/CMPlaceholderForm";
import CMResourceBundleForm from "./forms/CMResourceBundleForm";
import CMSettingsForm from "./forms/CMSettingsForm";
import CMSiteForm from "./forms/CMSiteForm";
import CMSitemapForm from "./forms/CMSitemapForm";
import CMSpinnerForm from "./forms/CMSpinnerForm";
import CMSymbolForm from "./forms/CMSymbolForm";
import CMTaxonomyForm from "./forms/CMTaxonomyForm";
import CMTeaserForm from "./forms/CMTeaserForm";
import CMTemplateSetForm from "./forms/CMTemplateSetForm";
import CMThemeForm from "./forms/CMThemeForm";
import CMVideoForm from "./forms/CMVideoForm";
import CMViewtypeForm from "./forms/CMViewtypeForm";
import EditorPreferencesForm from "./forms/EditorPreferencesForm";
import ConfigureQuickSearchPlugin
  from "@coremedia/studio-client.main.quicksearch-components/ConfigureQuickSearchPlugin";
import QuickSearchActionConfiguration from "@coremedia/studio-client.quicksearch-models/QuickSearchActionConfiguration";
import OpenDialogAction from "@coremedia/studio-client.ext.ui-components/actions/OpenDialogAction";
import FrameComponents_properties from "@coremedia/studio-client.ext.frame-components/FrameComponents_properties";
import AboutWindow from "@coremedia/studio-client.ext.frame-components/about/AboutWindow";
import { changePassword, typePreferences, upload } from "@coremedia/studio-client.common-icons";
import UploadDialog from "@coremedia/studio-client.main.editor-components/sdk/upload/dialog/UploadDialog";
import Upload_properties from "@coremedia/studio-client.cap-base-models/upload/Upload_properties";
import ContentQuickSearchType from "@coremedia/studio-client.quicksearch-models/content/ContentQuickSearchType";
import AppQuickSearchType from "@coremedia/studio-client.quicksearch-models/apps/AppQuickSearchType";
import QuickSearch_properties from "@coremedia/studio-client.main.quicksearch-components/QuickSearch_properties";
import StudioPreferenceWindow from "@coremedia/studio-client.main.editor-components/sdk/desktop/StudioPreferenceWindow";
import WindowSkin from "@coremedia/studio-client.ext.ui-components/skins/WindowSkin";
import ChangePasswordWindow from "@coremedia/studio-client.ext.frame-components/login/ChangePasswordWindow";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import WelcomeWidgetType
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/welcome/WelcomeWidgetType";
import WelcomeWidgetState
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/welcome/WelcomeWidgetState";
import BookmarksWidgetType
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/bookmarks/BookmarksWidgetType";
import WidgetState from "@coremedia/studio-client.main.editor-components/sdk/dashboard/WidgetState";
import BookmarksWidgetState
  from "@coremedia/studio-client.main.editor-components/sdk/dashboard/widgets/bookmarks/BookmarksWidgetState";
import WorkflowQuickSearchType
  from "@coremedia/studio-client.quicksearch-models/workflow/WorkflowQuickSearchType";
import WorkflowRouteKeys from "@coremedia/studio-client.workflow-services-api/routes/WorkflowRouteKeys";

interface BlueprintFormsStudioPluginConfig extends Config<BlueprintFormsStudioPluginBase> {
}

/* Configure the generic Studio components for the Blueprint content type model and use cases. */
class BlueprintFormsStudioPlugin extends BlueprintFormsStudioPluginBase {
  declare Config: BlueprintFormsStudioPluginConfig;

  static readonly xtype: string = "com.coremedia.blueprint.studio.config.blueprintFormsStudioPlugin";

  static readonly EDITED_BY_ME_TYPE: string = "editedByMe";

  static readonly EDITED_BY_OTHERS_TYPE: string = "editedByOthers";

  /**
   * The itemId of the first actions toolbar separator.
   */
  static readonly ACTIONS_TOOLBAR_SEP_FIRST_ITEM_ID: string = "actionsToolbarSepFirst";

  #root: Content = null;

  constructor(config: Config<BlueprintFormsStudioPlugin> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    this$.#root = session._.getConnection().getContentRepository().getRoot();
    super(ConfigUtils.apply(Config(BlueprintFormsStudioPlugin, {

      rules: [

        Config(EditorStartup, {
          plugins: [
            Config(ConfigureCollectionViewColumnsPlugin),
          ],
        }),

        Config(CollectionView, {
          plugins: [
            Config(PictureUtilsPlugin),
          ],
        }),

        Config(PreviewPanel, {
          plugins: [
            Config(AddPreviewDateSelectorButtonPlugin),
          ],
        }),

        Config(PreviewIFrameToolbar, {
          plugins: [
            Config(AddPlacementHighlightButtonPlugin),
          ],
        }),

        Config(PlacementLinkListPropertyField, {
          plugins: [
            Config(NestedRulesPlugin, {
              rules: [
                Config(LinkListPropertyField, {
                  plugins: [
                    Config(AddQuickCreateLinklistMenuPlugin),
                  ],
                }),
              ],
            }),
          ],
        }),

        Config(EditorMainView, {
          plugins: [
            Config(AddDefaultFolderChooserEntry, { lookup: BlueprintFormsStudioPluginBase.calculateQuickCreateFolder }),
          ],
        }),

        Config(TabbedDocumentFormDispatcher, {
          plugins: [
            Config(AddTabbedDocumentFormsPlugin, {
              documentTabPanels: [
                Config(EditorPreferencesForm, { itemId: "EditorPreferences" }),
                Config(CMActionForm, { itemId: "CMAction" }),
                Config(CMArticleForm, { itemId: "CMArticle" }),
                Config(CMAudioForm, { itemId: "CMAudio" }),
                Config(CMCSSForm, { itemId: "CMCSS" }),
                Config(CMChannelForm, { itemId: "CMChannel" }),
                Config(CMCollectionForm, { itemId: "CMCollection" }),
                Config(CMExternalLinkForm, { itemId: "CMExternalLink" }),
                Config(CMFolderPropertiesForm, { itemId: "CMFolderProperties" }),
                Config(CMJavaScriptForm, { itemId: "CMJavaScript" }),
                Config(CMGalleryForm, { itemId: "CMGallery" }),
                Config(CMDownloadForm, { itemId: "CMDownload" }),
                Config(CMHTMLForm, { itemId: "CMHTML" }),
                Config(CMInteractiveForm, { itemId: "CMInteractive" }),
                Config(CMImageForm, { itemId: "CMImage" }),
                Config(CMImageMapForm, { itemId: "CMImageMap" }),
                Config(CMLocTaxonomyForm, { itemId: "CMLocTaxonomy" }),
                Config(CMPersonForm, { itemId: "CMPerson" }),
                Config(CMPictureForm, { itemId: "CMPicture" }),
                Config(CMPlaceholderForm, { itemId: "CMPlaceholder" }),
                Config(CMResourceBundleForm, { itemId: "CMResourceBundle" }),
                Config(CMSettingsForm, { itemId: "CMSettings" }),
                Config(CMSiteForm, { itemId: "CMSite" }),
                Config(CMSitemapForm, { itemId: "CMSitemap" }),
                Config(CMSpinnerForm, { itemId: "CMSpinner" }),
                Config(CMSymbolForm, { itemId: "CMSymbol" }),
                Config(CMTaxonomyForm, { itemId: "CMTaxonomy" }),
                Config(CMTemplateSetForm, { itemId: "CMTemplateSet" }),
                Config(CMTeaserForm, { itemId: "CMTeaser" }),
                Config(CMThemeForm, { itemId: "CMTheme" }),
                Config(CMVideoForm, { itemId: "CMVideo" }),
                Config(CMViewtypeForm, { itemId: "CMViewtype" }),
              ],
            }),
          ],
        }),

        Config(DocumentTabPanel, {
          plugins: [
            Config(NestedRulesPlugin, {
              rules: [
                Config(LocalComboBox, {
                  plugins: [
                    Config(SyncComboBoxValueWithStorePlugin),
                  ],
                }),
              ],
            }),
          ],
        }),

        Config(DocumentForm, {
          plugins: [
            Config(ResetInactiveDocumentFormsPlugin),
          ],
        }),

        Config(FavoritesButton, {
          plugins: [
            new AddArrayItemsPlugin({
              arrayProperty: "defaultItems",
              items:
                [{
                  "_main": {
                    "searchText": "",
                    "contentType": "Document_",
                    "mode": "search",
                    "view": "list",
                    "folder": this$.#root,
                    "orderBy": "freshness desc",
                    "limit": 50,
                  },
                  "status": {
                    "inProduction": true,
                    "editedByMe": true,
                    "editedByOthers": true,
                    "notEdited": true,
                    "approved": true,
                    "published": true,
                    "deleted": false,
                  },
                  "lastEdited": { "lastEditedBy": "me" },
                  "_name": BlueprintStudio_properties.FavoritesToolbarDefaultSearchFolderNames_lastEdited,
                },
                  {
                    "_main": {
                      "searchText": "",
                      "contentType": "CMArticle",
                      "mode": "search",
                      "view": "list",
                      "folder": this$.#root,
                      "orderBy": "freshness desc",
                      "limit": 50,
                    },
                    "site": { "site": "PREFERRED" },
                    "status": {
                      "inProduction": true,
                      "editedByMe": true,
                      "editedByOthers": true,
                      "notEdited": true,
                      "approved": true,
                      "published": true,
                      "deleted": false,
                    },
                    "lastEdited": { "lastEditedBy": "anyone" },
                    "_name": BlueprintStudio_properties.FavoritesToolbarDefaultSearchFolderNames_articles,
                  },
                  {
                    "_main": {
                      "searchText": "",
                      "contentType": "CMPicture",
                      "mode": "search",
                      "view": "thumbnails",
                      "folder": this$.#root,
                      "orderBy": "freshness desc",
                      "limit": 50,
                    },
                    "site": { "site": "PREFERRED" },
                    "status": {
                      "inProduction": true,
                      "editedByMe": true,
                      "editedByOthers": true,
                      "notEdited": true,
                      "approved": true,
                      "published": true,
                      "deleted": false,
                    },
                    "lastEdited": { "lastEditedBy": "anyone" },
                    "_name": BlueprintStudio_properties.FavoritesToolbarDefaultSearchFolderNames_pictures,
                  },
                  {
                    "_main": {
                      "searchText": "",
                      "contentType": "CMChannel",
                      "mode": "search",
                      "view": "list",
                      "folder": this$.#root,
                      "orderBy": "freshness desc",
                      "limit": 50,
                    },
                    "site": { "site": "PREFERRED" },
                    "status": {
                      "inProduction": true,
                      "editedByMe": true,
                      "editedByOthers": true,
                      "notEdited": true,
                      "approved": true,
                      "published": true,
                      "deleted": false,
                    },
                    "lastEdited": { "lastEditedBy": "anyone" },
                    "_name": BlueprintStudio_properties.FavoritesToolbarDefaultSearchFolderNames_pages,
                  }],
            }),
          ],
        }),
      ],

      configuration: [
        /* override the standard studio labels with custom properties */
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ImageEditor_properties),
          source: resourceManager.getResourceBundle(null, BlueprintImageEditor_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ContentTypes_properties),
          source: resourceManager.getResourceBundle(null, BlueprintDocumentTypes_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ContentTypes_properties),
          source: resourceManager.getResourceBundle(null, Placements_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, Actions_properties),
          source: resourceManager.getResourceBundle(null, CustomLabels_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, TimeZones_properties),
          source: resourceManager.getResourceBundle(null, CustomTimeZones_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, QuickCreateSettings_properties),
          source: resourceManager.getResourceBundle(null, NewContentSettingsStudioPlugin_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, Validators_properties),
          source: resourceManager.getResourceBundle(null, Validation_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, DeviceTypes_properties),
          source: resourceManager.getResourceBundle(null, BlueprintDeviceTypes_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, Viewtypes_properties),
          source: resourceManager.getResourceBundle(null, BlueprintViewtypes_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, PageGridLayouts_properties),
          source: resourceManager.getResourceBundle(null, BlueprintPageGridLayouts_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, IssueCategories_properties),
          source: resourceManager.getResourceBundle(null, BlueprintIssueCategories_properties),
        }),
        /* enable studio shortcuts, override Shortcut.properties to specify different shortcut keys */
        new AddShortcutsPlugin({}),

        new EnableStateBasedPublicationButtonsPlugin({ adminOnly: true }),

        new ContentUtilConfigurationPlugin({ expandFolderLimit: 100 }),

        new ConfigureDocumentTypes({
          names: "CMLinkable",
          previewUrlTransformers: [
            new AddDependingParameterPreviewUrlTransformer({
              name: "contentType",
              valueExpression: BlueprintFormsStudioPluginBase.getContentTypeVE(),
            }),
          ],
        }),

        /* Register content types to be allowed to be dropped */
        new ConfigureDocumentTypes({
          names: "CMLinkable",
          richTextLinkable: true,
        }),

        /* register Image Types for thumbnail view and RichText Drag'n'Drop */
        new ConfigureDocumentTypes({
          names: "CMMedia,CMImage",
          imageProperty: "data",
          richTextImageBlobProperty: "data",
        }),
        new ConfigureDefaultRichTextImageDocumentType({ defaultRichTextImageType: "CMPicture" }),

        /* exclude DocTypes from the create new documents menu */
        new ConfigureDocumentTypes({
          names: "Dictionary,Preferences,Query,CMDynamicList,CMVisual,EditorPreferences,EditorProfile",
          exclude: true,
          excludeFromSearch: true,
        }),

        /* there is no preview available for these DocTypes */
        new ConfigureDocumentTypes({
          names: "CMAction,CMCSS,CMFolderProperties,CMImage,CMInteractive,CMJavaScript,\n                                            CMPlaceholder,CMResourceBundle,CMSettings,CMSite,CMSymbol,EditorPreferences,EditorProfile,\n                                            CMTemplateSet,CMViewtype,CMTaxonomy,CMLocTaxonomy",
          preview: false,
        }),
        /* there may be no preview available for these DocTypes */
        new ConfigureDocumentTypes({
          names: "CMObject",
          mayPreview: BlueprintFormsStudioPluginBase.isValidCMLinkable,
        }),

        /* All CMHasContexts documents (essentially non-CMContext/CMChannel) are shown in "fragment preview" */
        new ConfigureDocumentTypes({
          names: "CMHasContexts",
          previewUrlTransformers: [
            new AddParameterPreviewUrlTransformer({
              name: "view",
              value: "fragmentPreview",
            }),
          ],
        }),

        new ConfigureDocumentTypes({
          names: "CMTheme",
          previewUrlTransformers: [
            new AddParameterPreviewUrlTransformer({
              name: "view",
              value: "",
            }),
          ],
        }),

        new ConfigureDocumentTypes({
          names: "CMSitemap",
          previewUrlTransformers: [
            new AddParameterPreviewUrlTransformer({
              name: "view",
              value: "",
            }),
          ],
        }),

        new ConfigureDashboardPlugin({
          widgets: [
            new WelcomeWidgetState({}),
            new BookmarksWidgetState({
              column: WidgetState.NEXT,
            }),
            new WidgetState({
              widgetTypeId: BlueprintFormsStudioPlugin.EDITED_BY_ME_TYPE,
              column: WidgetState.NEXT,
              rowspan: 1,
            }),
          ],
          types: [
            new WelcomeWidgetType({}),
            new BookmarksWidgetType({}),
            new SimpleSearchWidgetType({}),

            new FixedSearchWidgetType({
              id_: BlueprintFormsStudioPlugin.EDITED_BY_ME_TYPE,
              name: BlueprintStudio_properties.Dashboard_standardConfiguration_lastEdited,
              search: new SearchState({ lastEditedBy: "me" }),
            }),

            new FixedSearchWidgetType({
              id_: BlueprintFormsStudioPlugin.EDITED_BY_OTHERS_TYPE,
              name: BlueprintStudio_properties.Dashboard_standardConfiguration_editedByOthers,
              search: new SearchState({
                editedByOthers: true,
                editedByMe: false,
                notEdited: false,
                approved: false,
                published: false,
              }),
            }),

            new TranslationStatusWidgetType({}),
          ],
        }),

        new ReusableDocumentFormTabsPlugin({ defaultLimit: 2 }),

        new RegisterLibraryTreeFilter({ path: "/Home/$USER/EditorProfile" }),

        new ConfigureQuickSearchPlugin({
          keepResults: true,
          imageDocTypes: [
            "CMPicture"
          ],
          actions: [
            new QuickSearchActionConfiguration({
              svgIcon: typePreferences,
              label: FrameComponents_properties.PreferenceWindow_title,
              additionalKeywords: [FrameComponents_properties.PreferenceWindow_shortcuts_text,
                FrameComponents_properties.PreferenceWindow_language_text,
                FrameComponents_properties.PreferenceWindow_SiteSelector_title,
                FrameComponents_properties.PreferenceWindow_dialogs_title,
                FrameComponents_properties.PreferenceWindow_shortcuts_text,
              ],
              action: (): void => {
                new OpenDialogAction({
                  dialogDefaults: Config(StudioPreferenceWindow, {}),
                }).execute();
              },
            }),
            new QuickSearchActionConfiguration({
              svgIcon: typePreferences,
              label: FrameComponents_properties.AboutWindow_title,
              additionalKeywords: [],
              action: (): void => {
                new OpenDialogAction({
                  dialogDefaults: Config(AboutWindow, {}),
                }).execute();
              },
            }),
            new QuickSearchActionConfiguration({
              svgIcon: upload,
              label: Upload_properties.Upload_button_label,
              additionalKeywords: [],
              action: (): void => {
                new OpenDialogAction({
                  dialogDefaults: Config(UploadDialog, {}),
                }).execute();
              },
            }),
            new QuickSearchActionConfiguration({
              svgIcon: changePassword,
              label: Editor_properties.HeaderToolbar_changePassword_btn_text,
              additionalKeywords: Editor_properties.HeaderToolbar_changePassword_btn_text.split(" "),
              isSearchable: async () => { return session._.getUser().isBuiltIn(); },
              action: (): void => {
                new OpenDialogAction({
                  dialogDefaults: Config(ChangePasswordWindow, {
                    ui: WindowSkin.GRID_200.getSkin()
                  }),
                }).execute();
              },
            }),
          ],
          types: [
            new AppQuickSearchType({
              title: QuickSearch_properties.QuickSearch_search_type_apps,
            }),
            new ContentQuickSearchType({
              id: "allContent",
              title: QuickSearch_properties.QuickSearch_search_type_content,
            }),
            new WorkflowQuickSearchType({
              id: "openWorkflows",
              workflowStatus: WorkflowRouteKeys.INBOX_ROUTE_SEGMENT_KEY,
              title: QuickSearch_properties.QuickSearch_search_type_workflow_inbox,
            }),
            new WorkflowQuickSearchType({
              id: "runningWorkflows",
              workflowStatus: WorkflowRouteKeys.PENDING_ROUTE_SEGMENT_KEY,
              title: QuickSearch_properties.QuickSearch_search_type_workflow_pending,
            }),
            new WorkflowQuickSearchType({
              id: "closedWorkflows",
              workflowStatus: WorkflowRouteKeys.FINISHED_ROUTE_SEGMENT_KEY,
              title: QuickSearch_properties.QuickSearch_search_type_workflow_finished,
            }),
          ]
        }),
      ],

    }), config));
  }
}

export default BlueprintFormsStudioPlugin;
