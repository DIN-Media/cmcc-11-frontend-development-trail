import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import WorkArea from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkArea";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Ext from "@jangaroo/ext-ts";
import Action from "@jangaroo/ext-ts/Action";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import { as } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import TaxonomyEditor from "../administration/TaxonomyEditor";
import OpenTaxonomyEditorAction from "./OpenTaxonomyEditorAction";

interface OpenTaxonomyEditorActionBaseConfig extends Config<Action> {
}

/** Opens the TaxonomyEditor **/
class OpenTaxonomyEditorActionBase extends Action {
  declare Config: OpenTaxonomyEditorActionBaseConfig;

  #taxonomyId: string = null;

  readonly items: Array<any>;

  constructor(config: Config<OpenTaxonomyEditorAction> = null) {
    config.handler = OpenTaxonomyEditorActionBase.showTaxonomyAdministrationWithLatestSelection;
    super(config);
    this.#taxonomyId = config.taxonomyId;
  }

  static showTaxonomyAdministrationWithLatestSelection(): void {
    OpenTaxonomyEditorActionBase.#openTaxonomyAdministration();

    EventUtil.invokeLater((): void => {
      const taxonomyAdminTab = as(Ext.getCmp("taxonomyEditor"), TaxonomyEditor);
      taxonomyAdminTab.showNodeSelectedNode();
    });
  }

  /**
   * Static call to open the taxonomy admin console.
   */
  static #openTaxonomyAdministration(): void {
    const workArea = as(editorContext._.getWorkArea(), WorkArea);
    const taxonomyAdminTab = as(Ext.getCmp("taxonomyEditor"), TaxonomyEditor);

    if (!taxonomyAdminTab) {
      const workAreaTabType = workArea.getTabTypeById(TaxonomyEditor.xtype);
      workAreaTabType.createTab(null, (tab: Panel): void => {
        const editor = as(tab, TaxonomyEditor);
        workArea.addTab(workAreaTabType, editor);
        workArea.setActiveTab(editor);
      });
    } else {
      workArea.setActiveTab(taxonomyAdminTab);
    }
  }
}

export default OpenTaxonomyEditorActionBase;
