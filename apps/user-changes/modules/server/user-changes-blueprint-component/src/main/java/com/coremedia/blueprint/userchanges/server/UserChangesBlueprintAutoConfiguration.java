package com.coremedia.blueprint.userchanges.server;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/user-changes-blueprint.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class UserChangesBlueprintAutoConfiguration {
}
