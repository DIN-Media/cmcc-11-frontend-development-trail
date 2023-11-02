import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import CustomLabels_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/CustomLabels_properties";
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
import SearchQueryHelper from "@coremedia/studio-client.main.cap-personalization-ui/SearchQueryHelper";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import ContentTypeStringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/ContentTypeStringPropertyField";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import SpinnerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/SpinnerPropertyField";
import QuickCreateLinklistMenu from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateLinklistMenu";
import ShowIssuesPlugin from "@coremedia/studio-client.main.editor-components/sdk/validation/ShowIssuesPlugin";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import PersonalizationDocTypes_properties from "./PersonalizationDocTypes_properties";
import SearchQueryPropertyField from "./SearchQueryPropertyField";

interface CMP13NSearchFormConfig extends Config<DocumentTabPanel> {
}

class CMP13NSearchForm extends DocumentTabPanel {
  declare Config: CMP13NSearchFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.personalization.editorplugin.config.cmP13NSearchForm";

  constructor(config: Config<CMP13NSearchForm> = null) {
    super(ConfigUtils.apply(Config(CMP13NSearchForm, {

      items: [
        Config(DocumentForm, {
          itemId: "personalization",
          title: BlueprintTabs_properties.Tab_content_title,
          items: [
            Config(TeaserDocumentForm),
            Config(PropertyFieldGroup, {
              itemId: "cmP13nDefaultContentForm",
              title: PersonalizationDocTypes_properties.CMSelectionRules_defaultContent_text,
              items: [
                Config(LinkListPropertyField, {
                  linkType: "CMTeasable",
                  hideLabel: true,
                  showThumbnails: true,
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
              title: CustomLabels_properties.PropertyGroup_Expression,
              itemId: "cmP13nSearchForm",
              items: [
                Config(SearchQueryPropertyField, {
                  propertyName: "searchQuery",
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                  ...ConfigUtils.append({
                    plugins: [
                      Config(ShowIssuesPlugin, {
                        propertyName: "searchQuery",
                        bindTo: config.bindTo,
                      }),
                    ],
                  }),
                }),
                Config(SearchQueryHelper),
                Config(ContentTypeStringPropertyField, {
                  bindTo: config.bindTo,
                  propertyName: "documentType",
                  linkType: "CMTeasable",
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                }),
                Config(LinkListPropertyField, {
                  propertyName: "searchContext",
                  itemId: "searchContext",
                  bindTo: config.bindTo,
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
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
                "/Settings/Options/Viewtypes/CMP13NSearch/",
                "Options/Viewtypes/CMP13NSearch/",
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

export default CMP13NSearchForm;
