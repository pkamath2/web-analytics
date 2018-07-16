package org.pk.web_analytics.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:consumer.properties")
public class WebAnalyticsConsumerProperties {

    @Autowired
    Environment env;

    public String getProperty(String propName){
        return this.env.getProperty(propName);
    }
}