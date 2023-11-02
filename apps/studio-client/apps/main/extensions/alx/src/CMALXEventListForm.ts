import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import ValidityColumn from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/columns/ValidityColumn";
import DefaultExtraDataForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/DefaultExtraDataForm";
import ContainerViewTypeSelectorForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ContainerViewTypeSelectorForm";
import MetaDataWithoutSettingsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MetaDataWithoutSettingsForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import TeaserDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/TeaserDocumentForm";
import ValidityDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ValidityDocumentForm";
import NameColumn from "@coremedia/studio-client.ext.cap-base-components/columns/NameColumn";
import StatusColumn from "@coremedia/studio-client.ext.cap-base-components/columns/StatusColumn";
import TypeIconColumn from "@coremedia/studio-client.ext.cap-base-components/columns/TypeIconColumn";
import LinkListThumbnailColumn from "@coremedia/studio-client.ext.content-link-list-components/columns/LinkListThumbnailColumn";
import DataField from "@coremedia/studio-client.ext.ui-components/store/DataField";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import SpinnerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/SpinnerPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import QuickCreateLinklistMenu from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateLinklistMenu";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import AnalyticsProviderComboBox from "./AnalyticsProviderComboBox";
import AnalyticsStudioPluginDocTypes_properties from "./AnalyticsStudioPluginDocTypes_properties";
import AnalyticsStudioPlugin_properties from "./AnalyticsStudioPlugin_properties";

interface CMALXEventListFormConfig extends Config<DocumentTabPanel> {
}

/**
 * The form used to edit a document of type CMALXEventList.
 */
class CMALXEventListForm extends DocumentTabPanel {
  declare Config: CMALXEventListFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.analytics.cmalxEventListForm";

  constructor(config: Config<CMALXEventListForm> = null) {
    super(ConfigUtils.apply(Config(CMALXEventListForm, {

      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm),
            Config(PropertyFieldGroup, {
              itemId: "alxEventListPicturesForm",
              title: AnalyticsStudioPluginDocTypes_properties.CMALXEventList_defaultContent_text,
              items: [
                Config(LinkListPropertyField, {
                  linkType: "CMTeasable",
                  showThumbnails: true,
                  hideLabel: true,
                  propertyName: "items",
                  bindTo: config.bindTo,
                  additionalToolbarItems: [
                    Config(Separator),
                    Config(QuickCreateLinklistMenu, {
                      bindTo: config.bindTo,
                      propertyName: "items",
                    }),
                  ],
                  fields: [
                    Config(DataField, {
                      name: ValidityColumn.STATUS_ID,
                      mapping: "",
                      convert: ValidityColumn.convert,
                    }),
                  ],
                  columns: [
                    Config(LinkListThumbnailColumn),
                    Config(TypeIconColumn),
                    Config(NameColumn, { flex: 1 }),
                    Config(ValidityColumn),
                    Config(StatusColumn),
                  ],
                }),
              ],
            }),
            Config(PropertyFieldGroup, {
              itemId: "alxEventListGenericForm",
              title: AnalyticsStudioPlugin_properties.SpacerTitle_generic,
              items: [
                Config(SpinnerPropertyField, {
                  propertyName: "maxLength",
                  minValue: 1,
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "timeRange",
                  minValue: 1,
                }),
                Config(StringPropertyField, { propertyName: "category" }),
                Config(StringPropertyField, { propertyName: "action" }),
                Config(AnalyticsProviderComboBox, { propertyName: "analyticsProvider" }),
              ],
            }),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/CMALXEventList/",
                "Options/Viewtypes/CMALXEventList/",
              ]
            }),
            Config(ValidityDocumentForm),
          ],
        }),
        Config(DefaultExtraDataForm),
        Config(MultiLanguageDocumentForm),
        Config(MetaDataWithoutSettingsForm),
      ],

    }), config));
  }
}

export default CMALXEventListForm;
