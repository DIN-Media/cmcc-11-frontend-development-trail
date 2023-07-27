import ContentLocalizationUtil from "@coremedia/studio-client.cap-base-models/content/ContentLocalizationUtil";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import BeanRecord from "@coremedia/studio-client.ext.ui-components/store/BeanRecord";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import LoadMask from "@jangaroo/ext-ts/LoadMask";
import Container from "@jangaroo/ext-ts/container/Container";
import ExtEvent from "@jangaroo/ext-ts/event/Event";
import GridPanel from "@jangaroo/ext-ts/grid/Panel";
import { as, bind, is } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import KeyEvent from "@jangaroo/runtime/KeyEvent";
import int from "@jangaroo/runtime/int";
import { AnyFunction } from "@jangaroo/runtime/types";
import TaxonomyNode from "../TaxonomyNode";
import TaxonomyNodeList from "../TaxonomyNodeList";
import TaxonomyStudioPlugin_properties from "../TaxonomyStudioPlugin_properties";
import TaxonomyUtil from "../TaxonomyUtil";
import TaxonomyRenderFactory from "../rendering/TaxonomyRenderFactory";
import TaxonomyCache from "./TaxonomyCache";
import TaxonomySuggestionsLinkListPanel from "./TaxonomySuggestionsLinkListPanel";

interface TaxonomySuggestionsLinkListPanelBaseConfig extends Config<GridPanel>, Partial<Pick<TaxonomySuggestionsLinkListPanelBase,
  "taxonomyIdExpression" |
  "disableSuggestions"
>> {
}

/**
 *
 */
class TaxonomySuggestionsLinkListPanelBase extends GridPanel {
  declare Config: TaxonomySuggestionsLinkListPanelBaseConfig;

  #suggestionsExpression: ValueExpression = null;

  #bindTo: ValueExpression = null;

  #propertyValueExpression: ValueExpression = null;

  taxonomyIdExpression: ValueExpression = null;

  #loadMask: LoadMask = null;

  #cache: TaxonomyCache = null;

  #selectedPositionsExpression: ValueExpression = null;

  #selectedItemsExpression: ValueExpression = null;

  #documentFormParent: DocumentForm = null;

  #needsUpdateAfterContentChange: boolean = false;

  /**
   * If true, suggestions field is not shown, default is false.
   */
  disableSuggestions: boolean = false;

  /**
   * @param config The configuration options. See the config class for details.
   *
   * @see com.coremedia.cms.editor.sdk.config.linkListPropertyFieldGridPanelBase
   */
  constructor(config: Config<TaxonomySuggestionsLinkListPanel> = null) {
    super(config);

    this.#documentFormParent = as(this.findParentBy((cont: Container): boolean =>
      is(cont, DocumentForm) && cont.up() && is(cont.up(), DocumentTabPanel),
    ), DocumentForm);

    if (this.#documentFormParent) {
      this.mon(this.#documentFormParent, "activate", bind(this, this.#documentFormActivated));
    }

    if (!config.disableSuggestions) {
      this.#bindTo = config.bindTo;

      this.#propertyValueExpression = this.#bindTo.extendBy("properties." + config.propertyName);
      this.#propertyValueExpression.addChangeListener(bind(this, this.#propertyChanged));
      this.#bindTo.addChangeListener(bind(this, this.#contentChanged));

      this.#cache = new TaxonomyCache(this.#bindTo, this.#propertyValueExpression, config.taxonomyIdExpression);
    }
  }

  protected override afterRender(): void {
    super.afterRender();

    const loadMaskCfg = Config(LoadMask, { target: this });
    loadMaskCfg.msg = TaxonomyStudioPlugin_properties.TaxonomySuggestions_loading;
    this.#loadMask = new LoadMask(loadMaskCfg);
    this.#loadMask.disable();

    this.#updateSuggestions(true);

    this.getEl().addListener("keyup", (evt: ExtEvent): void => {
      if (evt.getKey() === KeyEvent.DOM_VK_ENTER || evt.getKey() === KeyEvent.DOM_VK_RETURN) {
        const values: Array<any> = this.getSelectedValuesExpression().getValue();
        if (values.length > 0) {
          const selection: Content = values[0];
          const ref = TaxonomyUtil.parseRestId(selection);
          this.plusMinusClicked(ref);
        }
      }
    });
  }

  /**
   * Compute the disabled state of the add-all button.
   *
   * @param forceReadOnlyValueExpression the expression
   * @return a VE which disables the add all button
   */
  protected getAddAllDisabledVE(forceReadOnlyValueExpression: ValueExpression): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): boolean => {
      const readOnly: boolean = forceReadOnlyValueExpression && forceReadOnlyValueExpression.getValue();
      const suggestions: Array<any> = (this.getSuggestionsExpression() && this.getSuggestionsExpression().getValue()) || [];
      return readOnly || suggestions.length === 0;
    });
  }

  /**
   * Fired when the taxonomy property of the content has been changed.
   * We use this event to refresh (not reload) the taxonomy list.
   */
  #propertyChanged(): void {
    this.#updateSuggestions(false);
  }

  /**
   * Fired when the underlying content changes
   * We use this event to refresh and reload the taxonomy list iff
   * the surrounding content form is visible. Otherwise, wait
   * for activation.
   */
  #contentChanged(): void {
    if (this.#documentFormParent && this.#documentFormParent.isVisible()) {
      this.#updateSuggestions(true);
      this.#needsUpdateAfterContentChange = false;
    } else {
      this.#needsUpdateAfterContentChange = true;
    }
  }

  /**
   * After activation of the surrounding content form
   */
  #documentFormActivated(): void {
    if (this.#needsUpdateAfterContentChange) {
      this.#updateSuggestions(true);
      this.#needsUpdateAfterContentChange = false;
    }
  }

  protected getSuggestionsExpression(): ValueExpression {
    if (!this.#suggestionsExpression) {
      this.#suggestionsExpression = ValueExpressionFactory.create("hits", beanFactory._.createLocalBean());
    }
    return this.#suggestionsExpression;
  }

  //noinspection JSMethodCanBeStatic
  protected formatUnreadableName(record: BeanRecord): string {
    const content = as(record.getBean(), Content);
    return ContentLocalizationUtil.formatNotReadableName(content);
  }

  /**
   * Loads the values into the list.
   */
  #updateSuggestions(reload: boolean = false): void {
    if (!this.initialConfig.disableSuggestions) {
      this.setBusy(true);

      const callback: AnyFunction = (list: TaxonomyNodeList): void => {
        if (list) {
          this.#convertResultToContentList(list);
        }
      };

      if (reload) {
        this.#cache.invalidate(callback);
      } else {
        this.#cache.loadSuggestions(callback);
      }
    }
  }

  /**
   * Updates the empty list label so that loading is indicated.
   * @param busy
   */
  setBusy(busy: boolean = false): void {
    if (!this.#loadMask) {
      return;
    }

    if (busy) {
      this.#loadMask.show();
    } else {
      this.#loadMask.hide();
    }
  }

  /**
   * Return a value expression evaluating to an array of selected positions (indexes) in the link list.
   * @return a value expression evaluating to an array of selected position
   */
  getSelectedPositionsExpression(): ValueExpression {
    return this.#selectedPositionsExpression || (this.#selectedPositionsExpression = ValueExpressionFactory.create("positions", beanFactory._.createLocalBean()));
  }

  /**
   * Return a value expression evaluating to an array of selected values (elements) in the link list.
   * @return a value expression evaluating to an array of selected values
   */
  getSelectedValuesExpression(): ValueExpression {
    return this.#selectedItemsExpression || (this.#selectedItemsExpression = ValueExpressionFactory.create("items", beanFactory._.createLocalBean()));
  }

  #convertResultToContentList(list: TaxonomyNodeList): void {
    const items = list.getNodes();
    const contents = [];
    let callbackCount: int = items.length;
    for (let i = 0; i < items.length; i++) {
      const item: TaxonomyNode = items[i];
      const child = as(beanFactory._.getRemoteBean(item.getRef()), Content);
      child.load((bean: Content): void => {
        contents.push(bean);
        callbackCount--;
        if (callbackCount === 0) {
          this.getSuggestionsExpression().setValue(contents);

          //force re-rendering of records (e.g. if suggestion evaluation type has been changed)
          for (let i = 0; i < this.getStore().getCount(); i++) {
            const record = as(this.getStore().getAt(i), BeanRecord);
            record.data.html = null;
            record.commit();
          }
          this.setBusy(false);
        }
      });
    }
    if (items.length === 0) {
      this.getSuggestionsExpression().setValue([]);
      this.setBusy(false);
    }
  }

  /**
   * Adds all items of the list to the keyword list.
   */
  addAllKeywordsHandler(): void {
    const suggestions: Array<any> = this.getSuggestionsExpression().getValue();
    const existingEntries: Array<any> = this.#propertyValueExpression.getValue();
    const newEntries = [];
    for (let i = 0; i < existingEntries.length; i++) {
      newEntries.push(existingEntries[i]);
    }
    for (let j = 0; j < suggestions.length; j++) {
      newEntries.push(suggestions[j]);
    }
    this.#propertyValueExpression.setValue(newEntries);
    this.#updateSuggestions(false);
  }

  /**
   * Trigger a new evaluation of the content for suggestions.
   */
  protected reloadKeywordsHandler(): void {
    this.#updateSuggestions(true);
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * Displays each path item of a taxonomy
   * @param value
   * @param metaData
   * @param record
   * @return
   */
  protected taxonomyRenderer(value: any, metaData: any, record: BeanRecord): string {
    this.taxonomyIdExpression.loadValue((taxonomyId: string): void =>
      TaxonomyUtil.loadTaxonomyPath(record, this.#bindTo.getValue(), taxonomyId, (updatedRecord: BeanRecord): void => {
        const content = as(record.getBean(), Content);
        const renderer = TaxonomyRenderFactory.createSuggestionsRenderer(record.data.nodes, this.getId(), this.#cache.getWeight(content.getId()));
        renderer.doRender((html: string): void => {
          if (record.data.html !== html) {
            record.data.html = html;
            record.commit(false);
          }
        });
      }),
    );

    if (!record.data.html) {
      return "<div class='loading'>" + TaxonomyStudioPlugin_properties.TaxonomyLinkList_status_loading_text + "</div>";
    }
    return record.data.html;
  }

  //noinspection JSUnusedGlobalSymbols
  /**
   * Removes the given taxonomy
   */
  plusMinusClicked(nodeRef: string): void {
    TaxonomyUtil.removeNodeFromSelection(this.#propertyValueExpression, nodeRef);
    TaxonomyUtil.addNodeToSelection(this.#propertyValueExpression, nodeRef);
  }

  protected override onDestroy(): void {
    this.#bindTo && this.#bindTo.removeChangeListener(bind(this, this.#contentChanged));
    this.#documentFormParent && this.mun(this.#documentFormParent, "activate", bind(this, this.#documentFormActivated));
    this.#propertyValueExpression && this.#propertyValueExpression.removeChangeListener(bind(this, this.#propertyChanged));

    this.#loadMask && this.#loadMask.destroy();

    super.onDestroy();
  }
}

export default TaxonomySuggestionsLinkListPanelBase;
