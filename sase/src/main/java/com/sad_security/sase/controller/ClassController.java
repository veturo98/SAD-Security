package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class ClassController {
    
     
    @PostMapping("/crea-classe")
    public String CreazioneClasse(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
    
    @PostMapping("/iscriviti")
    public String IscrizioneClasse(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    @PostMapping("/conferma-richiesta")
    public String ConfermaRichiesta(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
    @PostMapping("/aggiungi-studente")
    public String AggiuntaStudene(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    


}
