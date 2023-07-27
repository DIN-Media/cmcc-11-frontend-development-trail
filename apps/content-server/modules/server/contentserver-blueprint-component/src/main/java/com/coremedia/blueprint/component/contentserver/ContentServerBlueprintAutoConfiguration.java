package com.coremedia.blueprint.component.contentserver;

import com.coremedia.blueprint.base.multisite.BlueprintMultisiteModelConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(BlueprintMultisiteModelConfiguration.class)
public class ContentServerBlueprintAutoConfiguration {
}
