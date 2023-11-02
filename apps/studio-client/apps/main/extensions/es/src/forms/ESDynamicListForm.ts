import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import CustomLabels_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/CustomLabels_properties";
import DefaultExtraDataForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/DefaultExtraDataForm";
import ContainerViewTypeSelectorForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ContainerViewTypeSelectorForm";
import MetaDataWithoutSettingsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MetaDataWithoutSettingsForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import TeaserDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/TeaserDocumentForm";
import ValidityDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ValidityDocumentForm";
import LocalComboBox from "@coremedia/studio-client.ext.ui-components/components/LocalComboBox";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import SpinnerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/SpinnerPropertyField";
import BindDisablePlugin from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/plugins/BindDisablePlugin";
import QuickCreateToolbarButton from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateToolbarButton";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import ElasticSocialStudioPlugin_properties from "../ElasticSocialStudioPlugin_properties";

interface ESDynamicListFormConfig extends Config<DocumentTabPanel> {
}

class ESDynamicListForm extends DocumentTabPanel {
  declare Config: ESDynamicListFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.elastic.social.studio.config.esDynamicListForm";

  /**
   * The itemId of the extended link list toolbar separator.
   */
  static readonly EXTENDED_LINK_LIST_PROPERTY_SEP_ITEM_ID: string = "extendedLinkListPropertyFieldSeparator";

  constructor(config: Config<ESDynamicListForm> = null) {
    super(ConfigUtils.apply(Config(ESDynamicListForm, {
      itemId: "ESDynamicList",

      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm),
            Config(PropertyFieldGroup, {
              title: CustomLabels_properties.PropertyGroup_Configuration_label,
              itemId: "esDynamicListConfigForm",
              items: [
                Config(LocalComboBox, {
                  name: "properties.aggregationType",
                  itemId: "aggregationType",
                  fieldLabel: ElasticSocialStudioPlugin_properties.ESDynamicList_aggregationType_text,
                  anchor: "100%",
                  store: [
                    ["TOP_RATED", ElasticSocialStudioPlugin_properties.TOP_RATED],
                    ["MOST_REVIEWED", ElasticSocialStudioPlugin_properties.MOST_REVIEWED],
                    ["TOP_REVIEWED", ElasticSocialStudioPlugin_properties.TOP_REVIEWED],
                    ["MOST_COMMENTED", ElasticSocialStudioPlugin_properties.MOST_COMMENTED],
                    ["MOST_RATED", ElasticSocialStudioPlugin_properties.MOST_RATED],
                    ["MOST_LIKED", ElasticSocialStudioPlugin_properties.MOST_LIKED],
                    ["MOST_SHARED", ElasticSocialStudioPlugin_properties.MOST_SHARED],
                  ],
                  triggerAction: "all",
                  encodeItems: true,
                  ...ConfigUtils.append({
                    plugins: [
                      Config(BindDisablePlugin, {
                        forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                        bindTo: config.bindTo,
                      }),
                      Config(BindPropertyPlugin, {
                        componentEvent: "change",
                        bindTo: config.bindTo.extendBy("properties").extendBy("aggregationType"),
                        bidirectional: true,
                      }),
                    ],
                  }),
                }),
                Config(LocalComboBox, {
                  name: "properties.interval",
                  itemId: "interval",
                  fieldLabel: ElasticSocialStudioPlugin_properties.ESDynamicList_interval_text,
                  anchor: "100%",
                  store: [
                    ["INFINITY", ElasticSocialStudioPlugin_properties.INFINITY],
                    ["LAST_YEAR", ElasticSocialStudioPlugin_properties.LAST_YEAR],
                    ["LAST_MONTH", ElasticSocialStudioPlugin_properties.LAST_MONTH],
                    ["LAST_WEEK", ElasticSocialStudioPlugin_properties.LAST_WEEK],
                    ["LAST_DAY", ElasticSocialStudioPlugin_properties.LAST_DAY],
                  ],
                  triggerAction: "all",
                  encodeItems: true,
                  ...ConfigUtils.append({
                    plugins: [
                      Config(BindDisablePlugin, {
                        forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                        bindTo: config.bindTo,
                      }),
                      Config(BindPropertyPlugin, {
                        componentEvent: "change",
                        bindTo: config.bindTo.extendBy("properties").extendBy("interval"),
                        bidirectional: true,
                      }),
                    ],
                  }),
                }),
                Config(LinkListPropertyField, {
                  propertyName: "channel",
                  itemId: "channel",
                  additionalToolbarItems: [
                    Config(Separator, { itemId: ESDynamicListForm.EXTENDED_LINK_LIST_PROPERTY_SEP_ITEM_ID }),
                    Config(QuickCreateToolbarButton, {
                      bindTo: config.bindTo,
                      contentType: "CMChannel",
                      propertyName: "channel",
                    }),
                  ],
                }),
                Config(SpinnerPropertyField, {
                  propertyName: "maxLength",
                  itemId: "maxLength",
                  minValue: 1,
                }),
              ],
            }),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/ESDynamicList/",
                "Options/Viewtypes/ESDynamicList/",
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

export default ESDynamicListForm;
