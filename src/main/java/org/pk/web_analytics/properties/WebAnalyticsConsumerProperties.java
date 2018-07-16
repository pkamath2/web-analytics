package org.pk.web_analytics.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:consumer.properties")
public class WebAnalyticsConsumerProperties {

    @Autowired
    Environment env;

    public String getProperty(String propName){
        return this.env.getProperty(propName);
    }

    //Spring Jira: https://jira.spring.io/browse/SPR-10241
    //Converting Spring Enviroment to Properties Map

    @SuppressWarnings("Duplicates")
    public Map<String, String> getAllKnownProperties() {
        Map<String, String> rtn = new HashMap<>();
        if (env instanceof ConfigurableEnvironment) {
            for (org.springframework.core.env.PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                        rtn.put(key, this.getProperty(key).toString());
                    }
                }
            }
        }
        return rtn;
    }
}
