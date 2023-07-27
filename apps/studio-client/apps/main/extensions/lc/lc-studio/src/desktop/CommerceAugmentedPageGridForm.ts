import CustomLabels_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/CustomLabels_properties";
import ValidityColumn from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/columns/ValidityColumn";
import VisibilityConfigurationForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/VisibilityConfigurationForm";
import ViewtypeRenderer from "@coremedia-blueprint/studio-client.main.blueprint-forms/util/ViewtypeRenderer";
import augmentedCategoryTreeRelation from "@coremedia-blueprint/studio-client.main.ec-studio/tree/augmentedCategoryTreeRelation";
import NameColumn from "@coremedia/studio-client.ext.cap-base-components/columns/NameColumn";
import StatusColumn from "@coremedia/studio-client.ext.cap-base-components/columns/StatusColumn";
import TypeIconColumn from "@coremedia/studio-client.ext.cap-base-components/columns/TypeIconColumn";
import LinkListThumbnailColumn from "@coremedia/studio-client.ext.content-link-list-components/columns/LinkListThumbnailColumn";
import AddQuickTipPlugin from "@coremedia/studio-client.ext.ui-components/plugins/AddQuickTipPlugin";
import DataField from "@coremedia/studio-client.ext.ui-components/store/DataField";
import PageGridPropertyField from "@coremedia/studio-client.main.bpbase-pagegrid-studio-plugin/pagegrid/PageGridPropertyField";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import Column from "@jangaroo/ext-ts/grid/column/Column";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LivecontextStudioPlugin_properties from "../LivecontextStudioPlugin_properties";

interface CommerceAugmentedPageGridFormConfig extends Config<PropertyFieldGroup>, Partial<Pick<CommerceAugmentedPageGridForm,
  "showLocal" |
  "pageGridPropertyName" |
  "fallbackPageGridPropertyName" |
  "layoutPaths"
>> {
}

class CommerceAugmentedPageGridForm extends PropertyFieldGroup {
  declare Config: CommerceAugmentedPageGridFormConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.commerceAugmentedPageGridForm";

  constructor(config: Config<CommerceAugmentedPageGridForm> = null) {
    super(ConfigUtils.apply(Config(CommerceAugmentedPageGridForm, {
      title: CustomLabels_properties.PropertyGroup_Placements_label,

      ...ConfigUtils.append({
        plugins: [
          Config(AddQuickTipPlugin, { text: LivecontextStudioPlugin_properties.AugmentedCategory_help_productpagegrid_tooltip }),
        ],
      }),

      items: [
        Config(PageGridPropertyField, {
          propertyName: config.pageGridPropertyName,
          fallbackPropertyName: config.fallbackPageGridPropertyName,
          hideLabel: true,
          showLocal: config.showLocal,
          layoutPaths: config.layoutPaths,
          pageTreeRelation: augmentedCategoryTreeRelation,
          fields: [
            Config(DataField, {
              name: ValidityColumn.STATUS_ID,
              mapping: "",
              convert: ValidityColumn.convert,
            }),
            Config(DataField, {
              name: "viewtypeStatus",
              mapping: "",
              convert: ViewtypeRenderer.convert,
            }),
          ],
          columns: [
            Config(LinkListThumbnailColumn),
            Config(TypeIconColumn),
            Config(NameColumn),
            Config(ValidityColumn),
            Config(Column, {
              stateId: "viewTypeUrl",
              width: 40,
              sortable: false,
              dataIndex: "viewtypeStatus",
              fixed: true,
              renderer: ViewtypeRenderer.renderer,
            }),
            Config(StatusColumn),
          ],
          placementRowWidgetItems: [
            Config(VisibilityConfigurationForm),
          ],
        }),
      ],

    }), config));
  }

  /**
   * Set true to display the inheritance information even if bindTo is considered to be the source of the local pagegrid. Default: false.
   */
  showLocal: boolean = false;

  pageGridPropertyName: string = null;

  fallbackPageGridPropertyName: string = null;

  layoutPaths: Array<string> = null;
}

export default CommerceAugmentedPageGridForm;
