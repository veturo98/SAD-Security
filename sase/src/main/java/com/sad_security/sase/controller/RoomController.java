package com.sad_security.sase.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.RoomService;

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
        return roomService.startContainerAsync(Classe,Lab);
    }




    @Data
    public static class startRoomBody{
        
        private String nomeClass;
        private String nomeLab;
        
    }
}