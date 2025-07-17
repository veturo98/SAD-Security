package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.controller.RoomController.createRoomBody;
import com.sad_security.sase.controller.RoomController.startRoomBody;
import com.sad_security.sase.controller.RoomController.stopRoomBody;
import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.ClasseRepository;
import com.sad_security.sase.repository.RoomRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

@Service
public class RoomService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RoomRepository roomRepository;

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

    // Cerca room
    public Optional<Room> cercaRoom(String roomName) {
        return roomRepository.findBylab(roomName);
    }


    // aggiunge la room
    public boolean aggiungiRoom(String room) {

        // Cerco se la roomè stata già creata
        Optional<Room> roomName = roomRepository.findBylab(room);
        // Se la room esiste non faccio niente
        if (roomName.isPresent() ) {
            System.out.println("la room esiste già");
            return true;
        } else {

            // Creazione room 
            Room newcRoom = new Room();

            

            newcRoom.setLab(room);
          
    

            roomRepository.save(newcRoom);
            System.out.println("Room creata");

            return false;
        }


    }

    public boolean checkRoom(String room) {

        // Cerco se la roomè stata già creata
        Optional<Room> roomName = roomRepository.findBylab(room);
        // Se la room esiste non faccio niente
        if (roomName.isPresent() ) {
            System.out.println("la room esiste già");
            return true;
        } else {
            System.out.println("la room non esiste");
            return false;
        }
    }



     public CompletableFuture<String>  createRoom(String classeName, String roomName, MultipartFile yamlFile) {

    
 try {

             // Costruisco l'URL da contattare
            String url = dockerApiBaseUrl + "/crea-lab/";


            // Creo il corpo della POST ed effettuo la richiesta
            createRoomBody request_body = new createRoomBody(classeName, roomName, yamlFile);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request_body, String.class);
         
            // Restituisco la risposta della POST
            return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());
        
        } catch (Exception e) {

            // Restituisco l'errore
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }


    }
//      @Async
// public CompletableFuture<String> creaClasse(String classe, String room, String user) {
//     try {
//         // Costruisco l'URL da contattare
//         String url = dockerApiBaseUrl + "/crea-laboratorio/";

//         // Creo un oggetto con i dati da inviare

//         requestBody.put("nomeClasse", classe);
//         requestBody.put("nomeLab", room);
//         requestBody.put("utente", user);

//         // Invio la richiesta POST
//         ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);

//         // Restituisco la risposta del server Python
//         return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());

//     } catch (Exception e) {
//         // In caso di errore, restituisco il messaggio di errore
//         return CompletableFuture.completedFuture("Errore durante la creazione della classe: " + e.getMessage());
//     }
// }


}
