const { jangarooConfig } = require("@jangaroo/core");

module.exports = jangarooConfig({
  type: "code",
  sencha: {
    loadEarly: true,
  },
  autoLoad: [
    "./src/init",
  ],
});
