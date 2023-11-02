import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import BlueprintTabs_properties from "../BlueprintTabs_properties";
import DefaultExtraDataForm from "./components/DefaultExtraDataForm";
import ContainerViewTypeSelectorForm from "./containers/ContainerViewTypeSelectorForm";
import ItemsForm from "./containers/ItemsForm";
import MetaDataWithoutSearchableForm from "./containers/MetaDataWithoutSearchableForm";
import MultiLanguageDocumentForm from "./containers/MultiLanguageDocumentForm";
import TeaserDocumentForm from "./containers/TeaserDocumentForm";
import ValidityDocumentForm from "./containers/ValidityDocumentForm";

interface CMCollectionFormConfig extends Config<DocumentTabPanel> {
}

class CMCollectionForm extends DocumentTabPanel {
  declare Config: CMCollectionFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.cmCollectionForm";

  constructor(config: Config<CMCollectionForm> = null) {
    super(ConfigUtils.apply(Config(CMCollectionForm, {

      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          itemId: "contentTab",
          items: [
            Config(TeaserDocumentForm),
            Config(ItemsForm),
            Config(ContainerViewTypeSelectorForm, {
              collapsed: false,
              paths: [
                "/Settings/Options/Viewtypes/CMCollection/",
                "Options/Viewtypes/CMCollection/",
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
}

export default CMCollectionForm;
