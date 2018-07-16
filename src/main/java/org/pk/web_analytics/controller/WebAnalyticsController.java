package org.pk.web_analytics.controller;

import org.pk.web_analytics.processor.WebAnalyticsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebAnalyticsController {

    @Autowired
    WebAnalyticsProcessor webAnalyticsProcessor;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/web-analytics/get")
    public String getAnalytics(){
        String analytics = webAnalyticsProcessor.getLatestCount();
        return analytics;
    }
}
