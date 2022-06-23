import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";

interface DerivedNavigationContextField_properties {
  fieldLabel_text: string;

  gridpanel_columns_type_text: string;
  gridpanel_columns_name_text: string;
  gridpanel_columns_status_text: string;

  toolbar_openFolderPropertiesInTab_text: string;
  toolbar_openFolderPropertiesInTab_icon: string;
  toolbar_openFolderPropertiesInTab_tooltip: string;

}

const DerivedNavigationContextField_properties: DerivedNavigationContextField_properties = {
  fieldLabel_text: "Derived Contexts",
  gridpanel_columns_type_text: "Type",
  gridpanel_columns_name_text: "Name",
  gridpanel_columns_status_text: "State",
  toolbar_openFolderPropertiesInTab_text: "Open folder properties in tab",
  toolbar_openFolderPropertiesInTab_icon: CoreIcons_properties.type_settings,
  toolbar_openFolderPropertiesInTab_tooltip: "Open folder properties in tab",
};

export default DerivedNavigationContextField_properties;
