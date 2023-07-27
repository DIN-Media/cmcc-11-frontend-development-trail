import studioApps from "@coremedia/studio-client.app-context-models/apps/studioApps";
import { openOrFocusApp } from "@coremedia/studio-client.app-context-models/openWindow/defaultWindowOpener";
import { observeWindowStudioAppsConnection } from "@coremedia/studio-client.app-context-models/connectedWindows/connectedWindowObserver";

studioApps._.setWindowOpenHandler(openOrFocusApp);
studioApps._.addWindowValidityObservable(observeWindowStudioAppsConnection());
