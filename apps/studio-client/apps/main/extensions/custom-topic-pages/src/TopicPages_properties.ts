import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";

/**
 * Interface values for ResourceBundle "TopicPages".
 * @see TopicPages_properties#INSTANCE
 */
interface TopicPages_properties {

/**
 *###################################################################################
 * Administration
 *###################################################################################
 */
  TopicPages_administration_title: string;
  TopicPages_administration_icon: string;
  TopicPages_grid_header_name: string;
  TopicPages_search_title: string;
  TopicPages_grid_header_page: string;
  TopicPages_grid_header_options: string;
  TopicPages_search_emptyText: string;
  TopicPages_search_search_tooltip: string;
  TopicPages_taxonomy_combo_title: string;
  TopicPages_taxonomy_combo_emptyText: string;
  TopicPages_create_link: string;
  TopicPages_no_preferred_site: string;
  TopicPages_no_channel_configured_title: string;
  TopicPages_no_channel_configured: string;
  TopicPages_name: string;
  TopicPages_page_icon: string;
  TopicPages_icon: string;
  TopicPages_filtered: string;
  TopicPages_deletion_title: string;
  TopicPages_deletion_tooltip: string;
  TopicPages_deletion_text: string;
  TopicPages_root_channel_checked_out_title: string;
  TopicPages_root_channel_checked_out_msg: string;
  TopicPages_root_channel_not_found_title: string;
  TopicPages_root_channel_not_found_msg: string;
  topic_pages_button_tooltip: string;
  topic_pages_button_no_preferred_site_tooltip: string;
  topic_pages_button_no_topic_page_settings_tooltip: string;
}

/**
 * Singleton for the current user Locale's instance of ResourceBundle "TopicPages".
 * @see TopicPages_properties
 */
const TopicPages_properties: TopicPages_properties = {
  TopicPages_administration_title: "Topic Pages",
  TopicPages_administration_icon: CoreIcons_properties.taxonomy,
  TopicPages_grid_header_name: "Tag",
  TopicPages_search_title: "Search",
  TopicPages_grid_header_page: "Edited Page",
  TopicPages_grid_header_options: "Enabled",
  TopicPages_search_emptyText: "Search…",
  TopicPages_search_search_tooltip: "Start search",
  TopicPages_taxonomy_combo_title: "Show Tag",
  TopicPages_taxonomy_combo_emptyText: "No Tags Available",
  TopicPages_create_link: "Create Manually Edited Page",
  TopicPages_no_preferred_site: "<i>No preferred site selected<\/i>",
  TopicPages_no_channel_configured_title: "Error",
  TopicPages_no_channel_configured: "The topic page settings for the active page '{0}' does not link to a default layout for new topic pages.",
  TopicPages_name: "Topic Page",
  TopicPages_page_icon: CoreIcons_properties.type_page,
  TopicPages_icon: CoreIcons_properties.taxonomy,
  TopicPages_filtered: "Not all topic pages are displayed, enter a search term to reduce the amount of topic pages.",
  TopicPages_deletion_title: "Delete Topic Page",
  TopicPages_deletion_tooltip: "Delete Topic Page",
  TopicPages_deletion_text: "Do you really want to delete the custom topic page '{0}'?",
  TopicPages_root_channel_checked_out_title: "Error",
  TopicPages_root_channel_checked_out_msg: "The main page content of the active site '{0}' is checked out by another user.",
  TopicPages_root_channel_not_found_title: "Error",
  TopicPages_root_channel_not_found_msg: "The main page content of the active site '{0}' could not be resolved. The update of the topic page linking failed.",
  topic_pages_button_tooltip: "Open Topic Pages",
  topic_pages_button_no_preferred_site_tooltip: "Please select a preferred site to open the topic pages.",
  topic_pages_button_no_topic_page_settings_tooltip: "No root channel for topic pages found. Please check the 'TopicPages' settings of the preferred site.",
};

export default TopicPages_properties;
