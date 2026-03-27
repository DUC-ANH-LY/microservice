package com.example.saga.twophase.coordinator.client;

import com.example.saga.twophase.shared.CommitRequest;
import com.example.saga.twophase.shared.PrepareRequest;
import com.example.saga.twophase.shared.RollbackRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for calling participant services (prepare, commit, rollback).
 */
@Component
public class ParticipantClient {

  private final RestClient restClient;
  private final ParticipantUrls urls;

  public ParticipantClient(ParticipantUrls urls) {
    this.urls = urls;
    this.restClient = RestClient.builder()
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

  public boolean prepare(String baseUrl, PrepareRequest request) {
    return post(baseUrl + "/prepare", request, HttpStatus.OK);
  }

  public boolean commit(String baseUrl, CommitRequest request) {
    return post(baseUrl + "/commit", request, HttpStatus.OK);
  }

  public boolean rollback(String baseUrl, RollbackRequest request) {
    return post(baseUrl + "/rollback", request, HttpStatus.OK);
  }

  private boolean post(String url, Object body, HttpStatus expectedStatus) {
    try {
      ResponseEntity<Void> response = restClient.post()
          .uri(url)
          .body(body)
          .retrieve()
          .toBodilessEntity();
      return response.getStatusCode() == expectedStatus;
    } catch (HttpStatusCodeException e) {
      return false;
    }
  }

  public ParticipantUrls getUrls() {
    return urls;
  }
}
