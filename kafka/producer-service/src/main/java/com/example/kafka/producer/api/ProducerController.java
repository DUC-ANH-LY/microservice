package com.example.kafka.producer.api;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class ProducerController {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String topic;

  public ProducerController(
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${demo.topic.name:demo.messages}") String topic
  ) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  @PostMapping("/publish")
  public PublishResponse publish(@RequestBody PublishRequest req) throws Exception {
    if (req == null || !StringUtils.hasText(req.getValue())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value is required");
    }

    String key = StringUtils.hasText(req.getKey()) ? req.getKey() : UUID.randomUUID().toString();
    SendResult<String, String> result = kafkaTemplate.send(topic, key, req.getValue()).get();
    RecordMetadata m = result.getRecordMetadata();
    return new PublishResponse(m.topic(), m.partition(), m.offset(), key);
  }
}

