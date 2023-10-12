import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import LiveContextStudioPluginValidator_properties from "./LiveContextStudioPluginValidator_properties";

/**
 * Overrides of ResourceBundle "LiveContextStudioPluginValidator" for Locale "de".
 * @see LiveContextStudioPluginValidator_properties#INSTANCE
 */
ResourceBundleUtil.override(LiveContextStudioPluginValidator_properties, {
  Validator_catalogError_text: "Katalog konnte nicht geladen werden. Ein unerwarteter Katalogfehler ist aufgetreten.",
  Validator_CMProductTeaser_EmptyExternalId_text: "Kein Produkt verlinkt. Der Teaser sollte auf ein Produkt verweisen.",
  Validator_CMProductTeaser_InvalidId_text: "Produkt mit code \"{0}\" konnte nicht von Katalog \"{1}\" geladen werden.",
  Validator_CMProductTeaser_InvalidStoreContext_text: "Produkt konnte nicht geladen werden. Katalogkonfiguration ist ungültig.",
  Validator_CMProductTeaser_StoreContextNotFound_text: "Der Katalogkontext für den aktuellen Inhalt konnte nicht gefunden werden.",
  Validator_CMProductTeaser_CatalogNotFoundError_text: "Der Katalog \"{0}\" für das Produkt mit der ID \"{1}\" konnte nicht gefunden werden.",
  Validator_CMExternalChannel_EmptyCategory_text: "Dieses Feld darf nicht leer sein. Die Seite muss auf eine Kategorie verweisen.",
  Validator_CMExternalChannel_InvalidId_text: "Die Kategorie mit der ID \"{0}\" existiert nicht in dem Katalog \"{1}\".",
  Validator_CMExternalChannel_InvalidStoreContext_text: "Die Kategorie konnte nicht geladen werden. Die Katalogkonfiguration konnte nicht gefunden werden.",
  Validator_CMExternalChannel_CatalogNotFoundError_text: "Der Katalog \"{0}\" für die Kategorie mit der ID \"{1}\" konnte nicht gefunden werden.",
  Validator_CMExternalProduct_EmptyProduct_text: "Dieses Feld darf nicht leer sein. Die Seite muss auf ein Produkt verweisen.",
  Validator_CMExternalProduct_InvalidId_text: "Das Produkt mit der ID \"{0}\" existiert nicht in dem Katalog \"{1}\".",
  Validator_CMExternalProduct_InvalidStoreContext_text: "Das Produkt konnte nicht geladen werden. Die Katalogkonfiguration konnte nicht gefunden werden.",
  Validator_CMExternalProduct_CatalogNotFoundError_text: "Der Katalog \"{0}\" für das Produkt mit der ID \"{1}\" konnte nicht gefunden werden.",
  Validator_CMExternalPage_EmptyExternalPageId_text: "Die externe ID ist leer. Die Seite muss auf eine externe Seite verweisen.",
  Validator_CMMarketingSpot_EmptyExternalId_text: "Kein e-Marketing Spot verlinkt. Der Teaser sollte auf ein e-Marketing Spot verweisen.",
  Validator_CMMarketingSpot_InvalidId_text: "e-Marketing Spot mit code \"{0}\" existiert nicht in dem Katalog \"{1}\".",
  Validator_CMMarketingSpot_InvalidStoreContext_text: "e-Marketing Spot konnte nicht geladen werden. Katalogkonfiguration ist ungültig.",
  Validator_CMMarketingSpot_StoreContextNotFound_text: "Der Katalogkontext für den aktuellen Inhalt konnte nicht gefunden werden.",
  Validator_CMProductList_InvalidId_text: "Kategorie mit Code \"{0}\" existiert nicht in dem Katalog \"{1}\".",
  Validator_CMProductList_InvalidStoreContext_text: "Kategorie konnte nicht geladen werden. Katalogkonfiguration ist ungültig.",
  Validator_CMProductList_StoreContextNotFound_text: "Der Katalogkontext für den aktuellen Inhalt konnte nicht gefunden werden.",
  Validator_CMProductList_CatalogNotFoundError_text: "Der Katalog \"{0}\" für die Kategorie mit der ID \"{1}\" konnte nicht gefunden werden.",
  Validator_CMProductList_DocTypeNotSupported_text: "Der Dokumenttyp \"Produktliste\" wird in dieser Site nicht unterstützt und sollte nicht benutzt werden.",
  Validator_CMProductList_legacy_value_text: "Der Wert \"{0}\" für den Filter \"{1}\" ist nicht mehr gültig. Bitte wählen Sie den Wert erneut aus.",
  Validator_CMProductList_invalid_multi_facet_text: "Die Suchfilterkonfiguration ist ungültig. Zum Reparieren klicken Sie im Dokumentenformular auf \"Alle ungültige Filterdaten löschen\".",
  Validator_CMProductList_invalid_multi_facet_query_text: "Der Wert \"{0}\" für den Filter \"{1}\" ist nicht mehr gültig. Bitte wählen Sie einen neuen Wert.",
  Validator_CMChannel_SegmentReservedCharsFound_text: "Das Segment darf die Zeichenfolge \"{0}\" nicht enthalten. Sie wird als interner Trenner benutzt.",
  Validator_CMChannel_SegmentReservedPrefix_text: "Das Segment darf nicht mit \"{0}\" anfangen.",
  Validator_CMChannel_SegmentReservedSuffix_text: "Das Segment darf nicht mit \"{0}\" enden.",
  Validator_CMChannel_FallbackSegmentReservedCharsFound_text: "Das Segment erbt vom Titel und wird \"{1}\" sein. Aber es darf die Zeichenfolge \"{0}\" nicht enthalten. Sie wird als interner Trenner benutzt.",
  Validator_CMChannel_FallbackSegmentReservedPrefix_text: "Das Segment erbt vom Titel und wird \"{1}\" sein. Aber es darf nicht mit \"{0}\" anfangen.",
  Validator_CMChannel_FallbackSegmentReservedSuffix_text: "Das Segment erbt vom Titel und wird \"{1}\" sein. Aber es darf nicht mit \"{0}\" enden.",
  Validator_possibly_missing_master_reference_from_master_augmentation_text: "In der abgeleiteten Site {0} existiert ein augmentierender Content, der eventuell auf diesen Content als sein Master verlinken sollte: {1} (ID: {2}).",
  Validator_possibly_missing_master_reference_from_derived_augmentation_text: "In der Master-Site {0} existiert ein augmentierender Content, der wahrscheinlich als Master verlinkt sein sollte: {1} (ID: {2}). Die Master-Version sollte entsprechend gesetzt werden, vermutlich auf die aktuelle Version des Masters.",
});
