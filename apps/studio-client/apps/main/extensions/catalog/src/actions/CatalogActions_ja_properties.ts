import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import CatalogActions_properties from "./CatalogActions_properties";

/**
 * Overrides of ResourceBundle "CatalogActions" for Locale "ja".
 * @see CatalogActions_properties#INSTANCE
 */
ResourceBundleUtil.override(CatalogActions_properties, {
  Action_unlink_text: "削除",
  Action_unlink_tooltip: "現在のカテゴリーから削除します。他のカテゴリーに保存します。",
  Action_unlink_title: "アイテムを削除する",
  Action_unlink_message: "カテゴリー '{0}' から現在のアイテムを削除しますか?",
});
