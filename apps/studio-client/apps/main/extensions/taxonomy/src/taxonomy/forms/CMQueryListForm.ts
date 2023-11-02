import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import DefaultExtraDataForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/DefaultExtraDataForm";
import ContainerViewTypeSelectorForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ContainerViewTypeSelectorForm";
import FixedIndexItemsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/FixedIndexItemsForm";
import MetaDataWithoutSearchableForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MetaDataWithoutSearchableForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import TeaserDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/TeaserDocumentForm";
import ValidityDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/ValidityDocumentForm";
import SpacingBEMEntities from "@coremedia/studio-client.ext.ui-components/bem/SpacingBEMEntities";
import VerticalSpacingPlugin from "@coremedia/studio-client.ext.ui-components/plugins/VerticalSpacingPlugin";
import QueryEditor_properties from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/QueryEditor_properties";
import ContentQueryForm from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/components/ContentQueryForm";
import LinkListConditionEditor from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/conditions/LinkListConditionEditor";
import LinkingTaxonomyConditionEditor from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/conditions/LinkingTaxonomyConditionEditor";
import ModificationDateConditionEditor from "@coremedia/studio-client.main.bpbase-studio-dynamic-query-list/conditions/ModificationDateConditionEditor";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LocationTaxonomyConditionEditor from "../queryeditor/LocationTaxonomyConditionEditor";
import TaxonomyConditionEditor from "../queryeditor/TaxonomyConditionEditor";

interface CMQueryListFormConfig extends Config<DocumentTabPanel>, Partial<Pick<CMQueryListForm,
  "contentType" |
  "folders"
>> {
}

class CMQueryListForm extends DocumentTabPanel {
  declare Config: CMQueryListFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.taxonomy.cmQueryListForm";

  constructor(config: Config<CMQueryListForm> = null) {
    super(ConfigUtils.apply(Config(CMQueryListForm, {

      items: [
        Config(DocumentForm, {
          title: QueryEditor_properties.DCQE_label_conditions,
          itemId: "contentQueryDocumentForm",
          items: [
            Config(ContentQueryForm, {
              bindTo: config.bindTo,
              itemId: "contentQueryForm",
              forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
              queryPropertyName: "localSettings",
              documentTypesPropertyName: "documenttype",
              sortingPropertyName: "order",
              plugins: [
                Config(VerticalSpacingPlugin, { modifier: SpacingBEMEntities.VERTICAL_SPACING_MODIFIER_200 }),
              ],
              conditions: [
                Config(ModificationDateConditionEditor, {
                  bindTo: config.bindTo,
                  propertyName: "freshness",
                  group: "attributes",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                  sortable: true,
                  timeSlots: [
                    {
                      name: "sameDay",
                      text: QueryEditor_properties.DCQE_text_modification_date_same_day,
                      expression: "TODAY",
                    },
                    {
                      name: "sevenDays",
                      text: QueryEditor_properties.DCQE_text_modification_date_seven_days,
                      expression: "7 DAYS TO NOW",
                    },
                    {
                      name: "thirtyDays",
                      text: QueryEditor_properties.DCQE_text_modification_date_thirty_days,
                      expression: "30 DAYS TO NOW",
                    },
                  ],
                }),
                Config(LinkListConditionEditor, {
                  bindTo: config.bindTo,
                  propertyName: "documents",
                  itemId: "contextConditionEditor",
                  group: "attributes",
                  contentType: "CMChannel",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                }),
                Config(LinkListConditionEditor, {
                  bindTo: config.bindTo,
                  propertyName: "authors",
                  itemId: "authorConditionEditor",
                  group: "attributes",
                  contentType: "CMPerson",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                }),
                Config(TaxonomyConditionEditor, {
                  bindTo: config.bindTo,
                  propertyName: "subjecttaxonomy",
                  taxonomyId: "Subject",
                  itemId: "subjectTaxonomyConditionEditor",
                  group: "attributes",
                  contentType: "CMTaxonomy",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                }),
                Config(LocationTaxonomyConditionEditor, {
                  bindTo: config.bindTo,
                  propertyName: "locationtaxonomy",
                  taxonomyId: "Location",
                  itemId: "locationTaxonomyConditionEditor",
                  group: "attributes",
                  contentType: "CMLocTaxonomy",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                }),
                Config(LinkingTaxonomyConditionEditor, {
                  bindTo: config.bindTo,
                  group: "attributes",
                  propertyName: "contextTaxonomies",
                  itemId: "linkingTaxonomyConditionEditor",
                  documentTypes: ["CMArticle", "CMVideo", "CMPicture", "CMGallery", "CMChannel"],
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
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
                "/Settings/Options/Viewtypes/CMQueryList/",
                "Options/Viewtypes/CMQueryList/",
              ]
            }),
            Config(ValidityDocumentForm),
          ],
        }),
        Config(DefaultExtraDataForm),
        Config(MultiLanguageDocumentForm),
        Config(MetaDataWithoutSearchableForm),
      ],

    }), config));
  }

  /**
   * The content type to fill the tree with.
   */
  contentType: string = null;

  /**
   * The comma separated folder values to read the content from.
   */
  folders: string = null;
}

export default CMQueryListForm;
