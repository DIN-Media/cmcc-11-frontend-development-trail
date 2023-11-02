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
import SelectionRulesField from "@coremedia/studio-client.main.cap-personalization-ui/SelectionRulesField";
import BooleanCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/BooleanCondition";
import DateCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/DateCondition";
import DateTimeCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/DateTimeCondition";
import EnumCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/EnumCondition";
import IntegerCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/IntegerCondition";
import PercentageKeywordCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/PercentageKeywordCondition";
import SegmentCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/SegmentCondition";
import StringCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/StringCondition";
import TimeCondition from "@coremedia/studio-client.main.cap-personalization-ui/condition/TimeCondition";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import QuickCreateLinklistMenu from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateLinklistMenu";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import PersonalizationContext_properties from "./PersonalizationContext_properties";
import PersonalizationDocTypes_properties from "./PersonalizationDocTypes_properties";
import PersonalizationPlugIn_properties from "./PersonalizationPlugIn_properties";

interface CMSelectionRulesFormConfig extends Config<DocumentTabPanel> {
}

class CMSelectionRulesForm extends DocumentTabPanel {
  declare Config: CMSelectionRulesFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.personalization.editorplugin.config.cmSelectionRulesForm";

  constructor(config: Config<CMSelectionRulesForm> = null) {
    super(ConfigUtils.apply(Config(CMSelectionRulesForm, {

      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm, { collapsed: true }),
            Config(PropertyFieldGroup, {
              itemId: "cmSelectionRulesPicturesForm",
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

            /* workaround for rulesField-layout-bug: use custom margin style */
            Config(PropertyFieldGroup, {
              itemId: "cmSelectionRulesConditionsForm",
              title: PersonalizationDocTypes_properties.CMSelectionRules_rules_text,
              items: [
                Config(SelectionRulesField, {
                  itemId: "rules",
                  propertyName: "rules",
                  hideLabel: true,
                  allowedContentType: "CMTeasable",
                  conditionItems: [
                    Config(TimeCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_system_timeOfDay,
                      propertyName: "system.timeOfDay",
                    }),
                    Config(DateCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_system_date,
                      propertyName: "system.date",
                    }),
                    Config(DateTimeCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_system_dateTime,
                      propertyName: "system.dateTime",
                    }),
                    Config(EnumCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_system_dayOfWeek,
                      propertyName: "system.dayOfWeek",
                      operators: ["lt", "eq", "gt"],
                      operatorNames: {
                        lt: PersonalizationPlugIn_properties.con_day_week_lt,
                        eq: PersonalizationPlugIn_properties.con_day_week_eq,
                        gt: PersonalizationPlugIn_properties.con_day_week_gt,
                      },
                      value: [
                        [1, PersonalizationContext_properties.p13n_context_system_dayOfWeek_1],
                        [2, PersonalizationContext_properties.p13n_context_system_dayOfWeek_2],
                        [3, PersonalizationContext_properties.p13n_context_system_dayOfWeek_3],
                        [4, PersonalizationContext_properties.p13n_context_system_dayOfWeek_4],
                        [5, PersonalizationContext_properties.p13n_context_system_dayOfWeek_5],
                        [6, PersonalizationContext_properties.p13n_context_system_dayOfWeek_6],
                        [7, PersonalizationContext_properties.p13n_context_system_dayOfWeek_7],
                      ],
                    }),
                    Config(StringCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_referrer,
                      propertyName: "referrer.url",
                    }),
                    Config(StringCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_referrer_query,
                      propertyName: "referrer.query",
                      operators: ["ct"],
                    }),
                    Config(EnumCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_referrer_searchengine,
                      propertyName: "referrer.searchengine",
                      operators: ["eq", "ne"],
                      value: [
                        ["bing", "Bing"],
                        ["google", "Google"],
                        ["yahoo", "Yahoo!"],
                      ],
                    }),
                    Config(EnumCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_location_city,
                      propertyName: "location.city",
                      operators: ["eq", "ne"],
                      value: [
                        ["\"Hamburg\"", "Hamburg"],
                        ["\"SanFrancisco\"", "San Francisco"],
                        ["\"London\"", "London"],
                        ["\"Singapore\"", "Singapore"],
                      ],
                    }),
                    Config(SegmentCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_segment,
                      propertyPrefix: "segment",
                    }),
                    Config(PercentageKeywordCondition, {
                      conditionName: PersonalizationContext_properties.p13n_context_keyword,
                      propertyPrefix: "keyword",
                      isDefault: true,
                    }),
                    Config(IntegerCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_number_of_comments,
                      propertyName: "es_check.numberOfComments",
                      operator: "greater than",
                    }),
                    Config(IntegerCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_number_of_ratings,
                      propertyName: "es_check.numberOfRatings",
                      operator: "greater than",
                    }),
                    Config(IntegerCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_number_of_likes,
                      propertyName: "es_check.numberOfLikes",
                      operator: "greater than",
                    }),
                    Config(IntegerCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_number_of_explicit_interests,
                      propertyName: "explicit.numberOfExplicitInterests",
                      operator: "greater than",
                    }),
                    Config(BooleanCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_social_background_login,
                      propertyName: "es_check.userLoggedIn",
                      checkedByDefault: "true",
                    }),
                    Config(EnumCondition, {
                      conditionName: PersonalizationPlugIn_properties.con_gender,
                      propertyName: "socialuser.gender",
                      operators: ["eq", "ne"],
                      value: [
                        ["male", PersonalizationPlugIn_properties.con_gender_male],
                        ["female", PersonalizationPlugIn_properties.con_gender_female],
                      ],
                    }),
                  ],
                }),
              ],
            }),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/CMSelectionRules/",
                "Options/Viewtypes/CMSelectionRules/",
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

export default CMSelectionRulesForm;
