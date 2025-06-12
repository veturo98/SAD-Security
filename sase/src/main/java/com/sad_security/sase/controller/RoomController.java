package com.sad_security.sase.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.RoomService;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/start-room")
    public CompletableFuture<String> startRoom(@RequestBody String nomeLab) {
        return roomService.startContainerAsync(nomeLab);
    }
}