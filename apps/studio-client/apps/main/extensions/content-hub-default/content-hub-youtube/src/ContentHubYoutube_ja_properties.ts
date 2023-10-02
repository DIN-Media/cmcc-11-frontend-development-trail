import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import ContentHubYoutube_properties from "./ContentHubYoutube_properties";

/**
 * Overrides of ResourceBundle "ContentHubYoutube" for Locale "ja".
 * @see ContentHubYoutube_properties#INSTANCE
 */
ResourceBundleUtil.override(ContentHubYoutube_properties, {
  text_sectionItemKey: "説明",
  lastModified_sectionItemKey: "最終更新日時",
  videoId_sectionItemKey: "ID",
  link_sectionItemKey: "リンク",
  YouTubeErrorCode_USAGE_LIMIT_EXCEEDED_title: "1日の制限を超えました。",
  YouTubeErrorCode_USAGE_LIMIT_EXCEEDED: "Youtube: '{1}' からの回答",
  YouTubeErrorCode_QUOTA_POINTS_EXCEEDED_title: "割当てられた上限を超えました",
  YouTubeErrorCode_QUOTA_POINTS_EXCEEDED: "Googleアカウント内の上限を超えたため、リクエストを完了できません。",
});
