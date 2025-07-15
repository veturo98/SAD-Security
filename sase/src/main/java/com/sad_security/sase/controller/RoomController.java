package com.sad_security.sase.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Controller per la room: non restituisce pagine
@RestController
@RequestMapping("/room")
public class RoomController {

    // Dichiaro i service
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Mappo la chiamata per l'avvio delle room
    @PostMapping("/start")
    public CompletableFuture<String> startRoom(@RequestBody startRoomBody startRoom) {

        // Formatto i campi pre l'invio della richiesta
        String Classe = startRoom.getNomeClass();
        String Lab = startRoom.getNomeLab();
        String Utente = startRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + startRoom);

        return roomService.startContainerAsync(Classe, Lab, Utente);
    }

    // Mappo la chiamata per l'avvio delle room
    @PostMapping("/stop")
    public CompletableFuture<String> stopRoom(@RequestBody stopRoomBody stopRoom) {

        // Formatto i campi pre l'invio della richiesta

        String Utente = stopRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + stopRoom);

        return roomService.stopContainer(Utente);
    }

    // Il professore crea un nuovo laboratorio
    @PostMapping("/creaLaboratorio")
    @ResponseBody
public Map<String, String> creaLaboratorio(
        @RequestParam("classeId") String classeNome,
        @RequestParam("room") String roomName,
        @RequestParam("yamlFile") MultipartFile yamlFile) {

    Map<String, String> response = new HashMap<>();

    try {
            // Verifica che il file non sia vuoto e sia YAML
        if (yamlFile.isEmpty() || 
            (!yamlFile.getOriginalFilename().endsWith(".yaml") && 
             !yamlFile.getOriginalFilename().endsWith(".yml"))) {
            response.put("message", "File non valido. Deve essere un file .yaml o .yml");
            response.put("type", "error");
            return response;
        }

        // Leggi il contenuto YAML (opzionale, a seconda di cosa ti serve)
        String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
        System.out.println("Contenuto YAML:\n" + yamlContent);

        // Salva la room (dovrai adattare alla tua logica)
        roomService.aggiungiRoom(roomName);

        //service per inviare dati al backend
        roomService.createRoom(classeNome, roomName, yamlFile);

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

    // utile per ottenere tutte le room presenti nel DB
    @GetMapping("/checkroom")
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

    @PostMapping("/risultati/pubblica")
    public String PubblicazioneRisultati(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/utente/flag")
    public String InserimentoFlag(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/risultati/visualizza")
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

        private String nomeClass;
        private String nomeLab;
        private MultipartFile yamlFile;

    }

}