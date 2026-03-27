package com.example.kafka.producer.api;

public class PublishResponse {
  private final String topic;
  private final Integer partition;
  private final Long offset;
  private final String key;

  public PublishResponse(String topic, Integer partition, Long offset, String key) {
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
    this.key = key;
  }

  public String getTopic() {
    return topic;
  }

  public Integer getPartition() {
    return partition;
  }

  public Long getOffset() {
    return offset;
  }

  public String getKey() {
    return key;
  }
}

