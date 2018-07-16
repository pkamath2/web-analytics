package org.pk.web_analytics.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.pk.web_analytics.bo.Analytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class WebAnalyticsProcessor {

    @Autowired
    Environment env;

    @Autowired
    ObjectMapper objectMapper;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs() {
        Map<String, String> props = getAllKnownProperties(this.env);
        return new StreamsConfig(props);
    }

    @Bean
    public StreamsBuilderFactoryBean kStreamBuilder(StreamsConfig streamsConfig) {
        return new StreamsBuilderFactoryBean(streamsConfig);
    }

    @Autowired
    @Qualifier("&kStreamBuilder")
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;


    @Bean
    public KStream<String, String> createAnalyticsInStream(StreamsBuilder streamsBuilder){

        KStream<String, String> analyticsKStream = streamsBuilder.stream("WEB_ANALYTICS_IN");


        analyticsKStream.flatMap((s, analyticsString) -> {
            List<KeyValue<String, String>> analytics = new ArrayList<>();
            Analytics analyticsObj = null;

            try {
                analyticsObj = objectMapper.readValue(analyticsString, Analytics.class);

                analytics.add(new KeyValue<>("NAME", analyticsObj.getName()));
                analytics.add(new KeyValue<>("PREFERRED_ANIMAL", analyticsObj.getPreferred_animal()));
                analytics.add(new KeyValue<>("GENDER", analyticsObj.getGender()));
                if(analyticsObj.getUser_agent().indexOf("Macintosh") >=0)
                    analytics.add(new KeyValue<>("DEVICE", "Apple"));
                else if(analyticsObj.getUser_agent().indexOf("iPhone") >=0)
                    analytics.add(new KeyValue<>("DEVICE", "iPhone"));
                else if(analyticsObj.getUser_agent().indexOf("Linux") >=0)
                    analytics.add(new KeyValue<>("DEVICE", "Linux"));
                else if(analyticsObj.getUser_agent().indexOf("Windows") >=0 || analyticsObj.getUser_agent().indexOf("windows") >=0)
                    analytics.add(new KeyValue<>("DEVICE", "Windows"));
                else
                    analytics.add(new KeyValue<>("DEVICE", "No Idea!"));


                if(analyticsObj.getUser_agent().indexOf(" Chrome/") >=0)
                    analytics.add(new KeyValue<>("BROWSER", "Chrome"));
                else if(analyticsObj.getUser_agent().indexOf(" Safari/") >=0 )
                    analytics.add(new KeyValue<>("BROWSER", "Safari"));
                else if(analyticsObj.getUser_agent().indexOf("MSIE") >=0 || analyticsObj.getUser_agent().indexOf("(Windows NT") >=0 )
                    analytics.add(new KeyValue<>("BROWSER", "IE"));
                else
                    analytics.add(new KeyValue<>("BROWSER", "No Idea!"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return analytics;
        }).to("WEB_ANALYTICS_OUT");

        return analyticsKStream;
    }

    @Bean
    public KStream<String, String> createAnalyticsOutStream(StreamsBuilder streamsBuilder){

        Predicate preferredAnimalPredicate = (k,v) -> k.equals("PREFERRED_ANIMAL");
        Predicate genderPredicate = (k,v) -> k.equals("GENDER");
        Predicate devicePredicate = (k,v) -> k.equals("DEVICE");
        Predicate browserPredicate = (k,v) -> k.equals("BROWSER");

        KStream<String, String> consolidatedStream = streamsBuilder.stream("WEB_ANALYTICS_OUT");

        KStream<String, String>[] all_streams = consolidatedStream.branch(preferredAnimalPredicate,
                                                                            genderPredicate,
                                                                            devicePredicate,
                                                                            browserPredicate);

        all_streams[0].groupBy((k,v) -> v.toUpperCase()).count(Materialized.as("PREFERRED_ANIMAL_STORE"));
        all_streams[1].groupBy((k,v) -> v.toUpperCase()).count(Materialized.as("GENDER_STORE"));
        all_streams[2].groupBy((k,v) -> v.toUpperCase()).count(Materialized.as("DEVICE_STORE"));
        all_streams[3].groupBy((k,v) -> v.toUpperCase()).count(Materialized.as("BROWSER_STORE"));

        return consolidatedStream;
    }


    public String getLatestCount(){

        ReadOnlyKeyValueStore<String, String> store1 = streamsBuilderFactoryBean.getKafkaStreams().store("PREFERRED_ANIMAL_STORE", QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, String> store2 = streamsBuilderFactoryBean.getKafkaStreams().store("GENDER_STORE", QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, String> store3 = streamsBuilderFactoryBean.getKafkaStreams().store("DEVICE_STORE", QueryableStoreTypes.keyValueStore());
        ReadOnlyKeyValueStore<String, String> store4 = streamsBuilderFactoryBean.getKafkaStreams().store("BROWSER_STORE", QueryableStoreTypes.keyValueStore());

        Map<String, String> preferredAnimalCount = new HashMap<>();
        Map<String, String> genderCount = new HashMap<>();
        Map<String, String> deviceCount = new HashMap<>();
        Map<String, String> browserCount = new HashMap<>();

        store1.all().forEachRemaining(kv -> preferredAnimalCount.put(kv.key,kv.value));
        store2.all().forEachRemaining(kv -> genderCount.put(kv.key,kv.value));
        store3.all().forEachRemaining(kv -> deviceCount.put(kv.key,kv.value));
        store4.all().forEachRemaining(kv -> browserCount.put(kv.key,kv.value));

        Map<String, Map<String, String>> all_count = new HashMap<>();
        all_count.put("PREFERRED_ANIMAL", preferredAnimalCount);
        all_count.put("GENDER", genderCount);
        all_count.put("DEVICE", deviceCount);
        all_count.put("BROWSER", browserCount);

        String latest_count = "";

        try {
            latest_count = objectMapper.writeValueAsString(all_count);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return latest_count;

    }


    //Spring Jira: https://jira.spring.io/browse/SPR-10241
    //Converting Spring Enviroment to Properties Map

    @SuppressWarnings("Duplicates")
    public static Map<String, String> getAllKnownProperties(Environment env) {
        Map<String, String> rtn = new HashMap<>();
        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                        if(key.startsWith("consumer.")) {
                            String nkey = key.replace("consumer.", "");
                            rtn.put(nkey, propertySource.getProperty(key).toString());
                        }
                    }
                }
            }
        }
        return rtn;
    }


}
