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
import QuickCreateLinklistMenu from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateLinklistMenu";
import QuickCreateToolbarButton from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateToolbarButton";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import AnalyticsProviderComboBox from "./AnalyticsProviderComboBox";
import AnalyticsStudioPluginDocTypes_properties from "./AnalyticsStudioPluginDocTypes_properties";
import AnalyticsStudioPlugin_properties from "./AnalyticsStudioPlugin_properties";
import FilteringContentTypeStringPropertyField from "./FilteringContentTypeStringPropertyField";

interface CMALXPageListFormConfig extends Config<DocumentTabPanel> {
}

/**
 * The form used to edit a document of type CMALXPageList.
 */
class CMALXPageListForm extends DocumentTabPanel {
  declare Config: CMALXPageListFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.analytics.cmalxPageListForm";

  constructor(config: Config<CMALXPageListForm> = null) {
    super(ConfigUtils.apply(Config(CMALXPageListForm, {

      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm),
            Config(PropertyFieldGroup, {
              itemId: "alxPageListDefaultContentForm",
              title: AnalyticsStudioPluginDocTypes_properties.CMALXPageList_defaultContent_text,
              items: [
                Config(LinkListPropertyField, {
                  linkType: "CMTeasable",
                  showThumbnails: true,
                  hideLabel: true,
                  propertyName: "defaultContent",
                  bindTo: config.bindTo,
                  additionalToolbarItems: [
                    Config(Separator),
                    Config(QuickCreateLinklistMenu, {
                      bindTo: config.bindTo,
                      propertyName: "defaultContent",
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
              itemId: "alxPageListGenericForm",
              title: AnalyticsStudioPlugin_properties.SpacerTitle_generic,
              items: [
                Config(FilteringContentTypeStringPropertyField, {
                  propertyName: "documentType",
                  baseType: "CMLinkable",
                }),
                Config(LinkListPropertyField, {
                  propertyName: "baseChannel",
                  itemId: "baseChannel",
                  additionalToolbarItems: [
                    Config(Separator),
                    Config(QuickCreateToolbarButton, {
                      bindTo: config.bindTo,
                      contentType: "CMChannel",
                      propertyName: "baseChannel",
                    }),
                  ],
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "maxLength",
                  itemId: "maxLength",
                  minValue: 1,
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "timeRange",
                  itemId: "timeRange",
                  minValue: 1,
                }),
                Config(AnalyticsProviderComboBox, { propertyName: "analyticsProvider" }),
              ],
            }),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/CMALXPageList/",
                "Options/Viewtypes/CMALXPageList/",
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

export default CMALXPageListForm;
