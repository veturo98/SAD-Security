package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.controller.RoomController.startRoomBody;
import com.sad_security.sase.controller.RoomController.stopRoomBody;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.model.RoomAvviata;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.repository.RoomAvviataRepository;
import com.sad_security.sase.repository.RoomClasseRepository;
import com.sad_security.sase.repository.RoomRepository;

import java.time.Duration;
import java.time.LocalDateTime;
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

    // crea una nuova entry all'interno del database room
    public boolean salvaNuovaRoom(String room, String descrizione, String flag) {

        // Cerco se la room è stata già creata
        Optional<Room> roomName = roomRepository.findBynome(room);
        // Se la room esiste non faccio niente
        if (roomName.isPresent()) {
            System.out.println("la room esiste già");
            return true;
        } else {

            // Creazione room
            Room newRoom = new Room();

            newRoom.setNome(room);
            newRoom.setDescrizione(descrizione);
            newRoom.setFlag(flag);

            roomRepository.save(newRoom);
            System.out.println("Room salvata nel database");

            return false;
        }

    }

    // Controlla se la room esiste
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

    // Ottieni la lista delle room dal nome della classe
    public List<String> getRoomListbyClasse(String classe) {

        // Cerco se la roomè stata già creata
        List<RoomClasse> roomList = roomClasseRepository.findByClasse(classe);

        return roomList.stream()
                .map(RoomClasse::getRoom)
                .distinct()
                .collect(Collectors.toList());
    }

    
    // public boolean getRoomList(String classe) {
    //     // Cerco se la room è stata già creata
    //     List<RoomClasse> roomList = roomClasseRepository.findByClasse(classe);

    //     List<String> roomNames = roomList.stream()
    //             .map(RoomClasse::getRoom)
    //             .collect(Collectors.toList());

    //     // Se esistono room, restituisco true
    //     if (!roomNames.isEmpty()) {
    //         System.out.println("La room esiste già");
    //         return true;
    //     } else {
    //         System.out.println("La room non esiste");
    //         return false;
    //     }
    // }

    public String getDescrizione(String nomeRoom) {
        // Cerco se la room è stata già creata
        Optional<Room> room = roomRepository.findBynome(nomeRoom);

        String descrizioneRoom = room.get().getDescrizione();
        System.out.println("Descrizione di " + nomeRoom + " " + descrizioneRoom);

        // Se esistono room, restituisco true
        if (descrizioneRoom.isEmpty()) {
            System.out.println("Nessuna descrizione!");
            return "Nessuna descrizione";
        } else {
            return descrizioneRoom;
        }
    }

    // Aggiunge la nuova room creata sul server che gestisce le risorse
    public CompletableFuture<String> aggiungiRisorsaServer(String roomName, MultipartFile yamlFile) {
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


    @Autowired
    private RoomAvviataRepository roomAvviataRepository;
    

    //Verifica se l'utente ha avviato una room, e nel caso viene creata l'associazione nel database
    public boolean VerificaRoomAvviata(String room, String studente) {
        
        // Cerco se l'utnete ha già avviato la room
        Optional<RoomAvviata> roomAvviata = roomAvviataRepository.findByRoomAndStudente( room, studente);
        
        // Se la room è stata già avviata dallo studente ritorno
        if (roomAvviata.isPresent()) {
            System.out.println("l'utente ha già avviato la room");
            return true;
        } else {

            // Creazione associazione
            RoomAvviata roomAvv = new RoomAvviata();
            roomAvv.setRoom(room);
            roomAvv.setStudente(studente);
            roomAvv.setTimestamp(LocalDateTime.now());

            roomAvviataRepository.save(roomAvv);
            System.out.println("Associazione creata: room '" + room + "' avviata dallo studente '" + studente + "'.");

            return false;
        }
    }


    // Verifica se la flag inserita è corretta
    public LocalDateTime flagCorretta(String studente, String room ,String flag ){

        // Ottengo la flag corrispondente alla room nel database
       String flagDatabase = roomRepository.findBynome(flag).get().getFlag();

       if(flagDatabase.equals(flag)){

        LocalDateTime startTimestamp = roomAvviataRepository.findTimeByRoomAndStudente(room,studente);
        return startTimestamp;
       }
       return null;
       
    }

     // Calcola score 
    public boolean calcoloScore (String room, String studente, LocalDateTime tempoAvvio, LocalDateTime tempoCompletamento){

       RoomAvviata roomAvv = roomAvviataRepository.findByRoomAndStudente(room, studente).get();

        long tempoImpiegato = Duration.between(tempoAvvio, tempoCompletamento).toMinutes();
        long score = 100-((tempoImpiegato/120)*100);
        
        // RoomAvviata roomAvv = new RoomAvviata();


        if(score<0){
            score =0;
        }
        
        roomAvv.setScore(score);
        roomAvviataRepository.save(roomAvv);
        return true;
    } 

}
