package com.example.kafka.consumer.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
  private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

  private final String instanceName;
  private final String groupId;
  private final String clientId;

  public MessageListener(
      @Value("${demo.instance.name:consumer}") String instanceName,
      @Value("${spring.kafka.consumer.group-id:kafka-consumer-group-demo}") String groupId,
      @Value("${spring.kafka.consumer.client-id:consumer}") String clientId
  ) {
    this.instanceName = instanceName;
    this.groupId = groupId;
    this.clientId = clientId;
  }

  @KafkaListener(
      topics = "${demo.topic.name:demo.messages}",
      groupId = "${spring.kafka.consumer.group-id:kafka-consumer-group-demo}"
  )
  public void onMessage(ConsumerRecord<String, String> record) {
    log.info(
        "instance={} groupId={} clientId={} topic={} partition={} offset={} key={} value={}",
        instanceName,
        groupId,
        clientId,
        record.topic(),
        record.partition(),
        record.offset(),
        record.key(),
        record.value()
    );
  }
}

