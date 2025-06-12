package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoomController {


    @PostMapping("/start-room")
    public String getRoom(@RequestParam String user, @RequestParam String room_type) {
        
        // Start della room con il service

        return new String();
    }

}
