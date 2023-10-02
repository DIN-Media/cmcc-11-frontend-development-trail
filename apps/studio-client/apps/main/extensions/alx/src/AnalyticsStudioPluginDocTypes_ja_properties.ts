import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import AnalyticsStudioPluginDocTypes_properties from "./AnalyticsStudioPluginDocTypes_properties";

/**
 * Overrides of ResourceBundle "AnalyticsStudioPluginDocTypes" for Locale "ja".
 * @see AnalyticsStudioPluginDocTypes_properties#INSTANCE
 */
ResourceBundleUtil.override(AnalyticsStudioPluginDocTypes_properties, {
  CMALXBaseList_text: "アナリティクス基礎リスト",
  CMALXPageList_text: "アナリティクスページリスト",
  CMALXPageList_documentType_text: "コンテンツタイプ",
  CMALXPageList_documentType_emptyText: "必要なコンテンツタイプ。",
  CMALXPageList_baseChannel_text: "ベースチャンネル",
  CMALXPageList_baseChannel_emptyText: "求めるコンテンツアイテムが属するベースチャンネルを入力します。",
  CMALXPageList_defaultContent_text: "デフォルトコンテンツ",
  CMALXPageList_defaultContent_emptyText: "こちらのライブラリからドラッグして、デフォルトコンテンツを追加します。",
  CMALXBaseList_maxLength_text: "最長",
  CMALXBaseList_maxLength_emptyText: "アナリティクスページリストの最長の長さを入力してください。",
  CMALXBaseList_timeRange_text: "時間の範囲",
  CMALXBaseList_timeRange_emptyText: "時間の範囲を入力して(今日より以前の日)を含めます。",
  CMALXBaseList_analyticsProvider_text: "アナリティクス提供者のID",
  CMALXBaseList_analyticsProvider_emptyText: "アナリティクス提供者のIDを入力します。",
  CMALXEventList_text: "アナリティクスイベントリスト",
  CMALXEventList_category_text: "イベントのカテゴリー",
  CMALXEventList_category_emptyText: "追跡目標のグループのために提供された名前を入力します。(例、「動画」)",
  CMALXEventList_action_text: "イベントのアクション",
  CMALXEventList_action_emptyText: "追跡すべきイベントまたはインタラクションのタイプを入力 (例、「プレイが押された」)",
  CMALXEventList_defaultContent_text: "デフォルトコンテンツ",
  "CMChannel_localSettings.analyticsProvider_text": "デフォルトのアナリティクスプロバイダの名前",
  "CMChannel_localSettings.analyticsProvider_emptyText": "デフォルトのアナリティクスプロバイダのIDを入力。",
});
