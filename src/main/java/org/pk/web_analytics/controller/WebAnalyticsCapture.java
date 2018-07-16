package org.pk.web_analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.pk.web_analytics.bo.Analytics;
import org.pk.web_analytics.processor.WebAnalyticsProcessor;
import org.pk.web_analytics.replay.WebAnalyticsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebAnalyticsCapture {

    @Autowired
    WebAnalyticsProcessor webAnalyticsProcessor;



    @Autowired
    Environment env;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<String, String>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = getAllKnownProperties(this.env);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<String, String>(producerFactory());
    }

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebAnalyticsListener webAnalyticsListener;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/save")
    public String saveAnalytics(@RequestParam String data,
                                @RequestHeader(value="User-Agent") String userAgent){

        System.out.println("data = [" + data + "], userAgent = [" + userAgent + "]");

        try {
            Map dataMap = objectMapper.readValue(data, HashMap.class);
            Analytics analyticsObj = new Analytics(dataMap.get("name").toString(), userAgent, dataMap.get("preferred_animal").toString(), dataMap.get("gender").toString());
            ProducerRecord<String, String> analyticsProducerRecord = new ProducerRecord<>("WEB_ANALYTICS_IN", new Double(Math.random()*100).toString(), objectMapper.writeValueAsString(analyticsObj));
            kafkaTemplate.send(analyticsProducerRecord);
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }
        return "success";
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/get")
    public String getAnalytics(){

        String analytics = webAnalyticsProcessor.getLatestCount();

        return analytics;
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/push")
    public void pushToReplay(){

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("WEB_ANALYTICS_REPLAY", "REPLAY_KEY", Math.random()+"");
        kafkaTemplate.send(producerRecord);

    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/reseek")
    public void pullToReplay(@RequestParam int offset){
        webAnalyticsListener.reseek(offset);
    }


    //Spring Jira: https://jira.spring.io/browse/SPR-10241
    //Converting Spring Enviroment to Properties Map

    @SuppressWarnings("Duplicates")
    public static Map<String, Object> getAllKnownProperties(Environment env) {
        Map<String, Object> rtn = new HashMap<>();
        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                        if(key.startsWith("producer.")) {
                            String nkey = key.replace("producer.", "");
                            rtn.put(nkey, propertySource.getProperty(key).toString());
                        }
                    }
                }
            }
        }
        return rtn;
    }
}
