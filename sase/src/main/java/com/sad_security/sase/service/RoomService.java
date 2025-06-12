package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

@Service
public class RoomService {

     private final RestTemplate restTemplate = new RestTemplate();


    @Value("${docker.api.base-url}")
    private String dockerApiBaseUrl;

    @Async
    public CompletableFuture<String> startContainerAsync(String name) {
        try {
            String url = dockerApiBaseUrl + "/start-container/" + name;
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }
    
    
}
