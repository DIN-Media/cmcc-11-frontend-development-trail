import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import ContainerViewTypeSelectorForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ContainerViewTypeSelectorForm";
import FixedIndexItemsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/FixedIndexItemsForm";
import MetaDataWithoutSearchableForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MetaDataWithoutSearchableForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import TeaserDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/TeaserDocumentForm";
import ValidityDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ValidityDocumentForm";
import CatalogModel from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogModel";
import CatalogLinkPropertyField from "@coremedia-blueprint/studio-client.main.ec-studio/components/link/CatalogLinkPropertyField";
import CatalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/helper/CatalogHelper";
import LabelableSkin from "@coremedia/studio-client.ext.ui-components/skins/LabelableSkin";
import QueryEditor_properties from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/QueryEditor_properties";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import ComboBoxStringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/ComboBoxStringPropertyField";
import SpinnerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/SpinnerPropertyField";
import JsonStore from "@jangaroo/ext-ts/data/JsonStore";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LivecontextStudioPlugin_properties from "../LivecontextStudioPlugin_properties";
import CMProductListFormBase from "./CMProductListFormBase";
import CategoryFacetsPropertyField from "./facets/CategoryFacetsPropertyField";

interface CMProductListFormConfig extends Config<CMProductListFormBase> {
}

class CMProductListForm extends CMProductListFormBase {
  declare Config: CMProductListFormConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.cmProductListForm";

  static readonly ITEMS_PROPERTY_NAME: string = "items";

  constructor(config: Config<CMProductListForm> = null) {
    super(ConfigUtils.apply(Config(CMProductListForm, {

      items: [
        Config(DocumentForm, {
          title: QueryEditor_properties.DCQE_label_conditions,
          itemId: "conditionsTab",
          items: [

            Config(PropertyFieldGroup, {
              title: LivecontextStudioPlugin_properties.CMProductList_category_text,
              itemId: "cmProductListCategoryFieldGroup",
              items: [
                Config(CatalogLinkPropertyField, {
                  itemId: "externalId",
                  propertyName: "externalId",
                  maxCardinality: 1,
                  replaceOnDrop: true,
                  linkTypeNames: [CatalogModel.TYPE_CATEGORY],
                  dropAreaHandler: CatalogHelper.getInstance().openCatalog,
                  dropAreaText: LivecontextStudioPlugin_properties.Category_Link_empty_text,
                }),
              ],
            }),

            Config(CategoryFacetsPropertyField, {
              bindTo: config.bindTo,
              externalIdPropertyName: "externalId",
              structPropertyName: "localSettings",
              forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
            }),

            Config(PropertyFieldGroup, {
              title: LivecontextStudioPlugin_properties.CMProductList_searchRefinement_text,
              itemId: "cmProductListSearchRefinementFieldGroup",
              manageHeight: false,
              items: [
                Config(ComboBoxStringPropertyField, {
                  itemId: "cmProductListOrderByField",
                  ui: LabelableSkin.PLAIN_LABEL.getSkin(),
                  propertyName: "localSettings.productList.orderBy",
                  fieldWidth: 200,
                  fieldLabel: LivecontextStudioPlugin_properties["CMProductList_localSettings.productList.orderBy_text"],
                  emptyText: LivecontextStudioPlugin_properties["CMProductList_localSettings.productList.orderBy_emptyText"],
                  valueField: "id",
                  displayField: "value",
                  reverseTransformer: (value: any): any => value === null ? "" : value,
                  store: new JsonStore({
                    fields: ["id", "value"],
                    data: [
                      {
                        id: "ORDER_BY_TYPE_PRICE_ASC",
                        value: LivecontextStudioPlugin_properties["CMProductList_localSettings.productList.orderBy.ORDER_BY_TYPE_PRICE_ASC_text"],
                      },
                      {
                        id: "ORDER_BY_TYPE_PRICE_DSC",
                        value: LivecontextStudioPlugin_properties["CMProductList_localSettings.productList.orderBy.ORDER_BY_TYPE_PRICE_DSC_text"],
                      },
                    ],
                  }),
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "localSettings.productList.offset",
                  itemId: "cmProductListSearchRefinementOffset",
                  fieldWidth: 200,
                  minValue: 1,
                  ui: LabelableSkin.PLAIN_LABEL.getSkin(),
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "localSettings.productList.maxLength",
                  itemId: "cmProductListSearchRefinementMaxLength",
                  fieldWidth: 200,
                  minValue: 1,
                  maxValue: 500,
                  ui: LabelableSkin.PLAIN_LABEL.getSkin(),
                }),
              ],
            }),
          ],
        }),
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm),
            Config(FixedIndexItemsForm),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/CMProductList/",
                "Options/Viewtypes/CMProductList/",
              ]
            }),
            Config(ValidityDocumentForm),
          ],
        }),
        Config(MultiLanguageDocumentForm),
        Config(MetaDataWithoutSearchableForm),
      ],
    }), config));
  }
}

export default CMProductListForm;
