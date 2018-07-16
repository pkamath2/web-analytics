# Local
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic WEB_ANALYTICS_IN

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic WEB_ANALYTICS_OUT

bin/kafka-topics.sh --list --zookeeper localhost:2181

#Confluent Cloud on GCP

ccloud topic create WEB_ANALYTICS_IN
ccloud topic create WEB_ANALYTICS_OUT
ccloud topic create WEB_ANALYTICS_REPLAY

ccloud topic list

ccloud topic describe WEB_ANALYTICS_IN
ccloud topic describe WEB_ANALYTICS_OUT
ccloud topic describe WEB_ANALYTICS_REPLAY

ccloud topic alter WEB_ANALYTICS_IN --config="retention.ms=604800000" //1 week retention
ccloud topic alter WEB_ANALYTICS_REPLAY --config="retention.ms=604800000" //1 week retention
