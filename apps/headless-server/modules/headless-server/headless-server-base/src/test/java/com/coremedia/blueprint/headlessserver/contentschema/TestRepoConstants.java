package com.coremedia.blueprint.headlessserver.contentschema;

/**
 * This class contains ids and data which is found in the test xml repository at '/test/resources/content/contentrepository.xml'.
 */
public final class TestRepoConstants {
  public static final String SITES_REPO_PATH = "/Sites/TestSite";

  public static final String MASTER_SITE_ID = "the-site-id";
  public static final String MASTER_SITE_NAME = "the-site-name";
  public static final String MASTER_SITE_LOCALE = "en-US";
  public static final String MASTER_SITE_REPO_PATH = SITES_REPO_PATH + "/TestSiteUS";

  public static final String DERIVED_SITE_ID = "the-derived-site-id";
  public static final String DERIVED_SITE_NAME = "the-derived-site-name";
  public static final String DERIVED_SITE_LOCALE = "de-DE";
  public static final String DERIVED_SITE_REPO_PATH = SITES_REPO_PATH + "/TestSiteDE";

  public static final Integer ROOT_CHANNEL_ID = 111112;
  public static final String ROOT_CHANNEL_UUID = "2b44cbcf-4dca-405f-a274-bae6d9eeadfd";
  public static final String ROOT_CHANNEL_SEGMENT = "root-en";
  public static final String ROOT_CHANNEL_REPO_PATH = MASTER_SITE_REPO_PATH + "/Content/Navigation";

  public static final Integer DERIVED_ROOT_CHANNEL_ID = 11144;
  public static final String DERIVED_ROOT_CHANNEL_UUID = "2c0b9846-2d46-47e0-af6a-875e685196e9";
  public static final String DERIVED_ROOT_CHANNEL_SEGMENT = "root-de";
  public static final String DERIVED_ROOT_CHANNEL_REPO_PATH = DERIVED_SITE_REPO_PATH + "/Content/Navigation";

  public static final Integer PICTURE_ID = 111114;
  public static final String PICTURE_UUID = "6a454873-f57e-4ef4-8a60-0e2083e9d6d5";
  public static final Integer VIDEO_ID = 111118;
  public static final String VIDEO_UUID = "47112af0-97b1-49bd-84e7-80701297422e";
  public static final Integer DOWNLOAD_ID = 111120;
  public static final String DOWNLOAD_UUID = "682cffee-6b67-42f4-b608-b56e07dae22b";
  public static final Integer HTML_ID = 111122;
  public static final String HTML_UUID = "0ebe01ee-341c-4667-88a0-67f740c668a0";
  public static final String MEDIA_REPO_PATH = MASTER_SITE_REPO_PATH + "/Content/Pictures";
  public static final String MEDIA_DELIVERY_PATH = "/caas/v1/media/";

  public static final Integer ARTICLE_ID = 111116;
  public static final String ARTICLE_UUID = "a3eef115-05f6-4d62-a784-37218575ff79";
  public static final Integer TIME_TRAVEL_ARTICLE_ID = 111126;
  public static final String ARTICLE_REPO_PATH = MASTER_SITE_REPO_PATH + "/Content/Articles";

  public static final Integer BLOG_TAXONOMY_ID = 130;
  public static final String BLOG_TAXONOMY_UUID = "39395448-62e6-412a-a1cb-ad31bff67ed0";
  public static final Integer HAMBURG_LOCATION_TAXONOMY_ID = 13444;
  public static final String HAMBURG_LOCATION_TAXONOMY_UUID = "3ee8a696-9720-4131-ab87-390c628f4d85";

  public static final String SETTINGS_KEY = "testInt";
  public static final Integer SETTINGS_VALUE = 5;
  public static final String SUB_SETTINGS_KEY = "subStruct.subLevelStringProperty";
  public static final String SUB_SETTINGS_VALUE = "First Sub Level";

  public static final Integer CLANDESTINE_SETTINGS_ID = 111124;
  public static final String SETTINGS_REPO_PATH = MASTER_SITE_REPO_PATH + "/Content/Settings";
  public static final Integer SELECTION_RULES_ID = 111128;
  public static final String SELECTION_RULES_UUID = "257e360c-865c-47cd-a3d1-3279640142f5";
}
