import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import RemoteBean from "@coremedia/studio-client.client-core/data/RemoteBean";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { as } from "@jangaroo/runtime";
import CatalogObject from "../../src/model/CatalogObject";

class CatalogObjectTest extends AbstractCatalogTest {

  #catalogObject: CatalogObject & RemoteBean = null;

  override setUp(): void {
    super.setUp();
    this.#catalogObject = beanFactory._.getRemoteBeanOfType("livecontext/store/HeliosSiteId", CatalogObject);
  }

  async testCatalogObject(): Promise<void> {
    await this.#catalogObject.load();
    Assert.assertEquals("PerfectChefESite", this.#catalogObject.getName());
  }

}

export default CatalogObjectTest;
