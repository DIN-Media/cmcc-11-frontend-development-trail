import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import ViewTypeSelectorForm from "./ViewTypeSelectorForm";

interface ContainerViewTypeSelectorFormConfig extends Config<ViewTypeSelectorForm> {
}

class ContainerViewTypeSelectorForm extends ViewTypeSelectorForm {
  declare Config: ContainerViewTypeSelectorFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.containerViewTypeSelectorForm";

  constructor(config: Config<ContainerViewTypeSelectorForm> = null) {
    super(ConfigUtils.apply(config, Config(ContainerViewTypeSelectorForm, { paths: [
        // add existing paths
        ...config.paths,
        // new folder paths since 2310.1, keep in sync with PlacementFieldViewtypeSelectorBase
        "/Settings/Options/Viewtypes/Container/",
        "Options/Viewtypes/Container/",
        // deprecated folder paths
        "/Settings/Options/Viewtypes/CMChannel/",
        "Options/Viewtypes/CMChannel/"
      ]
    })));
  }
}

export default ContainerViewTypeSelectorForm;
