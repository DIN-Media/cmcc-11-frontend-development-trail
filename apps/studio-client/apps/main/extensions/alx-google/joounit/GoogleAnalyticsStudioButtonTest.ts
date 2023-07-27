import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import AbstractRemoteTest from "@coremedia/studio-client.client-core-test-helper/AbstractRemoteTest";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import { MockCall } from "@coremedia/studio-client.client-core-test-helper/MockFetch";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import createComponentSelector from "@coremedia/studio-client.ext.ui-components/util/createComponentSelector";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import Button from "@jangaroo/ext-ts/button/Button";
import Viewport from "@jangaroo/ext-ts/container/Viewport";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { as } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import int from "@jangaroo/runtime/int";
import GoogleAnalyticsReportPreviewButton from "../src/GoogleAnalyticsReportPreviewButton";
import GoogleAnalyticsStudioButtonTestView from "./GoogleAnalyticsStudioButtonTestView";

class GoogleAnalyticsStudioButtonTest extends AbstractRemoteTest {

  #viewPort: Viewport = null;

  constructor() {
    super();
  }

  override setUp(): void {
    super.setUp();

    EditorContextImpl.initEditorContext();

    this.#viewPort = new GoogleAnalyticsStudioButtonTestView(
      Config(GoogleAnalyticsStudioButtonTestView, { contentValueExpression: ValueExpressionFactory.createFromValue() }));
  }

  override tearDown(): void {
    super.tearDown();
  }

  // noinspection JSUnusedGlobalSymbols
  testButtonDisabled(): void {
    const button = as(
      this.#viewPort.down(createComponentSelector()._xtype(GoogleAnalyticsReportPreviewButton.xtype).build()), GoogleAnalyticsReportPreviewButton);
    Assert.assertTrue(button.disabled);
  }

  // noinspection JSUnusedGlobalSymbols
  async testDeepLinkReportUrl(): Promise<void> {
    let args: any = undefined;
    window.open = ((...myArgs): Window => {
      args = myArgs;
      return window;
    });

    const button = as(
      this.#viewPort.down(createComponentSelector()._xtype(GoogleAnalyticsReportPreviewButton.xtype).build()), GoogleAnalyticsReportPreviewButton);
    button.setContent(Object.setPrototypeOf({
      getNumericId(): int {
        return 42;
      },
      get(prop: string): any {
        if (prop === "type") {
          return { name: "typeWithPreview" };
        }
      },
    }, Content.prototype));
    // wait until button is disabled:
    await waitUntil((): boolean => !button.disabled);
    (button.handler as (button: Button) => any)(button); // simulate click

    // wait until window is opened:
    await waitUntil((): boolean => args !== undefined);
  }

  static readonly #DRILLDOWN_URL: string = "http://host.domain.net/gai/drilldown/42";

  protected override getMockCalls(): MockCall[] {
    return [
      {
        "request": { "uri": "alxservice/42" },
        "response": { "body": { "googleAnalytics": GoogleAnalyticsStudioButtonTest.#DRILLDOWN_URL } },
      },
    ];
  }

}

export default GoogleAnalyticsStudioButtonTest;
