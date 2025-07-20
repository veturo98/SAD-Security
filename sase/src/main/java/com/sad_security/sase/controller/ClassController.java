package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Iscrizione;
import com.sad_security.sase.service.ClassService;
import com.sad_security.sase.service.IscrizioneService;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;


// REST CONTROLLER PER LE RICHIESTE DI MANAGEMENT DELLA CLASSE
@RestController
@RequestMapping("/classe")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private IscrizioneService iscrizioneService;

    // Gestione della richiesta della lista delle classi
    @GetMapping({"/professore/getClassi", "/studente/getClassi"})
    @ResponseBody
    public List<String> getClassi() {

        // Chiama il servizio che si occupa di restituire la classi
        List<Classe> classi = classService.trovaTutteLeClassi();

        // Costruisce la risposta come json
        return classi.stream()
                .map(Classe::getNome)
                .collect(Collectors.toList());
    }

    // Gestione della richiesta degli studenti iscritti ad una classe
    @PostMapping("/professore/listaIscritti")
    @ResponseBody
    public List<Map<String, Object>> getStudenti(@RequestParam("classeId") String classe) {

        // Chiama il servizio che si occupa di restituire la lista degli studenti iscritti
        List<Iscrizione> iscrizioni = iscrizioneService.trovaStudenti(classe);

        // Costruisce la risposta come json
        return iscrizioni.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studente", r.getStudente());
                    return map;
                })
                .collect(Collectors.toList());

    }

    // Gestione della richiesta di creazione di una classe
    @PostMapping("/professore/crea")
    @ResponseBody
    public Map<String, String> creaClasse(@RequestParam String classe) {

        // Chiama il service che si occupa di aggiungere la classe (risponde true se la crea)
        boolean exists = classService.aggiungiClasse(classe);

        // Costruisce il messaggio di risposta in base all'esito del service
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

    // Gestione della richiesta di iscrizione ad una classe
    @PostMapping("/studente/iscriviti")
    @ResponseBody
    public Map<String, String> IscrizioneClasse(@RequestParam("nomeClasse") String nomeClasse) {

        // Controlla se l'utente è autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> response = new HashMap<>();

        // Se l'utente non è autenticato restituisce errore
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            response.put("message", "Professore non autenticato");
            response.put("type", "error");
            return response;
        }


        // Verifica se l'utente è gia iscritto a quella classe
        String studentName = authentication.getName();
        boolean iscritto = iscrizioneService.controllaIscrizione(studentName, nomeClasse);

        // Se già è iscritto restituisce errore
        if (iscritto) {
            response.put("message", "L'utente è già iscritto alla classe ");
            response.put("type", "error");
            return response;
        }

        // Altrimenti risponde con un messaggio di conferma
        iscrizioneService.aggiungiIscrizione(studentName, nomeClasse);
        response.put("message", "L'utente si è iscritto alla classe ");
        response.put("type", "success");
        return response;

    }

    // Gestione della richiesta di conoscere le classi a cui è iscritto uno studente
    @GetMapping("/studente/getClassiIscritte")
    @ResponseBody
    public List<String> getClassiIscrittePerStudente(Authentication authentication) {
        

        // Se lo studente non è autenticato allora restituisce vuoto
        String username = authentication.getName();

        if (username.isEmpty()) {
            return Collections.emptyList();
        }

        // Altrimenti invoca il service che cerca le classi 
        return iscrizioneService.getNomiClassiIscritte(username);
    }


    // Dichiarazione del tipo di body della richiesta di creazione classe
    @Data
    @AllArgsConstructor
    public static class creaClasseBody {
        private String nomeClasse;

    }

}
