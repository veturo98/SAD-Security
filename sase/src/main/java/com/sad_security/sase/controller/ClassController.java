package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Iscrizione;
import com.sad_security.sase.service.ClassService;
import com.sad_security.sase.service.IscrizioneService;

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

/**
 * REST controller per la gestione delle richieste relative alle classi.
 */
@RestController
@RequestMapping("/classe")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private IscrizioneService iscrizioneService;

    /**
     * Gestione della richiesta della lista delle classi.
     *
     * @return una lista con i nomi delle classi
     */
    @GetMapping({ "/professore/getClassi", "/studente/getClassi" })
    @ResponseBody
    public Map<String, Object> getClassi() {

        // Ottiene tutte le classi disponibili
        List<Classe> classi = classService.trovaTutteLeClassi();

        // Recupera la lista dei nomi
        List<String> nomiClassi = classi.stream().map(Classe::getNome).collect(Collectors.toList());

        // Costruisce la risposta in JSON
        Map<String, Object> response = new HashMap<>();
        response.put("message", "");
        response.put("type", "success");
        response.put("data", nomiClassi);

        return response;
    }

    /**
     * Gestione della richiesta degli studenti iscritti ad una classe.
     *
     * @param classe ID della classe
     * @return lista di studenti iscritti
     */
    @PostMapping("/professore/listaIscritti")
    @ResponseBody
    public Map<String, Object> getStudenti(@RequestParam("classeId") String classe) {

        /**
         * Chiama il servizio che si occupa di restituire la lista degli studenti
         * iscritti.
         */
        List<Iscrizione> iscrizioni = iscrizioneService.trovaStudenti(classe);

        /**
         * Costruisce la risposta come JSON.
         */
        List<Map<String, Object>> studenti_iscritti = iscrizioni.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studente", r.getStudente());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "");
        response.put("type", "success");
        response.put("data", studenti_iscritti);

        return response;
    }

    /**
     * Gestione della richiesta di creazione di una classe.
     *
     * @param classe nome della classe da creare
     * @return mappa con messaggio e tipo (success/error)
     */
    @PostMapping("/professore/crea")
    @ResponseBody
    public Map<String, Object> creaClasse(@RequestParam String classe) {

        /**
         * Chiama il service che si occupa di aggiungere la classe
         * (risponde true se la crea).
         */
        boolean exists = classService.aggiungiClasse(classe);

        /**
         * Costruisce il messaggio di risposta in base all'esito del service.
         */
        Map<String, Object> response = new HashMap<>();

        if (exists) {
            response.put("message", "La classe esiste già");
            response.put("type", "error");
        } else {
            response.put("message", "La classe creata con successo.");
            response.put("type", "success");
        }

        return response;
    }

    /**
     * Gestione della richiesta di iscrizione ad una classe.
     *
     * @param nomeClasse nome della classe a cui iscriversi
     * @return mappa con messaggio e tipo (success/error)
     */
    @PostMapping("/studente/iscriviti")
    @ResponseBody
    public Map<String, Object> IscrizioneClasse(@RequestParam("nomeClasse") String nomeClasse) {

        /**
         * Controlla se l'utente è autenticato.
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        /**
         * Se l'utente non è autenticato restituisce errore.
         */
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            response.put("message", "Professore non autenticato");
            response.put("type", "error");
            return response;
        }

        /**
         * Verifica se l'utente è già iscritto a quella classe.
         */
        String studentName = authentication.getName();
        boolean iscritto = iscrizioneService.controllaIscrizione(studentName, nomeClasse);

        /**
         * Se già iscritto, restituisce errore.
         */
        if (iscritto) {
            response.put("message", "L'utente è già iscritto alla classe ");
            response.put("type", "error");
            return response;
        }

        /**
         * Altrimenti risponde con un messaggio di conferma.
         */
        iscrizioneService.aggiungiIscrizione(studentName, nomeClasse);
        response.put("message", "L'utente si è iscritto alla classe ");
        response.put("type", "success");
        return response;
    }

    /**
     * Gestione della richiesta per ottenere le classi a cui è iscritto uno
     * studente.
     *
     * @param authentication oggetto di autenticazione
     * @return lista dei nomi delle classi a cui lo studente è iscritto
     */
    @GetMapping("/studente/getClassiIscritte")
    @ResponseBody
    public Map<String, Object> getClassiIscrittePerStudente(Authentication authentication) {

        /**
         * Se lo studente non è autenticato allora restituisce lista vuota.
         */
        String username = authentication.getName();

        Map<String, Object> response = new HashMap<>();

        if (username.isEmpty()) {
            response.put("message", "Utente non autenticato");
            response.put("type", "error");
            response.put("data", Collections.emptyList());
            return response;
        }

        /**
         * Altrimenti invoca il service che cerca le classi.
         */
        List<String> iscrizioni_utente = iscrizioneService.getNomiClassiIscritte(username);

        response.put("message", "");
        response.put("type", "success");
        response.put("data", iscrizioni_utente);
        
        return response;
    }

}
