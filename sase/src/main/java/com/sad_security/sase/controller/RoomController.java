package com.sad_security.sase.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sad_security.sase.model.RoomAvviata;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// CONTROLLER PER LE RICHIESTE DI ROOM MANAGEMENT
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // Gestione della richiesta di avvio della room
    @PostMapping("/studente/start")
    public Map<String, String> startRoom(@RequestBody startRoomBody startRoom)
            throws InterruptedException, ExecutionException {

        // Formatta i campi pre l'invio della richiesta
        String classe = startRoom.getNomeClass();
        String lab = startRoom.getNomeLab();
        String utente = startRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + startRoom);

        // Controlla se la room è già stata avviata dall'utente
        boolean isPresent = roomService.VerificaRoomAvviata(classe, lab, utente);

        // Avvia il container
        CompletableFuture<startRoomResponse> esito_avvio = roomService.startContainerAsync(classe, lab, utente);

        Map<String, String> res = new HashMap<>();

        // In base all'esito dell'avvio costruisce la risposta
        if (esito_avvio.get().type.equals("success")) {

            // Se la room non è mai stata avviata allora salva persistente
            if (isPresent == false) {
                boolean esito_salvataggio = roomService.SalvaRoomAvviata(classe, lab, utente);

                // Se fallisce il salvataggio lo comunico solo al backend
                if (esito_salvataggio == false) {
                    System.out.println("Errore nel salvataggio della room");
                }
            }

            res.put("msg", esito_avvio.get().msg);
            res.put("command", esito_avvio.get().command);
            res.put("type", "success");
        } else {
            res.put("msg", esito_avvio.get().msg);
            res.put("type", "error");
        }

        return res;
    }

    // Gestione della richiesta di stop della room
    @PostMapping("/studente/stop")
    public Map<String, String> stopRoom(@RequestBody stopRoomBody stopRoom)
            throws InterruptedException, ExecutionException, JsonMappingException, JsonProcessingException {

        // Invoca il service che si occupa di stoppare il container
        String utente = stopRoom.getUtente();
        System.out.println("Sono il controller ed ho ricevuto questo " + stopRoom);

        CompletableFuture<String> response = roomService.stopContainer(utente);
        String result = response.get();
        System.out.println("result" + result);

        // Restituisce il messaggio adeguato
        Map<String, String> res = new HashMap<>();

        if (result == null || result.isEmpty()) {
            res.put("msg", "Impossibile stoppare il container");
            res.put("type", "error");
        } else {
            ObjectMapper mapper = new ObjectMapper();
            res = mapper.readValue(result, new TypeReference<Map<String, String>>() {
            });
        }

        return res;
    }

    // Gestione della richiesta di creazione della room
    @PostMapping("/professore/creaRoom")
    @ResponseBody
    public Map<String, String> creaRoom(
            @RequestParam("classeId") String classeNome,
            @RequestParam("room") String roomName,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("flag") String flag,
            @RequestParam("yamlFile") MultipartFile yamlFile) {

        Map<String, String> response = new HashMap<>();

        try {
            // Verifica che il file non sia vuoto e sia YAML
            String originalFilename = yamlFile.getOriginalFilename();

            if (yamlFile.isEmpty() ||
                    originalFilename == null ||
                    (!originalFilename.endsWith(".yaml") &&
                            !originalFilename.endsWith(".yml"))) {
                response.put("message", "File non valido. Deve essere un file .yaml o .yml");
                response.put("type", "error");
                return response;
            }

            // Legge il contenuto YAML (opzionale, a seconda di cosa ti serve)
            String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Contenuto YAML:\n" + yamlContent);

            // Salva la room nel database
            roomService.salvaNuovaRoom(roomName, descrizione, flag);

            // Salva l'associazione
            boolean associazione = roomService.aggiungiassociazione(classeNome, roomName);
            if (associazione) {
                response.put("message", "L'associazione tra questa classe e room è già stata fatta ");
                response.put("type", "error");
                return response;
            }

            // Invoca il service per inviare dati al backend
            roomService.aggiungiRisorsaServer(roomName, yamlFile);

            response.put("message", "Laboratorio creato con successo.");
            response.put("type", "success");
        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "Errore durante il caricamento del file.");
            response.put("type", "error");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Errore nella creazione del laboratorio.");
            response.put("type", "error");
        }

        return response;
    }

    // Gestione della richiesta di controllo dell'esistenza della room
    @GetMapping({ "/professore/checkroom", "/studente/checkroom" })
    @ResponseBody
    public Map<String, String> checkroomRoomName(@RequestParam String roomName) {

        // Invoca il service per controllare se la room esiste
        boolean exists = roomService.checkRoom(roomName);
        Map<String, String> response = new HashMap<>();
        if (exists) {
            response.put("message", "La room esiste già");
            response.put("type", "error");
        } else {
            response.put("message", "Nome room disponibile");
            response.put("type", "success");
        }

        return response;
    }

    // Gestione della richiesta della descrizione della room
    @GetMapping("/studente/getDescrizioneRoom")
    public Map<String, String> getDescrizioneRoom(@RequestParam String nomeRoom) {

        // Invoca il service per ottenere la descrizione
        String descrizione = roomService.getDescrizione(nomeRoom);

        Map<String, String> res = new HashMap<>();

        if (descrizione == null || descrizione.isEmpty()) {
            res.put("msg", "Impossibile stoppare il container");
            res.put("type", "error");
        } else {
            res.put("descrizione", descrizione);
            res.put("type", "success");
        }

        return res;
    }

    // Gestione della richiesta delle room associate ad una classe
    @GetMapping({"studente/getRoomsPerClasse", "professore/getRoomsPerClasse"})
    public ResponseEntity<List<String>> getRoomsPerClasse(@RequestParam String nomeClasse) {
        List<String> rooms = roomService.getRoomListbyClasse(nomeClasse);
        return ResponseEntity.ok(rooms);
    }

    // Gestione della richiesta di inserimento della flag
    @PostMapping("/studente/flag")
    public Map<String, String> InserimentoFlag(@RequestParam String room, @RequestParam String studente,
            @RequestParam String flag) {

        // Salva il timestamp di arrivo della richiesta
        LocalDateTime submitTime = LocalDateTime.now();

        // Controlla se la falg è corretta con il service adeguato
        LocalDateTime startTime = roomService.flagCorretta(studente, room, flag);

        Map<String, String> response = new HashMap<>();

        // Se la flag è corretta restituisce il tempo, se non è nullo allora calcolo lo
        // score
        if (startTime != null) {

            // Invoca il service di calcolo dello score
            roomService.calcoloScore(room, studente, submitTime, startTime);

            response.put("esito", "Flag corretta!");
            response.put("type", "success");

        } else {
            response.put("esito", "Flag errata");
            response.put("type", "error");
        }

        return response;
    }

    // Gestione della richiesta di visualizzazione dei risultati
    @PostMapping({ "/professore/risultati/visualizza", "/studente/risultati/visualizza" })
    @ResponseBody
    public List<Map<String, Object>> visualizzazioneRisultati(
            @RequestParam("classeId") String classe,
            @RequestParam("roomId") String room) {

        // Invoca il service per la visualizzazione dei risultati
        List<RoomAvviata> risultati = roomService.visualizzaRisultati(classe, room);

        // Se non ci sono risultati restituisce una lista vuota
        if (risultati == null || risultati.isEmpty()) {
            return Collections.emptyList();
        }

        // Costruisce il json con i risultati
        return risultati.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studente", r.getStudente());
                    map.put("score", r.getScore());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Gestione della richiesta dei laboratori di una classe
    @PostMapping("/professore/laboratori")
    @ResponseBody
    public List<Map<String, Object>> getLaboratori(@RequestParam("classeId") String classe) {

        // Invoca il service per trovare i laboratori di una classe
        List<RoomClasse> laboratori = roomService.trovaLaboratori(classe);

        return laboratori.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("room", r.getRoom());
                    return map;
                })
                .collect(Collectors.toList());

    }

    /* DICHIARAZIONE DEI TIPI DELLE RICHIESTE */
    @Data
    @AllArgsConstructor
    public static class startRoomBody {

        private String nomeClass;
        private String nomeLab;
        private String utente;

    }

    @Data
    @AllArgsConstructor
    public static class stopRoomBody {
        private String utente;

    }

    @Data
    @AllArgsConstructor
    public static class createRoomBody {

        private String nomeLab;
        private MultipartFile yamlFile;

    }

    @Data
    @AllArgsConstructor
    public static class patternRisultato {
        private String studente;
        private String score;
    }

    @Data
    @AllArgsConstructor
    public static class startRoomResponse {
        private String msg;
        private String command;
        private String type;
    }

}