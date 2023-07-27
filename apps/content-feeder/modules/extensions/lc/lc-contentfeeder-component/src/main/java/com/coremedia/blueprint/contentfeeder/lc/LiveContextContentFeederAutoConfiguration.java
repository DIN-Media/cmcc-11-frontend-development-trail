package com.coremedia.blueprint.contentfeeder.lc;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/livecontext-contentfeeder.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LiveContextContentFeederAutoConfiguration {
}
