package com.sad_security.sase.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.Data;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/start-room")
    public CompletableFuture<String> startRoom(@RequestBody startRoomBody startRoom) {

        String Classe = startRoom.getNomeClass();
        String Lab = startRoom.getNomeLab();
        String Utente = startRoom.getUtente();
        System.out.println("Sono il controller ed ho ricevuto questo"+startRoom);
        return roomService.startContainerAsync(Classe,Lab,Utente);
    }




    @Data
    @AllArgsConstructor 
    public static class startRoomBody{
        
        private String nomeClass;
        private String nomeLab;
        private String utente;
        
    }
}