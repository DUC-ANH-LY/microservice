package com.example.saga.twophase.coordinator.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "participant")
public class ParticipantUrls {

  private String payment = "http://localhost:8091";
  private String inventory = "http://localhost:8092";
  private String shipping = "http://localhost:8093";

  public String getPayment() {
    return payment;
  }

  public void setPayment(String payment) {
    this.payment = payment;
  }

  public String getInventory() {
    return inventory;
  }

  public void setInventory(String inventory) {
    this.inventory = inventory;
  }

  public String getShipping() {
    return shipping;
  }

  public void setShipping(String shipping) {
    this.shipping = shipping;
  }
}
