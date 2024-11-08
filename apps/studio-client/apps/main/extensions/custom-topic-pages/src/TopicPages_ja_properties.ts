import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import TopicPages_properties from "./TopicPages_properties";

/**
 * Overrides of ResourceBundle "TopicPages" for Locale "ja".
 * @see TopicPages_properties#INSTANCE
 */
ResourceBundleUtil.override(TopicPages_properties, {
  TopicPages_administration_title: "Topic Pages",
  TopicPages_grid_header_name: "Keyword",
  TopicPages_grid_header_page: "Edited Page",
  TopicPages_grid_header_options: "Enabled",
  TopicPages_search_emptyText: "Search…",
  TopicPages_search_search_tooltip: "Start search",
  TopicPages_taxonomy_combo_title: "Show Tags",
  TopicPages_taxonomy_combo_emptyText: "No tags available",
  TopicPages_create_link: "Create Manually Edited Page",
  TopicPages_deletion_title: "Delete Topic Page",
  TopicPages_deletion_tooltip: "Delete Topic Page",
  TopicPages_deletion_text: "Do you really want to delete the custom topic page '{0}'?",
  TopicPages_root_channel_checked_out_title: "Main Page Error",
  TopicPages_root_channel_checked_out_msg: "The main page of the active site '{0}' is checked out by another user.",
  TopicPages_root_channel_not_found_title: "Main Page Error",
  TopicPages_root_channel_not_found_msg: "The main page of the active site '{0}' could not be resolved. The update of the topic page linking failed.",
});
