package org.pk.web_analytics.replay;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: purnimakamath
 */
@EnableKafka
@Component
public class WebAnalyticsListener implements ConsumerSeekAware{//, ConsumerAwareMessageListener{

    ConsumerSeekCallback seekCallback = null;
    TopicPartition topicPartition = null;

    @Autowired
    Environment env;

    @Bean
    public Map<String, String> consumerConfigs(){
        return this.getAllKnownProperties(this.env);
    }

    @Bean
    public ConsumerFactory consumerFactory(){
        return new DefaultKafkaConsumerFactory(this.consumerConfigs());
    }

    @Autowired
    ConsumerFactory<String, String> consumerFactory;

    @KafkaListener(topics = "WEB_ANALYTICS_REPLAY", groupId = "web-analytics")
    public void listen(ConsumerRecord<String, String> consumerRecord, Consumer consumer){

        System.out.println(consumerRecord.key() + ":::" + consumerRecord.value());

    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback consumerSeekCallback) {
        this.seekCallback = consumerSeekCallback;
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
        System.out.println("************&&&&&&&&&&&************");
        System.out.println(map.entrySet());
        this.topicPartition = map.entrySet().iterator().next().getKey();
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback) {
    }

    public void reseek(int location){
        this.seekCallback.seek("WEB_ANALYTICS_REPLAY", topicPartition.partition(),location);
    }

//    @Override
//    public void onMessage(ConsumerRecord consumerRecord, Consumer consumer) {
//
//        consumer.
//
//    }

    //Spring Jira: https://jira.spring.io/browse/SPR-10241
    //Converting Spring Enviroment to Properties Map

    @SuppressWarnings("Duplicates")
    public static Map<String, String> getAllKnownProperties(Environment env) {
        Map<String, String> rtn = new HashMap<>();
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
