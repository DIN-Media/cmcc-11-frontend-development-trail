import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import FeedbackHubImagga_properties from "./FeedbackHubImagga_properties";

/**
 * Overrides of ResourceBundle "FeedbackHubImagga" for Locale "ja".
 * @see FeedbackHubImagga_properties#INSTANCE
 */
ResourceBundleUtil.override(FeedbackHubImagga_properties, {
  imagga_error_BASIC_AUTH_KEY_NOT_SET: "{0}の設定で有効なbasicAuthKeyを入力してください。",
  imagga_error_UPLOAD_FAILED: "写真のアップロードに失敗しました。Imaggaが問題 \"{1}\" に応答しました",
  imagga_error_LOGIN_ERROR: "Imaggaでのログインに失敗しました。設定で有効な認証情報キーを入力してください。",
  imagga_error_GET_TAGS_FROM_UPLOAD_FAILED: "Imaggaへのキーワードのリクエストに失敗しました。Imaggaが問題 \"{1}\" に応答しました",
  imagga_error_ERROR_PROCESSING_JSON: "Imaggaは現在使用できません。後でもう一度お試しください。",
  imagga_error_NOT_SUPPORTED_FILE_TYPE: "有効なファイルタイプをアップロードしてください。Imaggaはファイルタイプ「JPG」または「PNG」をサポートしています。",
});
