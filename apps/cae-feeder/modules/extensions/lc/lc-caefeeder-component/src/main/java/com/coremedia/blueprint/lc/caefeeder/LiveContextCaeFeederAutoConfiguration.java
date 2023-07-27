package com.coremedia.blueprint.lc.caefeeder;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = {
        "classpath:META-INF/coremedia/livecontext-caefeeder.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LiveContextCaeFeederAutoConfiguration {
}
