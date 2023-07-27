package com.coremedia.blueprint.contentfeeder.corporate;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/corporate-contentfeeder.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class CorporateContentFeederAutoConfiguration {
}
