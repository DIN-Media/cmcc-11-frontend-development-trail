import BlueprintDocumentTypes_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintDocumentTypes_properties";
import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import CategoryDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/CategoryDocumentForm";
import CollapsibleStringPropertyForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/CollapsibleStringPropertyForm";
import MetaDataInformationForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MetaDataInformationForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import TeaserWithPictureDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/TeaserWithPictureDocumentForm";
import ValidityDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ValidityDocumentForm";
import CatalogObjectPropertyNames from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogObjectPropertyNames";
import AugmentationUtil from "@coremedia-blueprint/studio-client.main.ec-studio/helper/AugmentationUtil";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ImageComponent from "@coremedia/studio-client.ext.ui-components/components/ImageComponent";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import VBoxLayout from "@jangaroo/ext-ts/layout/container/VBox";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CatalogThumbnailResolver from "../CatalogThumbnailResolver";
import LivecontextStudioPlugin_properties from "../LivecontextStudioPlugin_properties";
import CommercePricesPropertyFieldGroup from "../components/CommercePricesPropertyFieldGroup";
import CatalogAssetsProperty from "../components/link/CatalogAssetsProperty";
import CommerceAttributesForm from "../desktop/CommerceAttributesForm";
import CommerceAugmentedPageGridForm from "../desktop/CommerceAugmentedPageGridForm";
import CommerceDetailsForm from "../desktop/CommerceDetailsForm";
import CommerceProductStructureForm from "../desktop/CommerceProductStructureForm";

interface CMExternalProductFormConfig extends Config<DocumentTabPanel> {
}

class CMExternalProductForm extends DocumentTabPanel {
  declare Config: CMExternalProductFormConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.cmExternalProductForm";

  static readonly CONTENT_TAB_ITEM_ID: string = "contentTab";

  static readonly STRUCTURE_TAB_ITEM_ID: string = "structureTab";

  #catalogObjectExpression: ValueExpression = null;

  constructor(config: Config<CMExternalProductForm> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    this$.#catalogObjectExpression = AugmentationUtil.getCatalogObjectExpression(config.bindTo);
    super(ConfigUtils.apply(Config(CMExternalProductForm, {

      items: [
        Config(DocumentForm, {
          itemId: CMExternalProductForm.CONTENT_TAB_ITEM_ID,
          title: BlueprintTabs_properties.Tab_content_title,
          items: [
            Config(CollapsibleStringPropertyForm, {
              propertyName: "title",
              title: BlueprintDocumentTypes_properties.CMExternalProduct_title_text,
            }),
            Config(CommerceDetailsForm, {
              itemId: "commerceDetails",
              bindTo: this$.#catalogObjectExpression,
              contentBindTo: config.bindTo,
              collapsed: true,
            }),

            Config(CommerceAugmentedPageGridForm, {
              itemId: "pdpPagegrid",
              pageGridPropertyName: "pdpPagegrid",
              fallbackPageGridPropertyName: "placement",
            }),

            Config(CommercePricesPropertyFieldGroup, {
              bindTo: this$.#catalogObjectExpression,
              itemId: "prices",
            }),

            Config(PropertyFieldGroup, {
              title: LivecontextStudioPlugin_properties.Commerce_PropertyGroup_thumbnail_title,
              itemId: "picture",
              items: [
                Config(ImageComponent, {
                  width: 120,
                  plugins: [
                    Config(BindPropertyPlugin, {
                      componentProperty: "src",
                      bindTo: CatalogThumbnailResolver.imageValueExpression(this$.#catalogObjectExpression),
                    }),
                  ],
                }),
              ],
              layout: Config(VBoxLayout),
            }),
            Config(PropertyFieldGroup, {
              title: LivecontextStudioPlugin_properties.Commerce_Product_PropertyGroup_richMedia_title,
              itemId: "richMedia",
              bindTo: this$.#catalogObjectExpression,
              items: [
                Config(CatalogAssetsProperty, {
                  propertyName: CatalogObjectPropertyNames.VISUALS,
                  assetContentTypes: ["CMPicture", "CMVideo", "CMSpinner"],
                  emptyText: LivecontextStudioPlugin_properties.Commerce_Product_richMedia_emptyText,
                }),
              ],
            }),
            Config(PropertyFieldGroup, {
              title: LivecontextStudioPlugin_properties.Commerce_Product_PropertyGroup_downloads_title,
              itemId: "downloads",
              bindTo: this$.#catalogObjectExpression,
              items: [
                Config(CatalogAssetsProperty, {
                  propertyName: CatalogObjectPropertyNames.DOWNLOADS,
                  assetContentTypes: ["CMDownload"],
                  emptyText: LivecontextStudioPlugin_properties.Commerce_Product_downloads_emptyText,
                }),
              ],
            }),
            Config(TeaserWithPictureDocumentForm),
            Config(ValidityDocumentForm),
          ],
        }),
        Config(CommerceAttributesForm, { bindTo: this$.#catalogObjectExpression }),
        Config(CommerceProductStructureForm, {
          itemId: CMExternalProductForm.STRUCTURE_TAB_ITEM_ID,
          bindTo: this$.#catalogObjectExpression,
        }),
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_extras_title,
          itemId: "metadata",
          items: [
            Config(CategoryDocumentForm),
          ],
        }),
        Config(MultiLanguageDocumentForm),
        Config(MetaDataInformationForm),
      ],

    }), config));
  }
}

export default CMExternalProductForm;
