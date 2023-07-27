package com.coremedia.blueprint.feeder.content;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/content-feeder-blueprint.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class BlueprintContentFeederAutoConfiguration {
}
