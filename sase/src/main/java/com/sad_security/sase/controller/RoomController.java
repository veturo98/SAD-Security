package com.sad_security.sase.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

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

        return roomService.startContainerAsync(Classe,Lab,Utente);
    }


    @PostMapping("/crea")
    public String CreazioneLaboratorio(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
   
    
    @PostMapping("/risultati/pubblica")
    public String PubblicazioneRisultati(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    @PostMapping("/flag")
    public String InserimentoFlag(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    @PostMapping("/risultati/visualizza")
    public String VisualizzazioneRisultati(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    // Dichiaro la classe che contiene il corpo della room
    @Data
    @AllArgsConstructor 
    public static class startRoomBody{
        
        private String nomeClass;
        private String nomeLab;
        private String utente;
        
    }
}