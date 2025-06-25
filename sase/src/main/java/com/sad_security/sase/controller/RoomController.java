package com.sad_security.sase.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

// Controller per la room: non restituisce pagine
@RestController
public class RoomController {

    // Dichiaro i service
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Mappo la chiamata per l'avvio delle room
    @PostMapping("/start-room")
    public CompletableFuture<String> startRoom(@RequestBody startRoomBody startRoom) {

        // Formatto i campi pre l'invio della richiesta
        String Classe = startRoom.getNomeClass();
        String Lab = startRoom.getNomeLab();
        String Utente = startRoom.getUtente();

        System.out.println("Sono il controller ed ho ricevuto questo" + startRoom);

        return roomService.startContainerAsync(Classe,Lab,Utente);
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