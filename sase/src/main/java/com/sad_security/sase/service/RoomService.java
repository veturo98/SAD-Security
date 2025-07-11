package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sad_security.sase.controller.RoomController.startRoomBody;
import com.sad_security.sase.controller.RoomController.stopRoomBody;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

@Service
public class RoomService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Recupera il valore di docker-base
    @Value("${docker.api.base-url}")
    private String dockerApiBaseUrl;

    // Effettuo una richiesta asincrona al gestore delle risorse
    @Async
    public CompletableFuture<String> startContainerAsync(String classe, String room, String user) {

        try {

            // Costruisco l'URL da contattare
            String url = dockerApiBaseUrl + "/start-container/";

            // Creo il corpo della POST ed effettuo la richiesta
            startRoomBody request_body = new startRoomBody(classe, room, user);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request_body, String.class);
         
            // Restituisco la risposta della POST
            return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());
        
        } catch (Exception e) {

            // Restituisco l'errore
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }

        // Effettuo una richiesta asincrona al gestore delle risorse
    @Async
    public CompletableFuture<String> stopContainer(String user) {

        try {

            // Costruisco l'URL da contattare
            String url = dockerApiBaseUrl + "/stop-container/";

            // Creo il corpo della POST ed effettuo la richiesta
            stopRoomBody request_body = new stopRoomBody(user);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request_body, String.class);
         
            // Restituisco la risposta della POST
            return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());
        
        } catch (Exception e) {

            // Restituisco l'errore
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }

}
