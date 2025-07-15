package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Classe;

import com.sad_security.sase.service.ClassService;

import org.springframework.ui.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String IscrizioneClasse(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
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
