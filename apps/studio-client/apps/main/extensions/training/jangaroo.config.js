const { jangarooConfig } = require("@jangaroo/core");

module.exports = jangarooConfig({
  type: "code",
  autoLoad: [
    "./src/initContentTypeLocalization",
  ],
  sencha: {
    name: "com.coremedia.blueprint__training-studio",
    namespace: "com.coremedia.blueprint.training.studio",
    studioPlugins: [
      {
        mainClass: "com.coremedia.blueprint.training.studio.TrainingStudioPlugin",
        name: "Training Studio Plugin",
      },
    ],
  },
});
