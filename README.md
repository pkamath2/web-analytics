# Web Analytics

The eventual goal of this application is to update the local state stores with the latest count for each metric. 
It uses Kafka Streams to aggregate data from Kafka topic WEB_ANALYTICS_IN and save it down to state stores locally. 

The events/data on WEB_ANALYTICS_IN are produced by the application web-analytics-capture. These events are streamed and processed to create enriched/updated requests on the topic WEB_ANALYTICS_OUT. 
The events on the WEB_ANALYTICS_OUT topic are aggregated into local state stores.

Topic WEB_ANALYTICS_IN - Requests from the capture services are added to this topic. Events/messages are read out of this topic, processed (enriched etc) and added back to the WEB_ANALYTICS_OUT topic.
Topic WEB_ANALYTICS_OUT - Requests from this topic are streamed and aggregated into local state stores 

## Steps to run the application 
* Create a confluent cloud account (details in the scripts below) and set env variables CONFLUENT_USERNAME and CONFLUENT_PASSWORD
* Run the Spring Boot Application (providing -D parameters in the command line with the confluent credentials)
* /web-analytics/get gives a response with the latest count in the state stores. 



Some scripts to create the topics locally and Confluent Cloud (More details on how to use Confluent Cloud - https://docs.confluent.io/current/quickstart/cloud-quickstart.html)

## Local
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic WEB_ANALYTICS_IN

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic WEB_ANALYTICS_OUT

bin/kafka-topics.sh --list --zookeeper localhost:2181

## Confluent Cloud on GCP

ccloud topic create WEB_ANALYTICS_IN
ccloud topic create WEB_ANALYTICS_OUT
ccloud topic create WEB_ANALYTICS_REPLAY

ccloud topic list

ccloud topic describe WEB_ANALYTICS_IN
ccloud topic describe WEB_ANALYTICS_OUT
ccloud topic describe WEB_ANALYTICS_REPLAY

ccloud topic alter WEB_ANALYTICS_IN --config="retention.ms=604800000" //1 week retention
ccloud topic alter WEB_ANALYTICS_REPLAY --config="retention.ms=604800000" //1 week retention

