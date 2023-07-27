package com.coremedia.blueprint.caefeeder.config;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@ImportResource(value = {
        "classpath:META-INF/coremedia/caefeeder-blueprint.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@PropertySource("classpath:META-INF/coremedia/caefeeder-blueprint.properties")
public class CaeFeederBlueprintAutoConfiguration {
}
