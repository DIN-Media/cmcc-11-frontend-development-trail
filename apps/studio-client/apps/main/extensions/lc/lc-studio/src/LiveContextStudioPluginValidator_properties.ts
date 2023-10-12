
/**
 * Interface values for ResourceBundle "LiveContextStudioPluginValidator".
 * @see LiveContextStudioPluginValidator_properties#INSTANCE
 */
interface LiveContextStudioPluginValidator_properties {

/**
 *Generic error independent of the catalog entity type
 */
  Validator_catalogError_text: string;
/**
 *Validator_MyDocumentType_MyIssue_text=if you want to localize for the MyDocumentType the issue 'MyIssue'
 */
  Validator_CMProductTeaser_EmptyExternalId_text: string;
  Validator_CMProductTeaser_InvalidId_text: string;
  Validator_CMProductTeaser_InvalidStoreContext_text: string;
  Validator_CMProductTeaser_StoreContextNotFound_text: string;
  Validator_CMProductTeaser_CatalogNotFoundError_text: string;
  Validator_CMExternalChannel_EmptyCategory_text: string;
  Validator_CMExternalChannel_InvalidId_text: string;
  Validator_CMExternalChannel_InvalidStoreContext_text: string;
  Validator_CMExternalChannel_CatalogNotFoundError_text: string;
  Validator_CMExternalProduct_EmptyProduct_text: string;
  Validator_CMExternalProduct_InvalidId_text: string;
  Validator_CMExternalProduct_InvalidStoreContext_text: string;
  Validator_CMExternalProduct_CatalogNotFoundError_text: string;
  Validator_CMExternalPage_EmptyExternalPageId_text: string;
  Validator_CMMarketingSpot_EmptyExternalId_text: string;
  Validator_CMMarketingSpot_InvalidId_text: string;
  Validator_CMMarketingSpot_InvalidStoreContext_text: string;
  Validator_CMMarketingSpot_StoreContextNotFound_text: string;
  Validator_CMProductList_InvalidId_text: string;
  Validator_CMProductList_InvalidStoreContext_text: string;
  Validator_CMProductList_StoreContextNotFound_text: string;
  Validator_CMProductList_CatalogNotFoundError_text: string;
  Validator_CMProductList_DocTypeNotSupported_text: string;
  Validator_CMProductList_legacy_value_text: string;
  Validator_CMProductList_invalid_multi_facet_text: string;
  Validator_CMProductList_invalid_multi_facet_query_text: string;
  Validator_CMChannel_SegmentReservedCharsFound_text: string;
  Validator_CMChannel_SegmentReservedPrefix_text: string;
  Validator_CMChannel_SegmentReservedSuffix_text: string;
  Validator_CMChannel_FallbackSegmentReservedCharsFound_text: string;
  Validator_CMChannel_FallbackSegmentReservedPrefix_text: string;
  Validator_CMChannel_FallbackSegmentReservedSuffix_text: string;
  Validator_possibly_missing_master_reference_from_master_augmentation_text: string;
  Validator_possibly_missing_master_reference_from_derived_augmentation_text: string;
}

/**
 * Singleton for the current user Locale's instance of ResourceBundle "LiveContextStudioPluginValidator".
 * @see LiveContextStudioPluginValidator_properties
 */
const LiveContextStudioPluginValidator_properties: LiveContextStudioPluginValidator_properties = {
  Validator_catalogError_text: "Catalog could not be loaded. An unexpected catalog error occurred.",
  Validator_CMProductTeaser_EmptyExternalId_text: "No product is linked. The teaser should have a reference to a product.",
  Validator_CMProductTeaser_InvalidId_text: "Product with code \"{0}\" does not exist in catalog \"{1}\".",
  Validator_CMProductTeaser_InvalidStoreContext_text: "Product could not be loaded. Catalog configuration is not valid.",
  Validator_CMProductTeaser_StoreContextNotFound_text: "The catalog configuration for the current content could not be found.",
  Validator_CMProductTeaser_CatalogNotFoundError_text: "Catalog \"{0}\" could not be found for Product with ID \"{1}\".",
  Validator_CMExternalChannel_EmptyCategory_text: "This field must not be empty. The page must have a reference to a category.",
  Validator_CMExternalChannel_InvalidId_text: "Category with ID \"{0}\" does not exist in catalog \"{1}\".",
  Validator_CMExternalChannel_InvalidStoreContext_text: "Catalog could not be loaded. Catalog configuration is missing or not valid.",
  Validator_CMExternalChannel_CatalogNotFoundError_text: "Catalog \"{0}\" could not be found for Category with ID \"{1}\".",
  Validator_CMExternalProduct_EmptyProduct_text: "This field must not be empty. The page must have a reference to a product.",
  Validator_CMExternalProduct_InvalidId_text: "Product with ID \"{0}\" does not exist in catalog \"{1}\".",
  Validator_CMExternalProduct_InvalidStoreContext_text: "Product could not be loaded. Catalog configuration is missing or not valid.",
  Validator_CMExternalProduct_CatalogNotFoundError_text: "Catalog \"{0}\" could not be found for Product with ID \"{1}\".",
  Validator_CMExternalPage_EmptyExternalPageId_text: "The external page ID is empty. The page must have a reference to an external page.",
  Validator_CMMarketingSpot_EmptyExternalId_text: "No e-Marketing Spot is linked. The teaser should have a reference to an e-Marketing Spot.",
  Validator_CMMarketingSpot_InvalidId_text: "e-Marketing Spot with code \"{0}\" does not exist in catalog \"{1}\".",
  Validator_CMMarketingSpot_InvalidStoreContext_text: "e-Marketing Spot could not be loaded. Catalog configuration is not valid.",
  Validator_CMMarketingSpot_StoreContextNotFound_text: "The catalog configuration for the current content could not be found.",
  Validator_CMProductList_InvalidId_text: "Category with code \"{0}\" does not exist in catalog \"{1}\".",
  Validator_CMProductList_InvalidStoreContext_text: "Category could not be loaded. Catalog configuration is not valid.",
  Validator_CMProductList_StoreContextNotFound_text: "The catalog configuration for the current content could not be found.",
  Validator_CMProductList_CatalogNotFoundError_text: "Catalog \"{0}\" could not be found for category with ID \"{1}\".",
  Validator_CMProductList_DocTypeNotSupported_text: "The content type \"Product List\" is not supported in this site and shouldn't be used.",
  Validator_CMProductList_legacy_value_text: "The value \"{0}\" for filter \"{1}\" is not valid anymore. Please select this value again.",
  Validator_CMProductList_invalid_multi_facet_text: "The filter configuration is invalid. To repair, choose  \"Remove all invalid filters\" in the content form.",
  Validator_CMProductList_invalid_multi_facet_query_text: "The value \"{0}\" for filter \"{1}\" is not valid anymore. Please select a new value.",
  Validator_CMChannel_SegmentReservedCharsFound_text: "The segment contains \"{0}\" which is used as internal separator.",
  Validator_CMChannel_SegmentReservedPrefix_text: "The segment is not allowed to start with \"{0}\".",
  Validator_CMChannel_SegmentReservedSuffix_text: "The segment is not allowed to end with \"{0}\".",
  Validator_CMChannel_FallbackSegmentReservedCharsFound_text: "The segment is inherited from the title and will be \"{1}\". But it contains \"{0}\" which is used as internal separator.",
  Validator_CMChannel_FallbackSegmentReservedPrefix_text: "The segment is inherited from the title and will be \"{1}\". But it is not allowed to start with \"{0}\".",
  Validator_CMChannel_FallbackSegmentReservedSuffix_text: "The segment is inherited from the title and will be \"{1}\" But it is not allowed to end with \"{0}\".",
  Validator_possibly_missing_master_reference_from_master_augmentation_text: "An augmenting content in derived site {0} exists, which may miss a link to this content as its master: {1} (ID: {2}).",
  Validator_possibly_missing_master_reference_from_derived_augmentation_text: "An augmenting content in master site {0} exists, which should possibly be linked to as master: {1} (ID: {2}). Consider setting an appropriate master version carefully. Most likely the current master version.",
};

export default LiveContextStudioPluginValidator_properties;
