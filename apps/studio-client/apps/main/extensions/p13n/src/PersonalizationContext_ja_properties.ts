import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import PersonalizationContext_properties from "./PersonalizationContext_properties";

/**
 * Contains localization properties used for user-context data. Context names, their properties, and the respective
 * values can be localized by using the patterns
 *
 *   <actualContextName>=<localizedContextName> for context names
 *   <actualContextName>_<actualPropertyName>=<localizedPropertyName> for property keys
 *   <actualContextName>_<actualPropertyName>_<actualPropertyValue>=<localizedPropertyValue> for property values
 *
 * All non-word characters, that is everything but (A-Z, a-z, 0-9, or _), are removed from the actual names before
 * a property look-up is performed.
 *
 * If a localization of a context, property, or value is missing, the actual value is used.
 *
 * @see PersonalizationContext_properties#INSTANCE
 */
ResourceBundleUtil.override(PersonalizationContext_properties, {
  p13n_context_system: "システム情報",
  p13n_context_system_timeOfDay: "時刻",
  p13n_context_system_date: "日付",
  p13n_context_system_dayOfWeek: "曜日",
  p13n_context_system_dayOfWeek_1: "日曜日",
  p13n_context_system_dayOfWeek_2: "月曜日",
  p13n_context_system_dayOfWeek_3: "火曜日",
  p13n_context_system_dayOfWeek_4: "水曜日",
  p13n_context_system_dayOfWeek_5: "木曜日",
  p13n_context_system_dayOfWeek_6: "金曜日",
  p13n_context_system_dayOfWeek_7: "土曜日",
  p13n_context_system_dateTime: "日付と時間",
  p13n_context_keyword: "キーワード",
  p13n_context_personal: "個人情報",
  p13n_context_personal_dateofbirth: "誕生日",
  p13n_context_personal_street: "アドレス",
  p13n_context_personal_additionaladdressinformation: "追加の住所",
  p13n_context_personal_emailaddress: "Eメール",
  p13n_context_personal_role: "役割",
  p13n_context_personal_website: "ウェブサイト",
  p13n_context_personal_phonenumber: "電話",
  p13n_context_personal_postalcode: "郵便番号",
  p13n_context_personal_timezone: "タイムゾーン",
  p13n_context_personal_title: "タイトル",
  p13n_context_personal_givenname: "名前",
  p13n_context_personal_nickname: "ニックネーム",
  p13n_context_personal_familyname: "名字",
  p13n_context_personal_fullname: "フルネーム",
  p13n_context_personal_skypename: "Skype",
  p13n_context_personal_cellphonenumber: "モバイル",
  p13n_context_personal_locality: "地域",
  p13n_context_personal_organization: "組織",
  p13n_context_personal_country: "国",
  p13n_context_explicit_social: "Elastic Social",
  p13n_context_explicit_adaptive: "適応できるウェブサイト",
  p13n_context_explicit_mobile: "モバイル配送",
  p13n_context_explicit_multitouch: "マルチタッチポイント",
  p13n_context_referrer: "リファラー情報",
  p13n_context_referrer_emptyText: "セッションを最初にリクエストしたリファラーURL。",
  p13n_context_referrer_query: "検索クエリ",
  p13n_context_referrer_searchengine: "検索エンジン",
  p13n_context_segment: "セグメント",
  p13n_context_example: "コンテキストの例",
  p13n_context_example_boolean: "ブール値プロパティ",
  p13n_context_testuser_profile_socialsoftware: "Elastic Social",
  p13n_context_testuser_profile_givenname: "名前",
  p13n_context_testuser_profile_givenname_emptyText: "ペルソナの名前を入力。",
  p13n_context_testuser_profile_name: "名前",
  p13n_context_testuser_profile_name_emptyText: "ペルソナの名字を入力。",
  p13n_context_testuser_profile_birthday: "誕生日",
  p13n_context_testuser_profile_email: "Eメール",
  p13n_context_testuser_profile_systemproperties: "システムプロパティ",
  p13n_context_testuser_profile_date_and_time: "日付と時間",
  p13n_context_testuser_profile_interests: "興味 (キーワード)",
  p13n_context_testuser_profile_addinterests: "+ 興味プロパティを追加",
  p13n_context_testuser_profile_withvalue: "値で",
  p13n_context_testuser_profile_image: "画像",
  p13n_context_personal_dateandtime: "日付と時間",
  p13n_context_location_city: "市",
  p13n_context_gender: "性別",
});
