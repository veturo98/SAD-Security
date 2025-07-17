package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Studente;
import com.sad_security.sase.service.ClassService;
import com.sad_security.sase.service.IscrizioneService;
import com.sad_security.sase.service.StudenteService;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/classe")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private IscrizioneService iscrizioneService;
    
    @Autowired
    private StudenteService studenteService;
   

    // utile per ottenere tutte le classi presenti nel DB
    @GetMapping("/getClassi")
    @ResponseBody
    public List<String> getClassi() {
        List<Classe> classi = classService.trovaTutteLeClassi();
        return classi.stream()
                .map(Classe::getNome)
                .collect(Collectors.toList());
    }

    // In questo caso uso map perché devo solamente restituire un messaggio di
    // presenza o assenza della classe nel database
    @PostMapping("/crea")
    @ResponseBody
    public Map<String, String> creaClasse(@RequestParam String classe) {
        boolean exists = classService.aggiungiClasse(classe);
        Map<String, String> response = new HashMap<>();

        if (exists) {
            response.put("message", "La classe esiste già");
            response.put("type", "error");
        } else {
            response.put("message", "La classe creata con successo.");
            response.put("type", "success");
        }

        return response;
    }

    @PostMapping("/iscriviti")
    @ResponseBody
    public Map<String, String> IscrizioneClasse(@RequestParam("nomeClasse") String nomeClasse ) {
        
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        
       
        Map<String, String> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
        authentication instanceof AnonymousAuthenticationToken) {
        response.put("message", "Professore non autenticato");
        response.put("type", "error");
        return response;
        }

        // ottengo l'oggetto classe e studente da passare al service
        Optional <Classe> cl = classService.cercaClasse(nomeClasse);

        // ottengo l'oggetto studente
        String studentName = authentication.getName();
        Optional<Studente> studenteOptional = studenteService.findByUsername(studentName);
      
        if (studenteOptional.isEmpty() || cl.isEmpty()) {
        response.put("message", "Studente o classe non trovati");
        response.put("type", "error");
        return response;
    }
        

        // Verifica se l'utente è gia iscritto a quella classe
        boolean iscritto = iscrizioneService.controllaIscrizione(studenteOptional, cl); 

        if(iscritto){
            response.put("message", "L'utente è già iscritto alla classe ");
            response.put("type", "error");
            return response;
        }

        iscrizioneService.aggiungiIscrizione(studenteOptional, cl);
        response.put("message", "L'utente si è iscritto alla classe ");
        response.put("type", "success");
        return response;
    
    }


    @GetMapping("/getClassiIscritte")
@ResponseBody
public List<String> getClassiIscrittePerStudente(Authentication authentication) {
    String username = authentication.getName();
    Optional<Studente> studente = studenteService.findByUsername(username);

    if (studente.isEmpty()) {
        return Collections.emptyList();
    }

    // Ottieni lista di nomi classi a cui è iscritto lo studente
    return iscrizioneService.getNomiClassiIscritte(studente.get());
}


    @PostMapping("/conferma-richiesta")
    public String ConfermaRichiesta(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/aggiungi-studente")
    public String AggiuntaStudene(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    // Dichiaro la classe che contiene il corpo della richiesta di stop della room
    @Data
    @AllArgsConstructor
    public static class creaClasseBody {
        private String nomeClasse;

    }

}
