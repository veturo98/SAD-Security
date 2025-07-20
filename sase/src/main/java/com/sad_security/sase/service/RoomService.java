package com.sad_security.sase.service;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.controller.RoomController.startRoomBody;
import com.sad_security.sase.controller.RoomController.startRoomResponse;
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

    @Autowired
    private RoomClasseRepository roomClasseRepository;

    @Autowired
    private RoomAvviataRepository roomAvviataRepository;

    // Recupera l'indirizzo del gestore delle risorse dai config
    @Value("${gestore-risorse-url}")
    private String gestoreRisorseApiUrl;

    // Invia la richiesta di avvio dei container al gestore delle risorse
    @Async
    public CompletableFuture<startRoomResponse> startContainerAsync(String classe, String room, String user) {

        try {

            // Prepara i parametri per effettuare la richiesta
            startRoomBody request_body = new startRoomBody(classe, room, user);
            String url = gestoreRisorseApiUrl + "/start-container/";

            // Effettua una POST asincrona verso il gestore delle risorse
            ResponseEntity<startRoomResponse> response = restTemplate.postForEntity(
                    url, request_body, startRoomResponse.class);

            // Restituisce la risposta
            return CompletableFuture.completedFuture(response.getBody());

        } catch (Exception e) {

            // In caso di errore costruisce il messaggio di errore
            startRoomResponse errorResponse = new startRoomResponse(null, null, null);

            errorResponse.setMsg("Errore: " + e.getMessage());
            errorResponse.setCommand("");
            errorResponse.setType("error");

            // E restituisce la risposta
            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    // Invia la richiesta di stop del container al gestore delle risorse
    @Async
    public CompletableFuture<String> stopContainer(String user) {

        try {

            // Prepara i parametri per lo stop dei container
            String url = gestoreRisorseApiUrl + "/stop-container/";
            stopRoomBody request_body = new stopRoomBody(user);

            // Effettua la richiesta al gestore delle risorse
            ResponseEntity<String> response = restTemplate.postForEntity(url, request_body, String.class);

            // Restituisco la risposta della POST
            return CompletableFuture.completedFuture(response.getBody());

        } catch (Exception e) {

            // Restituisco l'errore
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }

    // Restituisce un oggetto room dato il nome
    public Optional<Room> cercaRoom(String roomName) {
        return roomRepository.findBynome(roomName);
    }

    // Salva una room inserita dal professore
    public void salvaNuovaRoom(String room, String descrizione, String flag) {

        // Creazione e salvataggio della room
        Room newRoom = new Room();

        newRoom.setNome(room);
        newRoom.setDescrizione(descrizione);
        newRoom.setFlag(flag);

        System.out.println("Room salvata nel database");

    }

    // Controlla se la room esiste
    public boolean checkRoom(String room) {

        // Cerco se la room è stata già creata
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

        // Cerco se la room è stata già creata
        List<RoomClasse> roomList = roomClasseRepository.findByClasse(classe);

        return roomList.stream()
                .map(RoomClasse::getRoom)
                .distinct()
                .collect(Collectors.toList());
    }

    // Restituisce la descrizione di una room dato il nome
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

            // Prepara i parametri per la richiesta
            String url = gestoreRisorseApiUrl + "/crea-room/";

            // Imposto il corpo multipart
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("nomeLab", roomName);
            body.add("yamlFile", new ByteArrayResource(yamlFile.getBytes()) {
                @Override
                public String getFilename() {
                    return "docker-compose.yml";
                }
            });

            // Costruisce l'header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Costruisce ed invia la richiesta
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return CompletableFuture.completedFuture(response.getBody());
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Errore: " + e.getMessage());
        }
    }

    // Controlla se la room esiste dato il nome
    public boolean isPresent(String roomName) {

        boolean esito;

        Optional<Room> room = roomRepository.findBynome(roomName);

        if (room.isEmpty()) {
            esito = false;
        } else {
            esito = true;
        }

        return esito;
    }

    // Metodo che tiene traccia di una room associata ad una classe
    public boolean aggiungiassociazione(String classe, String room) {

        // Cerco se l'associazione esiste
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

    // Verifica se l'utente ha avviato una room, e nel caso viene creata
    // l'associazione nel database
    public boolean VerificaRoomAvviata(String classe, String room, String studente) {

        // Cerco se l'utnete ha già avviato la room
        Optional<RoomAvviata> roomAvviata = roomAvviataRepository.findByRoomAndStudente(room, studente);

        // Se la room è stata già avviata dallo studente ritorno
        if (roomAvviata.isPresent()) {
            System.out.println("l'utente ha già avviato la room");
            return true;
        } else {
            return false;
        }
    }

    // Salva la rooom in maniera persistente
    public boolean SalvaRoomAvviata(String classe, String room, String studente) {

        // Creazione associazione
        RoomAvviata roomAvv = new RoomAvviata();
        roomAvv.setRoom(room);
        roomAvv.setStudente(studente);
        roomAvv.setClasse(classe);
        roomAvv.setTimestamp(LocalDateTime.now());

        // Se la room non viene salvata restituisce false
        RoomAvviata newRoom = roomAvviataRepository.save(roomAvv);

        if (newRoom.equals(null)) {
            return false;
        }

        // Altrimenti true
        System.out.println("Associazione creata: room '" + room + "' avviata dallo studente '" + studente + "'.");
        return true;
    }

    // Verifica se la flag inserita è corretta
    public LocalDateTime flagCorretta(String studente, String room, String flag) {

        // Ottengo la flag corrispondente alla room nel database
        String flagDatabase = roomRepository.findBynome(room).get().getFlag();

        if (flagDatabase.equals(flag)) {

            LocalDateTime startTimestamp = roomAvviataRepository.findTimeByRoomAndStudente(room, studente).get()
                    .getTimestamp();
            System.out.println("Timestamp di start: " + startTimestamp);
            return startTimestamp;
        }
        return null;

    }

    // Restituisce la lista delle room di cui si vuole visualizzare i risultati
    public List<RoomAvviata> getRoombyClasseAndRoom(String classe, String room) {

        Optional<List<RoomAvviata>> Risultati = roomAvviataRepository.findByClasseAndRoom(classe, room);

        if (Risultati.isEmpty()) {
            return null;
        }

        return Risultati.get();

    }

    // Calcola lo score
    public void calcoloScore(String room, String studente, LocalDateTime tempoAvvio,
            LocalDateTime tempoCompletamento) {

        // Cerco la room per la quale devo calcolare lo score
        RoomAvviata roomAvv = roomAvviataRepository.findByRoomAndStudente(room, studente).get();

        // Parametri del calcolo dello score
        int MaxScore = 100;
        int MaxDurationTime = 120;

        // Calcolo il tempo impiegato dall'utente a completare il laboratorio
        long tempoImpiegato = Duration.between(tempoCompletamento, tempoAvvio).toMinutes();
        System.out.println("tempo impiegato" + tempoImpiegato);

        // Funzione di score
        long score = MaxScore - ((tempoImpiegato * MaxScore) / MaxDurationTime);

        // Se lo score è negativo allora il punteggio è 0
        if (score < 0) {
            score = 0;
        }

        // Setto lo score e salvo la room
        roomAvv.setScore(score);
        roomAvviataRepository.save(roomAvv);

        return;
    }

    // Restituisce gli studenti iscritti alla classe
    public List<RoomClasse> trovaLaboratori(String classe) {
        List<RoomClasse> laboratori = roomClasseRepository.findByClasse(classe);
        return laboratori;
    }

}
