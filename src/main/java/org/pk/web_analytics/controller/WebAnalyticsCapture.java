package org.pk.web_analytics.controller;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.pk.web_analytics.processor.WebAnalyticsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebAnalyticsCapture {

    @Autowired
    WebAnalyticsProcessor webAnalyticsProcessor;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/save")
    public String saveAnalytics(){

        String analytics_data = "{\"name\":\"PK\",\"user-agent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36\"," +
                "\"gender\":\"F\",\"preferred_animal\":\"dog\"}";

        ProducerRecord<String, String> analyticsProducerRecord = new ProducerRecord<> ("WEB_ANALYTICS_IN", new Double(Math.random()*100).toString(), analytics_data);
        kafkaTemplate.send(analyticsProducerRecord);

        return "success";
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/get")
    public String getAnalytics(){

        String analytics = webAnalyticsProcessor.getLatestCount();

        return analytics;
    }
}
