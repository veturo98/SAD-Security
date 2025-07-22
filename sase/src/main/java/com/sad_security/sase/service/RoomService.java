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

    /**
     * Recupera l'indirizzo del gestore delle risorse dai config.
     */
    @Value("${gestore-risorse-url}")
    private String gestoreRisorseApiUrl;

    /**
     * Invia la richiesta di avvio dei container al gestore delle risorse in modo
     * asincrono.
     *
     * @param classe nome della classe
     * @param room   nome della room
     * @param user   nome utente che avvia il container
     * @return CompletableFuture con la risposta startRoomResponse
     */
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

    /**
     * Invia la richiesta di stop del container al gestore delle risorse in modo
     * asincrono.
     *
     * @param user nome utente che richiede lo stop
     * @return CompletableFuture con la risposta della richiesta (String)
     */
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

    /**
     * Restituisce un oggetto Room dato il nome.
     *
     * @param roomName nome della room da cercare
     * @return Optional contenente la Room se trovata
     */
    public Optional<Room> cercaRoom(String roomName) {
        return roomRepository.findBynome(roomName);
    }

    /**
     * Salva una nuova room inserita dal professore.
     *
     * @param room        nome della room
     * @param descrizione descrizione della room
     * @param flag        flag associata alla room
     */
    public void salvaNuovaRoom(String room, String descrizione, String flag) {

        // Creazione e salvataggio della room
        Room newRoom = new Room();

        newRoom.setNome(room);
        newRoom.setDescrizione(descrizione);
        newRoom.setFlag(flag);

        roomRepository.save(newRoom);

        System.out.println("Room salvata nel database");

    }

    /**
     * Controlla se la room esiste.
     *
     * @param room nome della room
     * @return true se la room esiste, false altrimenti
     */
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

    /**
     * Ottiene la lista delle room associate al nome di una classe.
     *
     * @param classe nome della classe
     * @return lista di nomi di room
     */
    public List<String> getRoomListbyClasse(String classe) {

        // Cerco se la room è stata già creata
        List<RoomClasse> roomList = roomClasseRepository.findByClasse(classe);

        return roomList.stream()
                .map(RoomClasse::getRoom)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Restituisce la descrizione di una room dato il nome.
     *
     * @param nomeRoom nome della room
     * @return descrizione della room o messaggio "Nessuna descrizione"
     */
    public String getDescrizione(String nomeRoom) {
        // Cerco se la room è stata già creata
        Optional<Room> room = roomRepository.findBynome(nomeRoom);

        String descrizioneRoom = room.get().getDescrizione();
        System.out.println("Descrizione di " + nomeRoom + " " + descrizioneRoom);

        // Se esistono room, restituisco la descrizione o messaggio
        if (descrizioneRoom.isEmpty()) {
            System.out.println("Nessuna descrizione!");
            return "Nessuna descrizione";
        } else {
            return descrizioneRoom;
        }
    }

    /**
     * Aggiunge una nuova risorsa sul server che gestisce le risorse, caricando un
     * file yaml.
     *
     * @param roomName nome della room
     * @param yamlFile file yaml multipart da caricare
     * @return CompletableFuture con la risposta (String) della chiamata al server
     *         risorse
     */
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

    /**
     * Controlla se una room è presente dato il nome.
     *
     * @param roomName nome della room
     * @return true se la room è presente, false altrimenti
     */
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

    /**
     * Tiene traccia di una room associata ad una classe.
     *
     * @param classe nome della classe
     * @param room   nome della room
     * @return true se l'associazione esiste già, false se creata nuova
     */
    public boolean aggiungiassociazione(String classe, String room) {

        // Cerco se l'associazione esiste
        Optional<RoomClasse> roomClass = roomClasseRepository.findByClasseAndRoom(classe, room);

        // Se l'associazione esiste non faccio niente
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

    /**
     * Verifica se l'utente ha avviato una room.
     *
     * @param classe   nome della classe
     * @param room     nome della room
     * @param studente nome dello studente
     * @return true se la room è già stata avviata dallo studente, false altrimenti
     */
    public boolean VerificaRoomAvviata(String classe, String room, String studente) {

        // Cerco se l'utente ha già avviato la room
        Optional<RoomAvviata> roomAvviata = roomAvviataRepository.findByRoomAndStudente(room, studente);

        // Se la room è stata già avviata dallo studente ritorno true
        if (roomAvviata.isPresent()) {
            System.out.println("l'utente ha già avviato la room");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Salva persistentemente una room avviata da uno studente.
     *
     * @param classe   la classe di riferimento
     * @param room     il nome della room avviata
     * @param studente l'identificativo dello studente che avvia la room
     * @return true se il salvataggio è andato a buon fine, false altrimenti
     */
    public boolean SalvaRoomAvviata(String classe, String room, String studente) {

        RoomAvviata roomAvv = new RoomAvviata();
        roomAvv.setRoom(room);
        roomAvv.setStudente(studente);
        roomAvv.setClasse(classe);
        roomAvv.setTimestamp(LocalDateTime.now());

        roomAvviataRepository.save(roomAvv);

        System.out.println("Associazione creata: room '" + room + "' avviata dallo studente '" + studente + "'.");
        return true;
    }

    /**
     * Verifica se la flag inserita dallo studente corrisponde a quella memorizzata
     * nel database per la room.
     *
     * @param studente l'identificativo dello studente che inserisce la flag
     * @param room     il nome della room
     * @param flag     la flag fornita dallo studente
     * @return il timestamp di avvio della room se la flag è corretta, null
     *         altrimenti
     */
    public LocalDateTime flagCorretta(String studente, String room, String flag) {

        String flagDatabase = roomRepository.findBynome(room).get().getFlag();

        if (flagDatabase.equals(flag)) {

            LocalDateTime startTimestamp = roomAvviataRepository.findTimeByRoomAndStudente(room, studente).get()
                    .getTimestamp();
            System.out.println("Timestamp di start: " + startTimestamp);
            return startTimestamp;
        }
        return null;
    }

    /**
     * Restituisce la lista delle room avviate di una determinata classe e room.
     *
     * @param classe il nome della classe
     * @param room   il nome della room
     * @return lista di RoomAvviata associata, oppure null se non ci sono risultati
     */
    public List<RoomAvviata> getRoombyClasseAndRoom(String classe, String room) {

        Optional<List<RoomAvviata>> Risultati = roomAvviataRepository.findByClasseAndRoom(classe, room);

        if (Risultati.isEmpty()) {
            return null;
        }

        return Risultati.get();
    }

    /**
     * Calcola lo score di uno studente per il completamento di una room basandosi
     * sul tempo impiegato.
     *
     * @param room               il nome della room
     * @param studente           l'identificativo dello studente
     * @param tempoAvvio         il timestamp di inizio della room
     * @param tempoCompletamento il timestamp di completamento della room
     */
    public void calcoloScore(String room, String studente, LocalDateTime tempoAvvio,
            LocalDateTime tempoCompletamento) {

        RoomAvviata roomAvv = roomAvviataRepository.findByRoomAndStudente(room, studente).get();

        int MaxScore = 100;
        int MaxDurationTime = 120; // in minuti

        // Calcolo del tempo impiegato dall'utente (durata negativa corretta)
        long tempoImpiegato = Duration.between(tempoCompletamento,tempoAvvio ).toMinutes();
        System.out.println("tempo impiegato: " + tempoImpiegato);

        // Calcolo del punteggio con formula lineare
        long score = MaxScore - ((tempoImpiegato * MaxScore) / MaxDurationTime);

        if (score < 0) {
            score = 0;
        }

        roomAvv.setScore(score);
        roomAvviataRepository.save(roomAvv);
    }

    /**
     * Restituisce la lista dei laboratori (associazioni room-classe) per una
     * specifica classe.
     *
     * @param classe il nome della classe
     * @return lista di RoomClasse associati alla classe
     */
    public List<RoomClasse> trovaLaboratori(String classe) {
        return roomClasseRepository.findByClasse(classe);
    }
}
