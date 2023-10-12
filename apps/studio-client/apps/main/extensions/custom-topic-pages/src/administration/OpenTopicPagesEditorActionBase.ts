import TopicsHelper from "@coremedia-blueprint/studio-client.main.taxonomy-studio/TopicsHelper";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import Bean from "@coremedia/studio-client.client-core/data/Bean";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import WorkArea from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkArea";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Ext from "@jangaroo/ext-ts";
import Action from "@jangaroo/ext-ts/Action";
import Component from "@jangaroo/ext-ts/Component";
import Button from "@jangaroo/ext-ts/button/Button";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import { as, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import { AnyFunction } from "@jangaroo/runtime/types";
import TopicPages_properties from "../TopicPages_properties";
import OpenTopicPagesEditorAction from "./OpenTopicPagesEditorAction";
import TopicPagesEditor from "./TopicPagesEditor";
import TopicPagesEditorBase from "./TopicPagesEditorBase";

interface OpenTopicPagesEditorActionBaseConfig extends Config<Action> {
}

/** Opens the Topic page editor **/
class OpenTopicPagesEditorActionBase extends Action {
  declare Config: OpenTopicPagesEditorActionBaseConfig;

  readonly items: Array<any>;

  constructor(config: Config<OpenTopicPagesEditorAction> = null) {
    config.handler = OpenTopicPagesEditorActionBase.#showTopicPagesEditor;
    super(config);
  }

  override addComponent(comp: Component): void {
    super.addComponent(comp);
    OpenTopicPagesEditorActionBase.isAdministrationEnabled((enabled: boolean): void => {
      this.setDisabled(!enabled);
      OpenTopicPagesEditorActionBase.#updateTooltip(enabled);
    });
    editorContext._.getSitesService().getPreferredSiteIdExpression().addChangeListener(bind(this, this.#preferredSiteChangedHandler));
  }

  #preferredSiteChangedHandler(): void {
    OpenTopicPagesEditorActionBase.isAdministrationEnabled((enabled: boolean): void => {
      this.setDisabled(!enabled);
      OpenTopicPagesEditorActionBase.#updateTooltip(enabled);
      if (!enabled) {
        const topicPagesAdminTab = as(Ext.getCmp(TopicPagesEditorBase.TOPIC_PAGES_EDITOR_ID), TopicPagesEditor);
        if (topicPagesAdminTab) {
          topicPagesAdminTab.destroy();
        }
      }
    });
  }

  static #updateTooltip(enabled: boolean): void {
    let msg = Editor_properties.Button_disabled_insufficient_privileges;

    const preferredSiteId = editorContext._.getSitesService().getPreferredSiteId();
    if (!preferredSiteId) {
      msg = TopicPages_properties.topic_pages_button_no_preferred_site_tooltip;
    }

    TopicsHelper.loadSettings((settingsRemoteBean: Bean): void => {
      const topicPageChannel: Content = settingsRemoteBean.get("topicPageChannel");
      if (!topicPageChannel) {
        msg = TopicPages_properties.topic_pages_button_no_topic_page_settings_tooltip;
        OpenTopicPagesEditorActionBase.#setButtonTooltip(msg);
      }
    });

    if (enabled) {
      msg = TopicPages_properties.topic_pages_button_tooltip;
    }

    OpenTopicPagesEditorActionBase.#setButtonTooltip(msg);
  }

  static #setButtonTooltip(msg: string): void {
    const button = (as(Ext.getCmp("btn-topicpages-editor"), Button));
    if (button) {
      button.setTooltip(msg);
    }
  }

  /**
   * Returns true if the current user can administrate the taxonomies.
   * @return
   */
  static isAdministrationEnabled(callback: AnyFunction): void {
    TopicsHelper.loadSettings((settingsRemoteBean: Bean): void => {
      const topicPageChannel: Content = settingsRemoteBean.get("topicPageChannel");
      if (!topicPageChannel) {
        console.log("[INFO]", "Topic Pages: could not find root channel for topic pages, please check the TopicPages settings document of the preferred site.");
        callback.call(null, false);
      }
      else {
        callback.call(null, true);
      }
    });
  }

  /**
   * Static call to open the taxonomy admin console.
   */
  static #showTopicPagesEditor(): void {
    const workArea = as(editorContext._.getWorkArea(), WorkArea);
    let topicPagesAdminTab = as(Ext.getCmp(TopicPagesEditorBase.TOPIC_PAGES_EDITOR_ID), TopicPagesEditor);

    if (!topicPagesAdminTab) {
      const workAreaTabType = workArea.getTabTypeById(TopicPagesEditor.xtype);
      workAreaTabType.createTab(null, (tab: Panel): void => {
        topicPagesAdminTab = as(tab, TopicPagesEditor);
        workArea.addTab(workAreaTabType, topicPagesAdminTab);
      });
    }
    workArea.setActiveTab(topicPagesAdminTab);
  }

  override removeComponent(comp: Component): void {
    super.removeComponent(comp);
    if (this.items && this.items.length === 0) {
      editorContext._.getSitesService().getPreferredSiteIdExpression().removeChangeListener(bind(this, this.#preferredSiteChangedHandler));
    }
  }
}

export default OpenTopicPagesEditorActionBase;
