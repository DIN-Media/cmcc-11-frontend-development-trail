package com.coremedia.blueprint.assets.feeder;

import com.coremedia.blueprint.assets.studio.validation.AssetValidatorsConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@Import(AssetValidatorsConfiguration.class)
@ImportResource(value = "classpath:META-INF/coremedia/am-contentfeeder.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class AssetContentFeederAutoConfiguration {
}
