import CatalogObjectPropertyNames
  from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogObjectPropertyNames";
import augmentedCategoryTreeRelation
  from "@coremedia-blueprint/studio-client.main.ec-studio/tree/augmentedCategoryTreeRelation";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import ImageComponent from "@coremedia/studio-client.ext.ui-components/components/ImageComponent";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import VBoxLayout from "@jangaroo/ext-ts/layout/container/VBox";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LivecontextStudioPlugin_properties from "../LivecontextStudioPlugin_properties";
import CommercePricesPropertyFieldGroup from "../components/CommercePricesPropertyFieldGroup";
import CatalogAssetsProperty from "../components/link/CatalogAssetsProperty";
import CommerceAugmentedPageGridForm from "./CommerceAugmentedPageGridForm";
import CommerceDetailsForm from "./CommerceDetailsForm";
import CatalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/helper/CatalogHelper";

interface CommerceProductContentFormConfig extends Config<DocumentForm> {
}

class CommerceProductContentForm extends DocumentForm {
  declare Config: CommerceProductContentFormConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.commerceProductContentForm";

  #augmentedCategoryExpression: ValueExpression = null;

  constructor(config: Config<CommerceProductContentForm> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    this$.#augmentedCategoryExpression = ValueExpressionFactory.createFromFunction((): any =>
      augmentedCategoryTreeRelation.getParentUnchecked(config.bindTo.getValue()),
    );
    super(ConfigUtils.apply(Config(CommerceProductContentForm, {
      title: LivecontextStudioPlugin_properties.Commerce_Tab_content_title,

      items: [
        Config(CommerceDetailsForm, { itemId: "productDetails" }),

        Config(CommerceAugmentedPageGridForm, {
          itemId: "pdpPagegrid",
          showLocal: true,
          forceReadOnlyValueExpression: ValueExpressionFactory.createFromValue(true),
          bindTo: this$.#augmentedCategoryExpression,
          pageGridPropertyName: "pdpPagegrid",
          fallbackPageGridPropertyName: "placement",
          virtual: true,
        }),

        Config(CommercePricesPropertyFieldGroup, {
          bindTo: config.bindTo,
          itemId: "prices",
        }),

        Config(PropertyFieldGroup, {
          title: LivecontextStudioPlugin_properties.Commerce_PropertyGroup_thumbnail_title,
          itemId: "thumbnail",
          items: [
            Config(ImageComponent, {
              width: 120,
              plugins: [
                Config(BindPropertyPlugin, {
                  componentProperty: "src",
                  bindTo: CatalogHelper.imageValueExpression(config.bindTo),
                }),
              ],
            }),
          ],
          layout: Config(VBoxLayout),
        }),
        Config(PropertyFieldGroup, {
          title: LivecontextStudioPlugin_properties.Commerce_Product_PropertyGroup_richMedia_title,
          itemId: "richMedia",
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
          items: [
            Config(CatalogAssetsProperty, {
              propertyName: CatalogObjectPropertyNames.DOWNLOADS,
              assetContentTypes: ["CMDownload"],
              emptyText: LivecontextStudioPlugin_properties.Commerce_Product_downloads_emptyText,
            }),
          ],
        }),
      ],

    }), config));
  }
}

export default CommerceProductContentForm;
