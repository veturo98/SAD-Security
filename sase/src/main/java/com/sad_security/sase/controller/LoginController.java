package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Persona;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class LoginController {
    

    // TBD completare l'aggiunta di un nuovo utente
    @PostMapping("/home")
    public void login(@RequestBody Persona persona) {
        
        
        return persona;
    }
    
}
