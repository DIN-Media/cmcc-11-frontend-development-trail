import contentTypeLocalizationRegistry
  from "@coremedia/studio-client.cap-base-models/content/contentTypeLocalizationRegistry";
import TrainingContentTypes_properties from "./TrainingContentTypes_properties";
import typeVideoTutorialIcon from "./icons/type-video-tutorial.svg";

// Add localization for the new content type "CMVideoTutorial"
contentTypeLocalizationRegistry.addLocalization("CMVideoTutorial", {
  displayName: TrainingContentTypes_properties.CMVideoTutorial_text,
  description: TrainingContentTypes_properties.CMVideoTutorial_toolTip,
  svgIcon: typeVideoTutorialIcon,
  properties: {
    title: {
      displayName: TrainingContentTypes_properties.CMVideoTutorial_title_text,
      description: TrainingContentTypes_properties.CMVideoTutorial_title_text,
    },
    detailText: { displayName: TrainingContentTypes_properties.CMVideoTutorial_detailText_text },
    video: {
      displayName: TrainingContentTypes_properties.CMVideoTutorial_video_text,
      emptyText: TrainingContentTypes_properties.CMVideoTutorial_video_emptyText,
    },
    copyright: {
      displayName: TrainingContentTypes_properties.CMVideoTutorial_copyright_text,
      emptyText: TrainingContentTypes_properties.CMVideoTutorial_copyright_emptyText,
    },
    productionInfo: { displayName: TrainingContentTypes_properties.CMVideoTutorial_productionInfo_text },
    duration: {
      displayName: TrainingContentTypes_properties.CMVideoTutorial_duration_text,
      emptyText: TrainingContentTypes_properties.CMVideoTutorial_duration_emptyText,
    },
    featuredProduct: {
      displayName: TrainingContentTypes_properties.CMVideoTutorial_featuredProduct_text,
      emptyText: TrainingContentTypes_properties.CMVideoTutorial_featuredProduct_emptyText,
    },
    pictures: { displayName: TrainingContentTypes_properties.CMVideoTutorial_pictures_text },
    localSettings: {
      properties: {
        targetGroup: {
          displayName: TrainingContentTypes_properties["CMVideoTutorial_localSettings.targetGroup_text"],
          description: TrainingContentTypes_properties["CMVideoTutorial_localSettings.targetGroup_toolTip"],
          emptyText: TrainingContentTypes_properties["CMVideoTutorial_localSettings.targetGroup_emptyText"],
        },
      },
    },
  },
});
