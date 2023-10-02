import ECommerceStudioPlugin from "@coremedia-blueprint/studio-client.main.ec-studio/ECommerceStudioPlugin";
import CatalogSearchContextMenu from "@coremedia-blueprint/studio-client.main.ec-studio/components/search/CatalogSearchContextMenu";
import AddItemsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/AddItemsPlugin";
import Component from "@jangaroo/ext-ts/Component";
import Item from "@jangaroo/ext-ts/menu/Item";
import Separator from "@jangaroo/ext-ts/menu/Separator";
import { cast } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import LivecontextStudioPlugin from "../LivecontextStudioPlugin";
import AugmentCategoryAction from "../action/AugmentCategoryAction";
import AugmentProductAction from "../action/AugmentProductAction";
import CreateMarketingSpotAction from "../action/CreateMarketingSpotAction";
import CreateProductTeaserAction from "../action/CreateProductTeaserAction";
import SearchProductVariantsAction from "../action/SearchProductVariantsAction";

interface AddActionsToCatalogSearchContextMenuPluginConfig extends Config<AddItemsPlugin> {
}

class AddActionsToCatalogSearchContextMenuPlugin extends AddItemsPlugin {
  declare Config: AddActionsToCatalogSearchContextMenuPluginConfig;

  #componentConfig: CatalogSearchContextMenu = null;

  constructor(config: Config<AddActionsToCatalogSearchContextMenuPlugin> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    this$.#componentConfig = cast(CatalogSearchContextMenu, config.cmp.initialConfig);
    super(ConfigUtils.apply(Config(AddActionsToCatalogSearchContextMenuPlugin, {
      items: [
        Config(Item, {
          itemId: LivecontextStudioPlugin.SEARCH_PRODUCT_VARIANTS_MENU_ITEM_ID,
          baseAction: new SearchProductVariantsAction({ catalogObjectExpression: this$.#componentConfig.selectedSearchItemsValueExpression }),
        }),
        Config(Separator),
        Config(Item, {
          itemId: LivecontextStudioPlugin.AUGMENT_PRODUCT_MENU_ITEM_ID,
          baseAction: new AugmentProductAction({ catalogObjectExpression: this$.#componentConfig.selectedSearchItemsValueExpression }),
        }),
        Config(Item, {
          itemId: LivecontextStudioPlugin.CREATE_PRODUCT_TEASER_MENU_ITEM_ID,
          baseAction: new CreateProductTeaserAction({ catalogObjectExpression: this$.#componentConfig.selectedSearchItemsValueExpression }),
        }),
        Config(Item, {
          itemId: LivecontextStudioPlugin.AUGMENT_CATEGORY_MENU_ITEM_ID,
          baseAction: new AugmentCategoryAction({ catalogObjectExpression: this$.#componentConfig.selectedSearchItemsValueExpression }),
        }),
        Config(Item, {
          itemId: LivecontextStudioPlugin.CREATE_MARKETING_SPOT_MENU_ITEM_ID,
          baseAction: new CreateMarketingSpotAction({ catalogObjectExpression: this$.#componentConfig.selectedSearchItemsValueExpression }),
        }),
      ],
      after: [
        Config(Component, { itemId: ECommerceStudioPlugin.OPEN_IN_TAB_MENU_ITEM_ID }),
      ],
    }), config));
  }
}

export default AddActionsToCatalogSearchContextMenuPlugin;
