import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import MarketingSpot from "../../src/model/MarketingSpot";

class MarketingSpotTest extends AbstractCatalogTest {

  #marketingSpot: MarketingSpot = null;

  override setUp(): void {
    super.setUp();
    this.#marketingSpot = beanFactory._.getRemoteBeanOfType("livecontext/marketingspot/HeliosSiteId/spot1", MarketingSpot);
  }

  // noinspection JSUnusedGlobalSymbols
  async testMarketingSpots(): Promise<void> {
    await this.#marketingSpot.load();
    Assert.assertEquals(true, this.#marketingSpot.getMarketing() !== null);
  }
}

export default MarketingSpotTest;
