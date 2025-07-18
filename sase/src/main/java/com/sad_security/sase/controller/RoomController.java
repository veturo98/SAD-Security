package com.sad_security.sase.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Controller per la room: non restituisce pagine
@RestController
@RequestMapping("/room")
public class RoomController {

    // Dichiaro i service
    @Autowired
    private RoomService roomService;

    // Mappo la chiamata per l'avvio delle room
    @PostMapping("/studente/start")
    public CompletableFuture<String> startRoom(@RequestBody startRoomBody startRoom) {

        // RICOMINCIA DA QUA DEVI CERCA IL TIMESTAMP E QUA DEVI ANDA A METTE QUELLO
        // SCHIFO DI CHIAMATE AL DATABASE PER SALVARE L'ASSOCIAZIONE CHE PRIMA NON
        // TENEVAMO, PER ORA MANDA SOLO LE RICHIESTE AL SERVER PYTHON

        // Formatto i campi pre l'invio della richiesta
        String Classe = startRoom.getNomeClass();
        String Lab = startRoom.getNomeLab();
        String Utente = startRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + startRoom);

        return roomService.startContainerAsync(Classe, Lab, Utente);
    }

    // Mappo la chiamata per l'avvio delle room
    @PostMapping("/studente/stop")
    public CompletableFuture<String> stopRoom(@RequestBody stopRoomBody stopRoom) {

        // Formatto i campi pre l'invio della richiesta

        String Utente = stopRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + stopRoom);

        return roomService.stopContainer(Utente);
    }

    // Il professore crea un nuovo laboratorio
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

            // Leggi il contenuto YAML (opzionale, a seconda di cosa ti serve)
            String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Contenuto YAML:\n" + yamlContent);

            // Salva la room nel database
            roomService.salvaNuovaRoom(roomName, descrizione, flag);

            // salvare l'associazione
            boolean associazione = roomService.aggiungiassociazione(classeNome, roomName);
            if (associazione) {
                response.put("message", "L'associazione tra questa classe e room è già stata fatta ");
                response.put("type", "error");
                return response;
            }

            // service per inviare dati al backend
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

    @GetMapping({ "/professore/checkroom", "/studente/checkroom" })
    @ResponseBody
    public Map<String, String> checkroomRoomName(@RequestParam String roomName) {

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

    @GetMapping("/studente/getDescrizioneRoom")
    public ResponseEntity<String> getDescrizioneRoom(@RequestParam String nomeRoom) {
        String descrizione = roomService.getDescrizione(nomeRoom);
        return ResponseEntity.ok(descrizione);
    }

    @GetMapping("/getRoomsPerClasse")
    public ResponseEntity<List<String>> getRoomsPerClasse(@RequestParam String nomeClasse) {
        List<String> rooms = roomService.getRoomListbyClasse(nomeClasse);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/professore/risultati/pubblica")
    public String PubblicazioneRisultati(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/studente/flag")
    public String InserimentoFlag(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping({ "/professore/risultati/visualizza", "/studente/risultati/visualizza" })
    public String VisualizzazioneRisultati(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    // Dichiaro la classe che contiene il corpo della richiesta di avvio room
    @Data
    @AllArgsConstructor
    public static class startRoomBody {

        private String nomeClass;
        private String nomeLab;
        private String utente;

    }

    // Dichiaro la classe che contiene il corpo della richiesta di stop della room
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

}