import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import BlueprintDocumentTypes_properties from "./BlueprintDocumentTypes_properties";

/**
 *
 * Display labels and tool tips for Content Types
 *
 * @see BlueprintDocumentTypes_properties#INSTANCE
 */
ResourceBundleUtil.override(BlueprintDocumentTypes_properties, {
  CMAbstractCode_text: "コード",
  CMAbstractCode_toolTip: "コードオブジェクトのベースタイプ",
  CMAbstractCode_code_text: "コード",
  CMAbstractCode_code_toolTip: "コードデータ",
  CMAbstractCode_code_emptyText: "ここにコードデータを入力します。",
  CMAbstractCode_dataUrl_text: "データURL",
  CMAbstractCode_dataUrl_toolTip: "データの読み込み先URL",
  CMAbstractCode_dataUrl_emptyText: "ここにデータの読み込み先URLを入力します。",
  CMAbstractCode_description_text: "説明",
  CMAbstractCode_description_toolTip: "テキスト説明",
  CMAbstractCode_description_emptyText: "ここにテキスト説明を入力します。",
  CMAbstractCode_ieExpression_text: "IEインクルード式",
  CMAbstractCode_ieExpression_toolTip: "Internet Explorer専用のインクルード式（「if lte IE 7」など）",
  CMAbstractCode_ieExpression_emptyText: "IEインクルード式",
  CMAbstractCode_ieRevealed_text: "IE表示",
  CMAbstractCode_ieRevealed_toolTip: "このプロパティを設定すると、Internet Explorer用のコードが表示されます",
  CMAbstractCode_ieRevealed_true_text: "Internet Explorer用コードの表示",
  CMAbstractCode_include_text: "インクルードするコード",
  CMAbstractCode_include_toolTip: "追加で読み込まれる必須コード",
  CMJavaScript_inHead_text: "Include the script in the HTML head",
  CMJavaScript_inHead_toolTip: "Setting this property includes the script in the HTML head instead of the body.",
  CMJavaScript_inHead_true_text: "Include the script in the HTML head",
  CMAction_text: "アクション",
  CMAction_toolTip: "アクションを表すドキュメント",
  CMAction_id_text: "ID",
  CMAction_id_emptyText: "ここにIDを入力します",
  CMAction_type_text: "種類",
  CMAction_type_emptyText: "ここにアクションの種類を入力します",
  CMArticle_text: "記事",
  CMArticle_toolTip: "記事",
  CMArticle_title_text: "記事タイトル",
  CMArticle_title_toolTip: "記事のタイトル",
  CMArticle_title_emptyText: "ここに記事のタイトルを入力します。",
  CMArticle_detailText_text: "記事本文",
  CMArticle_detailText_toolTip: "記事本文のテキスト",
  CMArticle_detailText_emptyText: "ここに記事の本文を入力します。",
  CMAudio_text: "オーディオ",
  CMAudio_toolTip: "オーディオメディアオブジェクト",
  CMAudio_dataUrl_text: "データURL",
  CMAudio_dataUrl_toolTip: "オーディオオブジェクトのURL",
  CMAudio_dataUrl_emptyText: "ここにオーディオオブジェクトのURLを入力します。",
  CMChannel_text: "ページ",
  CMChannel_toolTip: "ページ",
  CMChannel_footer_text: "フッターオブジェクト",
  CMChannel_footer_toolTip: "フッターオブジェクト",
  CMChannel_footer_emptyText: "ライブラリからオブジェクトをドラッグして追加します。",
  CMChannel_header_text: "ヘッダーオブジェクト",
  CMChannel_header_toolTip: "ヘッダーオブジェクト",
  CMChannel_header_emptyText: "ライブラリからオブジェクトをドラッグして追加します。",
  CMChannel_picture_text: "ページ画像",
  CMChannel_picture_toolTip: "ページの画像",
  CMChannel_sidebarList_text: "サイドバーリスト",
  CMChannel_sidebarList_emptyText: "ライブラリから要素をドラッグして追加します。",
  CMChannel_primarynav_text: "プライマリナビゲーション",
  CMChannel_primarynav_emptyText: "ライブラリから要素をドラッグして追加します。",
  CMChannel_subnavList_text: "サブナビゲーションリスト",
  CMChannel_subnavList_emptyText: "ライブラリから要素をドラッグして追加します。",
  CMChannel_title_text: "ページタイトル",
  CMChannel_title_toolTip: "ページ詳細タイトル",
  CMChannel_title_emptyText: "ここにページのタイトルを入力します。",
  "CMChannel_contextSettings.userFeedback.enabled_text": "ユーザーフィードバック",
  "CMChannel_contextSettings.userFeedback.enabled_true_text": "有効",
  "CMChannel_contextSettings.exampleStringProperty_text": "テキスト構造体の例",
  "CMChannel_contextSettings.exampleStringProperty_emptyText": "テキストを入力してください。",
  CMChannel_placement_text: "配置",
  "CMChannel_placement.placements_2.layout_text": "レイアウト",
  CMCollection_text: "コレクション",
  CMCollection_toolTip: "ティーザーとして使用可能なコンテンツのコレクション",
  CMCollection_items_text: "アイテム",
  CMCollection_items_toolTip: "このコレクションのアイテム",
  CMCollection_title_text: "コレクションタイトル",
  CMCollection_title_toolTip: "コレクションの詳細タイトル",
  CMCollection_title_emptyText: "ここにコレクションのタイトルを入力します。",
  CMContext_text: "ナビゲーションコンテキスト",
  CMContext_toolTip: "ナビゲーションコンテキスト",
  CMCSS_text: "CSS",
  CMCSS_toolTip: "CSS（カスケードスタイルシート）",
  CMCSS_code_text: "CSSデータ",
  CMCSS_code_toolTip: "CSSデータ",
  CMCSS_ieRevealed_text: "IE表示",
  CMCSS_ieRevealed_toolTip: "このプロパティを設定すると、Internet Explorer用のCSSが表示されます",
  CMCSS_ieRevealed_true_text: "Internet Explorer用CSSの表示",
  CMCSS_media_text: "CSS Media属性",
  CMCSS_media_toolTip: "オプションのCSS Media属性（screenなど）",
  CMCSS_include_text: "インクルードする/必須のCSS",
  CMCSS_include_toolTip: "インクルードする、または必須のCSS（必須のフレームワークなど）",
  Dictionary_text: "辞書",
  Dictionary_toolTip: "辞書",
  CMDownload_text: "ダウンロード",
  CMDownload_toolTip: "一般ダウンロード",
  CMDownload_data_text: "バイナリデータ",
  CMDownload_data_toolTip: "ダウンロード可能なオブジェクトのバイナリデータ",
  CMDownload_title_text: "ダウンロードタイトル",
  CMDownload_title_toolTip: "詳細タイトル",
  CMDownload_title_emptyText: "ここにダウンロードのタイトルを入力します。",
  CMDynamicList_text: "動的リスト",
  CMDynamicList_toolTip: "動的に投入されるリスト",
  CMDynamicList_maxLength_text: "最大長",
  CMDynamicList_maxLength_toolTip: "リストアイテムの最大数",
  CMDynamicList_maxLength_emptyText: "ここにリストアイテムの最大数を入力します。",
  CMExternalLink_text: "外部リンク",
  CMExternalLink_toolTip: "外部リンク",
  CMExternalLink_url_text: "URL",
  CMExternalLink_url_toolTip: "ウェブリソースを指すURL",
  CMExternalLink_url_emptyText: "ここにウェブリソースのURLを入力します。",
  CMExternalChannel_legacy_children_text: "ナビゲーションの子階層 (legacy, please remove)",
  CMFolderProperties_text: "フォルダプロパティ",
  CMFolderProperties_toolTip: "フォルダプロパティ",
  CMFolderProperties_contexts_text: "コンテキスト",
  CMFolderProperties_contexts_toolTip: "コンテキスト",
  CMFolderProperties_contexts_emptyText: "ライブラリからコンテキストをドラッグして追加します。",
  CMGallery_text: "ギャラリー",
  CMGallery_toolTip: "メディア資産のコレクション",
  CMGallery_items_text: "ギャラリー写真",
  CMGallery_items_toolTip: "このコレクションのアイテム",
  CMGallery_items_emptyText: "ライブラリから写真をドラッグして追加します。",
  CMGallery_title_text: "ギャラリータイトル",
  CMGallery_title_toolTip: "ギャラリーの詳細タイトル",
  CMGallery_title_emptyText: "ここにギャラリーのタイトルを入力します。",
  CMGallery_detailText_text: "ギャラリーテキスト",
  CMHasContexts_text: "ナビゲーションコンテキスト付きのドキュメント",
  CMHasContexts_toolTip: "1つ以上のナビゲーションコンテキストがあるドキュメント",
  CMHasContexts_contexts_text: "ナビゲーションコンテキスト",
  CMHasContexts_contexts_toolTip: "このオブジェクトを使用できるナビゲーションコンテキスト",
  CMHasContexts_contexts_emptyText: "ライブラリからドキュメントをドラッグして追加します。",
  CMHTML_text: "HTMLフラグメント",
  CMHTML_toolTip: "任意のHTMLデータを含める静的なHTMLフラグメント",
  CMHTML_data_text: "HTMLコード",
  CMHTML_data_toolTip: "HTMLコード",
  CMImage_text: "テクニカルイメージ",
  CMImage_toolTip: "編集上のテキストなしのテクニカルイメージ（CSS背景画像用など）",
  CMImage_data_text: "データ",
  CMImage_data_toolTip: "画像のバイナリデータ",
  CMImage_description_text: "画像の説明",
  CMImage_description_toolTip: "テキスト説明",
  CMImage_description_emptyText: "ここに説明を入力します。",
  CMImageMap_text: "画像マップ",
  CMImageMap_icon: "content-type-CMImageMap-icon",
  CMImageMap_toolTip: "論評画像マップ",
  CMImageMap_title: "ホットゾーン",
  CMImageMap_imageMapAreas_text: "ホットゾーン",
  CMImageMap_teaser_title: "画像マップティーザー",
  CMImageMap_pictures_text: "写真",
  "CMImageMap_localSettings.image-map_text": "ホットゾーン",
  "CMImageMap_localSettings.overlay_text": "オーバーレイコンフィギュレーション",
  "CMImageMap_localSettings.overlay.displayTitle_true_text": "画面タイトル/名前",
  "CMImageMap_localSettings.overlay.displayTitle_text": "画面タイトル/名前",
  "CMImageMap_localSettings.overlay.displayShortText_true_text": "画面ショートテキスト",
  "CMImageMap_localSettings.overlay.displayShortText_text": "画面ショートテキスト",
  "CMImageMap_localSettings.overlay.displayPicture_true_text": "画面写真",
  "CMImageMap_localSettings.overlay.displayPicture_text": "画面写真",
  CMImageMap_overlayConfiguration_title: "オーバーレイコンフィギュレーション",
  CMInteractive_text: "インタラクティブ",
  CMInteractive_toolTip: "インタラクティブメディアオブジェクト",
  CMInteractive_dataUrl_text: "データURL",
  CMInteractive_dataUrl_toolTip: "インタラクティブオブジェクトのURL",
  CMInteractive_dataUrl_emptyText: "ここにインタラクティブオブジェクトのURLを入力します。",
  CMJavaScript_text: "JavaScript",
  CMJavaScript_toolTip: "JavaScript",
  CMJavaScript_code_text: "JavaScriptコード",
  CMJavaScript_code_toolTip: "JavaScriptコード",
  CMJavaScript_include_text: "インクルードする/必須のJavaScript",
  CMJavaScript_include_toolTip: "インクルードする/必須のJavaScript",
  CMLinkable_text: "リンク可能なオブジェクト",
  CMLinkable_toolTip: "セグメントベースのURIを持つリンク可能なオブジェクト",
  CMLinkable_keywords_text: "HTMLキーワード",
  CMLinkable_keywords_toolTip: "HTMLに翻訳されるフリータグ、例、検索エンジン",
  CMLinkable_keywords_emptyText: "タグをこちらに入力。",
  CMLinkable_linkedSettings_text: "リンクされた設定",
  CMLinkable_linkedSettings_toolTip: "リンクされた設定",
  CMLinkable_linkedSettings_emptyText: "こちらのライブラリからドラッグしてコンテンツを追加。",
  CMLinkable_localSettings_text: "ローカルの設定",
  CMLinkable_localSettings_toolTip: "ローカルの設定",
  CMLinkable_localSettings_emptyText: "構造体XMLをこちらに貼り付け。",
  CMLinkable_locationTaxonomy_text: "場所タグ",
  CMLinkable_locationTaxonomy_toolTip: "場所タグ",
  CMLinkable_locationTaxonomy_emptyText: "こちらのライブラリからドラッグしてコンテンツを追加。",
  CMLinkable_segment_text: "URLセグメント",
  CMLinkable_segment_toolTip: "ドキュメントへのリンクにレンダリングされるURLセグメント。指定しない場合、ドキュメント名が使用されます。",
  CMLinkable_segment_emptyText: "ここにURLセグメントを入力します。",
  CMLinkable_subjectTaxonomy_text: "主題分類",
  CMLinkable_subjectTaxonomy_toolTip: "主題分類",
  CMLinkable_subjectTaxonomy_emptyText: "ライブラリから要素をドラッグして追加します。",
  CMLinkable_title_text: "敬称",
  CMLinkable_title_toolTip: "詳細タイトル",
  CMLinkable_title_emptyText: "ここにタイトルを入力します。",
  CMLinkable_validFrom_text: "有効期間開始日",
  CMLinkable_validFrom_toolTip: "有効期間開始日",
  CMLinkable_validFrom_emptyText: "有効期間開始日",
  CMLinkable_validTo_text: "有効期間終了日",
  CMLinkable_validTo_toolTip: "有効期間終了日",
  CMLinkable_validTo_emptyText: "有効期間終了日",
  CMLinkable_viewtype_text: "レイアウトバリエーション",
  CMLinkable_viewtype_toolTip: "レイアウトバリエーションとは、標準と異なるレイアウトのバリアントのことを指します。",
  CMLinkable_viewtype_emptyText: "デフォルトバリエーション",
  CMLinkable_validity_text: "有効期限",
  CMLinkable_pagegridLayout_text: "ページグリッドのレイアウト",
  CMLocalized_text: "ローカライズされたCoreMedia Blueprintオブジェクト",
  CMLocalized_toolTip: "ローカライズされたCoreMedia Blueprintオブジェクト",
  CMLocalized_editorialState_text: "編集状況",
  CMLocalized_editorialState_toolTip: "編集状況",
  CMLocalized_editorialState_emptyText: "セレクターを使用して状況を設定します",
  CMLocalized_locale_text: "言語",
  CMLocalized_locale_toolTip: "ドキュメントの言語（国/バリアント）",
  CMLocalized_locale_emptyText: "ここに言語を入力します。",
  CMLocalized_master_text: "マスター",
  CMLocalized_master_toolTip: "ドキュメントの派生元であるマスタードキュメント",
  CMLocalized_masterVersion_text: "マスターバージョン",
  CMLocalized_masterVersion_toolTip: "ドキュメントの派生元であるマスタードキュメントのバージョン",
  CMLocalized_masterVersion_emptyText: "ここにマスターバージョンを入力します。",
  CMLocalized_resourceBundles2_text: "リソースバンドル",
  CMLocalized_resourceBundles2_toolTip: "リソースバンドル",
  CMLocalized_resourceBundles2_emptyText: "ここライブラリからドラッグして、コンテンツを追加します。",
  "CMLocalized_com.coremedia.cms.editor.sdk.config.derivedContentsList_text": "派生したコンテンツ",
  CMLocTaxonomy_text: "場所分類",
  CMLocTaxonomy_toolTip: "場所分類",
  CMLocTaxonomy_latitudeLongitude_text: "緯度・経度",
  CMLocTaxonomy_postcode_text: "郵便番号",
  CMLocTaxonomy_postcode_toolTip: "郵便番号",
  CMLocTaxonomy_postcode_emptyText: "ここに郵便番号を入力します。",
  CMMail_text: "メール",
  CMMail_toolTip: "メール",
  CMMedia_text: "メディアオブジェクト",
  CMMedia_toolTip: "マルチメディアオブジェクト",
  CMMedia_alt_text: "代替テキスト",
  CMMedia_alt_toolTip: "レンダリング失敗時に表示する代替テキスト",
  CMMedia_alt_emptyText: "ここに代替テキストを入力します。",
  CMMedia_caption_text: "キャプション",
  CMMedia_caption_toolTip: "メディアオブジェクトのキャプション",
  CMMedia_copyright_text: "著作権",
  CMMedia_copyright_toolTip: "著作権",
  CMMedia_copyright_emptyText: "ここに著作権情報を入力します。",
  CMMedia_data_text: "データ",
  CMMedia_data_toolTip: "メディアオブジェクトのデータ",
  CMMedia_description_text: "説明",
  CMMedia_description_toolTip: "内部用の説明",
  CMMedia_description_emptyText: "ここに説明を入力します。",
  CMNavigation_text: "ナビゲーションアイテム",
  CMNavigation_toolTip: "ナビゲーションアイテム",
  CMNavigation_children_text: "ナビゲーションの子階層",
  CMNavigation_children_toolTip: "ナビゲーションの子階層",
  CMNavigation_css_text: "関連付けするCSS",
  CMNavigation_css_toolTip: "HTMLページにレンダリングされるCSS",
  CMNavigation_css_emptyText: "ライブラリからCSSファイルをドラッグして追加します。",
  //true text and text are kept intentionally same
  CMNavigation_hidden_text: "ナビゲーションとサイトマップで非表示にする",
  CMNavigation_hidden_true_text: "ナビゲーションとサイトマップで非表示にする",
  CMNavigation_hidden_toolTip: "このプロパティを設定すると、ナビゲーションとサイトマップでアイテムが非表示になります。",
  //true text and text are kept intentionally same
  CMNavigation_hiddenInSitemap_text: "サイトマップで非表示にする",
  CMNavigation_hiddenInSitemap_true_text: "サイトマップで非表示にする",
  CMNavigation_hiddenInSitemap_toolTip: "このプロパティを設定すると、サイトマップでアイテムが非表示になります。",
  CMNavigation_isRoot_text: "サイトとして使用する",
  CMNavigation_isRoot_toolTip: "ナビゲーションを最上位階層として使用する",
  CMNavigation_javaScript_text: "関連付けするJavaScript",
  CMNavigation_javaScript_toolTip: "HTMLページにレンダリングされるJavaScript",
  CMNavigation_javaScript_emptyText: "ライブラリからJavaScriptファイルをドラッグして追加します。",
  CMNavigation_pageGrid_text: "ページグリッド",
  CMNavigation_pageGrid_toolTip: "ページグリッド",
  CMObject_text: "CoreMedia Blueprintオブジェクト",
  CMObject_toolTip: "CoreMedia Blueprintベースオブジェクト",
  "CMObject_localSettings.fq.subjecttaxonomy_text": "ドキュメントに次のいずれかのキーワードが含まれます。",
  "CMObject_localSettings.fq.locationtaxonomy_text": "ドキュメントが次のいずれかの場所でタグ付けされています。",
  CMPicture_text: "画像",
  CMPicture_toolTip: "編集上の画像",
  "CMPicture_localSettings.disableCropping_text": "元の画像サイズを使用する",
  "CMPicture_localSettings.disableCropping_true_text": "元の画像サイズを使用する",
  CMPicture_data_text: "画像",
  CMPicture_data_toolTip: "バイナリ画像データ",
  CMPicture_title_text: "画像タイトル",
  CMPicture_title_toolTip: "この画像のタイトル",
  CMPicture_title_emptyText: "ここに画像のタイトルを入力します。",
  CMPicture_detailText_text: "キャプション",
  CMPicture_detailText_toolTip: "写真のキャプション",
  CMPicture_detailText_emptyText: "キャプションをこちらに入力。",
  CMPlaceholder_text: "プレースホルダー",
  CMPlaceholder_toolTip: "プレースホルダーとなるドキュメント",
  CMPlaceholder_icon: "content-type-CMAction-icon",
  CMPlaceholder_id_text: "ID",
  CMPlaceholder_id_emptyText: "プレースホルダ―のIDをこちらに入力。",
  Query_text: "クエリ",
  Query_toolTip: "クエリドキュメント",
  CMQueryList_text: "クエリリスト",
  CMQueryList_toolTip: "クエリリスト",
  "CMQueryList_localSettings.limit_emptyText": "選択する",
  CMSettings_text: "設定",
  CMSettings_toolTip: "設定ドキュメント",
  CMSettings_settings_text: "設定",
  CMSettings_settings_toolTip: "構造体の設定",
  CMSettings_settings_emptyText: "構造体の設定をこちらに入力。",
  Preferences_text: "設定",
  EditorPreferences_text: "エディタープリファレンス",
  EditorPreferences_data_toolTip: "エディタプリファレンス構造体",
  EditorPreferences_data_emptyText: "エディタプリファレンスをこちらで編集。",
  CMSite_text: "サイトインジケータ",
  CMSite_toolTip: "サイトインジケータはウェブサイトのルートページとなるようにページをマークします",
  CMSite_root_text: "ルートページ",
  CMSite_root_toolTip: "ウェブサイトのルートページ",
  CMSite_id_text: "ID",
  CMSite_id_toolTip: "このサイトの安定したID",
  CMSite_locale_emptyText: "ロケールをこちらに入力。",
  CMSite_locale_text: "ロケール",
  CMSite_locale_toolTip: "任意の国／バリエーションを伴うこのサイトの言語",
  CMSite_master_text: "Master",
  CMSite_master_toolTip: "このサイトを派生したMasterサイト",
  CMSite_name_text: "名前",
  CMSite_name_toolTip: "このサイトの名前",
  CMSite_siteManagerGroup_emptyText: "site managerグループ名をこちらに入力。",
  CMSite_siteManagerGroup_text: "Site Managerグループ",
  CMSite_siteManagerGroup_toolTip: "このサイトを管理することがあるユーザーのグループ",
  CMSitemap_text: "サイトマップ",
  CMSitemap_toolTip: "サイトマップを表すドキュメント",
  CMSitemap_root_text: "ルートページ",
  CMSitemap_root_toolTip: "生成するサイトマップのルートページ",
  CMSitemap_title_text: "サイトマップのタイトル",
  CMSitemap_title_toolTip: "サイトマップのタイトル",
  CMSitemap_title_emptyText: "ここにサイトマップのタイトルを入力します。",
  "CMSitemap_localSettings.sitemap_depth_emptyText": "サイトマップの深さをこちらに入力。",
  "CMSitemap_localSettings.sitemap_depth_text": "サイトマップの深さ",
  CMSitemap_localSettings_sitemap_depth_text: "サイトマップの深さ",
  CMSymbol_text: "記号",
  CMSymbol_toolTip: "既知の値を表す記号",
  CMSymbol_description_text: "説明",
  CMSymbol_description_toolTip: "内部用のテキスト説明",
  CMSymbol_description_emptyText: "ここに説明を入力します。",
  CMSymbol_icon_text: "画像",
  CMSymbol_icon_toolTip: "この記号の画像",
  CMTaxonomy_text: "分類",
  CMTaxonomy_toolTip: "分類",
  CMTaxonomy_children_text: "子",
  CMTaxonomy_children_toolTip: "子分類",
  CMTaxonomy_externalReference_text: "外部参照",
  CMTaxonomy_externalReference_toolTip: "外部参照",
  CMTaxonomy_externalReference_emptyText: "ここに外部キーワード分類への参照を入力します。",
  CMTaxonomy_value_text: "名前",
  CMTaxonomy_value_toolTip: "キーワード名",
  CMTaxonomy_value_emptyText: "ここにキーワード名を入力します。",
  CMTaxonomy_parent_text: "親",
  CMTaxonomy_parent_toolTip: "親分類",
  CMTeasable_text: "ティーザーとして使用可能なドキュメント",
  CMTeasable_toolTip: "埋め込みティーザー付きのティーザーとして使用可能なドキュメント",
  CMTeasable_detailText_text: "詳細テキスト",
  CMTeasable_detailText_toolTip: "ティーザーの詳細テキスト",
  CMTeasable_detailText_emptyText: "ここにティーザーのテキストを入力します。",
  //text and true text are kept same deliberately
  CMTeasable_notSearchable_text: "検索不可",
  CMTeasable_notSearchable_true_text: "検索不可",
  CMTeasable_pictures_text: "画像",
  CMTeasable_pictures_emptyText: "ライブラリから要素をドラッグして追加します。",
  CMTeasable_related_text: "関連コンテンツ",
  CMTeasable_related_toolTip: "関連コンテンツ",
  CMTeasable_related_emptyText: "ライブラリから関連コンテンツをドラッグして追加します",
  CMTeasable_teaserText_text: "ティーザーテキスト",
  CMTeasable_teaserText_toolTip: "ティーザーのテキスト",
  CMTeasable_teaserText_emptyText: "ここにティーザーのテキストを入力します。",
  CMTeasable_teaserTitle_text: "ティーザータイトル",
  CMTeasable_teaserTitle_toolTip: "ティーザーのタイトル",
  CMTeasable_teaserTitle_emptyText: "ここにティーザーのタイトルを入力します。",
  CMTeaser_text: "ティーザー",
  CMTeaser_toolTip: "ティーザー",
  CMTeaser_target_text: "ティーザーターゲット",
  CMTeaser_target_toolTip: "ティーザーの誘導先となるターゲットコンテンツ",
  CMVideo_text: "ビデオ",
  CMVideo_toolTip: "ビデオ",
  CMVideo_dataUrl_text: "データURL",
  CMVideo_dataUrl_toolTip: "ビデオオブジェクトのURL",
  CMVideo_dataUrl_emptyText: "ここにビデオオブジェクトのURLを入力します。",
  CMVideo_title_text: "ビデオタイトル",
  CMVideo_title_toolTip: "ビデオ詳細タイトル",
  CMVideo_title_emptyText: "ここにビデオのタイトルを入力します。",
  CMVideo_data_helpText: "ビデオをアップロードできます。",
  CMVideo_detailText_text: "ビデオテキスト",
  CMViewtype_text: "レイアウトバリエーション",
  CMViewtype_toolTip: "レイアウトバリエーションとは、標準と異なるレイアウトのバリアントのことを指します。",
  CMViewtype_layout_text: "Layout",
  CMViewtype_layout_toolTip: "Layout",
  CMViewtype_layout_emptyText: "Layout",
  CMVisual_text: "ビジュアル",
  CMVisual_toolTip: "ビジュアルメディアオブジェクト",
  CMVisual_data_text: "データ",
  CMVisual_data_toolTip: "データ",
  CMVisual_dataUrl_text: "データURL",
  CMVisual_dataUrl_toolTip: "このビジュアルメディアオブジェクトのURL",
  CMVisual_dataUrl_emptyText: "ここにビジュアルオブジェクトのURLを入力します。",
  CMVisual_height_text: "高さ",
  CMVisual_height_toolTip: "レンダリングするピクセル単位の高さ",
  CMVisual_height_emptyText: "ここにレンダリングするピクセル単位の高さを入力します。",
  CMVisual_width_text: "幅",
  CMVisual_width_toolTip: "レンダリングするピクセル単位の幅",
  CMVisual_width_emptyText: "ここにレンダリングするピクセル単位の幅を入力します。",
  CMTemplateSet_text: "テンプレートセット",
  CMTemplateSet_toolTip: "テンプレートセット",
  CMTemplateSet_description_text: "説明",
  CMTemplateSet_description_toolTip: "説明",
  CMTemplateSet_description_emptyText: "ここに説明を入力します。",
  CMTemplateSet_archive_text: "テンプレートアーカイブ",
  CMTemplateSet_archive_toolTip: "テンプレートアーカイブ（jarまたはzip）",
  CMTemplateSet_archive_helpText: "zipまたはjarアーカイブをアップロードできます",
  CMTemplateSet_metadata_archiveLabel_text: "ラベル",
  CMTemplateSet_metadata_files_text: "ファイル",
  CMTemplateSet_metadata_files_nameHeader_text: "名前",
  CMTemplateSet_metadata_files_sizeHeader_text: "サイズ",
  CMTemplateSet_metadata_files_timeHeader_text: "時刻",
  Meta_data_exif: "画像タグ",
  Meta_data_id3: "オーディオデータ",
});
