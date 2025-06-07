package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Persona;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class PersonController {
    

    @PostMapping("/addPerson")
    public void addPerson(@RequestBody Persona persona) {
        
        
        return entity;
    }
    
}
