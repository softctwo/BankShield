package com.bankshield.common.security.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * YAML属性源工厂
 * 用于加载YAML格式的配置文件
 * 
 * @author BankShield
 * @version 1.0.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        
        Properties properties = factory.getObject();
        
        String propertySourceName = name != null ? name : resource.getResource().getFilename();
        
        return new PropertiesPropertySource(propertySourceName, properties);
    }
}