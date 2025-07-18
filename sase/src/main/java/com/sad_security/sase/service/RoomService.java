package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.controller.RoomController.startRoomBody;
import com.sad_security.sase.controller.RoomController.stopRoomBody;
import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.repository.RoomClasseRepository;
import com.sad_security.sase.repository.RoomRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        return roomRepository.findBynome(roomName);
    }

    // aggiunge la room
    public boolean aggiungiRoom(String room) {

        // Cerco se la room è stata già creata
        Optional<Room> roomName = roomRepository.findBynome(room);
        // Se la room esiste non faccio niente
        if (roomName.isPresent()) {
            System.out.println("la room esiste già");
            return true;
        } else {

            // Creazione room
            Room newcRoom = new Room();

            newcRoom.setNome(room);

            roomRepository.save(newcRoom);
            System.out.println("Room creata");

            return false;
        }

    }

    public boolean checkRoom(String room) {

        // Cerco se la roomè stata già creata
        Optional<Room> roomName = roomRepository.findBynome(room);
        // Se la room esiste non faccio niente
        if (roomName.isPresent()) {
            System.out.println("la room esiste già");
            return true;
        } else {
            System.out.println("la room non esiste");
            return false;
        }
    }

    public List <String> getRoomListbyClasse(String classe) {

        // Cerco se la roomè stata già creata
        List <RoomClasse> roomList = roomClasseRepository.findByClasse(classe);
       
        return roomList.stream()
                   .map(RoomClasse::getRoom)
                   .distinct()
                   .collect(Collectors.toList());
    }

    public boolean getRoomList(String classe) {
    // Cerco se la room è stata già creata
    List<RoomClasse> roomList = roomClasseRepository.findByClasse(classe);

    List<String> roomNames = roomList.stream()
                                     .map(RoomClasse::getRoom)
                                     .collect(Collectors.toList());

    // Se esistono room, restituisco true
    if (!roomNames.isEmpty()) {
        System.out.println("La room esiste già");
        return true;
    } else {
        System.out.println("La room non esiste");
        return false;
    }
}


    public CompletableFuture<String> createRoom(String roomName, MultipartFile yamlFile) {
        try {
            String url = dockerApiBaseUrl + "/crea-room/";

            // Imposto il corpo multipart
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("nomeLab", roomName);
            body.add("yamlFile", new ByteArrayResource(yamlFile.getBytes()) {
                @Override
                public String getFilename() {
                    return "docker-compose.yml";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return CompletableFuture.completedFuture("Risposta server Python: " + response.getBody());
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }

    @Autowired
    private RoomClasseRepository roomClasseRepository;

    public boolean aggiungiassociazione(String classe, String room) {
        // Cerco se l'associazione
        Optional<RoomClasse> roomClass = roomClasseRepository.findByClasseAndRoom(classe, room);
        // Se la classe esiste non faccio niente
        if (roomClass.isPresent()) {
            System.out.println("l'associazione già");
            return true;
        } else {

            // Creazione associazione
            RoomClasse rc = new RoomClasse();
            rc.setClasse(classe);
            rc.setRoom(room);

            roomClasseRepository.save(rc);
            System.out.println("Associazione room class creata");

            return false;
        }
    }

}
