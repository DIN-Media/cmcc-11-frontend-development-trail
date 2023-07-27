package com.coremedia.blueprint.feeder.cae.assets;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = {
        "classpath:META-INF/coremedia/am-caefeeder.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class AssetCaeFeederAutoConfiguration {
}
