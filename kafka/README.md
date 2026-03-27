# Kafka consumer group demo (Spring Boot)

This demo shows **consumer groups**, **partition assignment**, and **rebalancing** using 2 Spring Boot apps:

- `producer-service`: REST API to publish messages to Kafka
- `consumer-service`: Kafka listener; run multiple instances with the **same** `groupId` to see partitions split/rebalanced

## Prereqs

- Java 17
- Maven
- Docker + Docker Compose

## Start Kafka locally

From `microservice/kafka`:

```bash
docker compose up -d
```

Kafka UI runs on `http://localhost:8088`.

## Run producer

```bash
cd producer-service
mvn spring-boot:run
```

Producer runs on `http://localhost:8081`.

## Run 2 consumer instances (same group)

Terminal A:

```bash
cd consumer-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

Terminal B (same `groupId`, different `clientId` + port):

```bash
cd consumer-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --spring.kafka.consumer.client-id=consumer-b"
```

Watch the consumer logs for `partition=...` to see how partitions are assigned, then stop/start one instance to trigger rebalancing.

## Publish messages

```bash
curl -X POST "http://localhost:8081/publish" \
  -H "Content-Type: application/json" \
  -d '{"key":"k1","value":"hello"}'
```

Try different keys to see stable partitioning for the same key.

- Producer Overview
        - Serializer -> convert java obj to []bytes
        - Partition -> key define what partition to go, if null choose partition by round-robin algorithm else hash the key then mapping to a partition 
            how to choose number of partition ? https://www.confluent.io/blog/how-choose-number-topics-partitions-kafka-cluster/
            https://stackoverflow.com/questions/38024514/understanding-kafka-topics-and-partitions
            https://viblo.asia/p/005-bao-nhieu-partition-la-du-cho-mot-topic-trong-apache-kafka-V3m5WQxQZO7
        - Header: add metadata https://www.redpanda.com/guides/kafka-cloud-kafka-headers
        - Interceptor https://oneuptime.com/blog/post/2026-01-30-kafka-interceptors/view
        - Quota and throting Kafka quotas and throttling protect brokers from resource exhaustion by enforcing data throughput (bytes/sec) and request rate (CPU %) limits on producers and consumers. 
- Kafka consumer 
        - consumer 1 subscribe a topic 1 -> get all message from all `partitions` in topic 1