import augmentedCategoryTreeRelation from "@coremedia-blueprint/studio-client.main.ec-studio/tree/augmentedCategoryTreeRelation";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LivecontextStudioPlugin_properties from "../LivecontextStudioPlugin_properties";
import CommerceAugmentedPageGridForm from "./CommerceAugmentedPageGridForm";
import CommerceCategoryContentForm from "./CommerceCategoryContentForm";
import CommerceCategoryStructureForm from "./CommerceCategoryStructureForm";
import CommerceSystemForm from "./CommerceSystemForm";
import CommerceWorkAreaTab from "./CommerceWorkAreaTab";

interface CommerceCategoryWorkAreaTabConfig extends Config<CommerceWorkAreaTab> {
}

class CommerceCategoryWorkAreaTab extends CommerceWorkAreaTab {
  declare Config: CommerceCategoryWorkAreaTabConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.commerceCategoryWorkAreaTab";

  static readonly CONTENT_TAB_ITEM_ID: string = "contentTab";

  static readonly STRUCTURE_TAB_ITEM_ID: string = "structureTab";

  static readonly SYSTEM_TAB_ITEM_ID: string = "systemTab";

  static readonly PDP_PAGEGRID_TAB_ITEM_ID: string = "pdpPageGridTab";

  #augmentedCategoryExpression: ValueExpression = null;

  constructor(config: Config<CommerceCategoryWorkAreaTab> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    this$.#augmentedCategoryExpression = ValueExpressionFactory.createFromFunction((): any =>
      augmentedCategoryTreeRelation.getParentUnchecked(this$.getEntityExpression().getValue()),
    );
    super(ConfigUtils.apply(Config(CommerceCategoryWorkAreaTab, {

      subTabs: [
        Config(CommerceCategoryContentForm, {
          itemId: CommerceCategoryWorkAreaTab.CONTENT_TAB_ITEM_ID,
          bindTo: this$.getEntityExpression(),
        }),
        Config(CommerceCategoryStructureForm, { itemId: CommerceCategoryWorkAreaTab.STRUCTURE_TAB_ITEM_ID }),
        Config(DocumentForm, {
          itemId: CommerceCategoryWorkAreaTab.PDP_PAGEGRID_TAB_ITEM_ID,
          title: LivecontextStudioPlugin_properties.CMExternalChannel_tab_PDP_pagegrid_title,
          items: [
            Config(CommerceAugmentedPageGridForm, {
              itemId: "pdpPagegrid",
              showLocal: true,
              forceReadOnlyValueExpression: ValueExpressionFactory.createFromValue(true),
              bindTo: this$.#augmentedCategoryExpression,
              pageGridPropertyName: "pdpPagegrid",
              fallbackPageGridPropertyName: "placement",
              virtual: true,
            }),
          ],
        }),
        Config(CommerceSystemForm, {
          autoHide: true,
          itemId: CommerceCategoryWorkAreaTab.SYSTEM_TAB_ITEM_ID,
        }),
      ],

    }), config));
  }
}

export default CommerceCategoryWorkAreaTab;
