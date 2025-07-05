package com.sad_security.sase.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sad_security.sase.service.UtenteService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {
    // TBD
    // Schermate di gestione dell'utente
    @Autowired
    private UtenteService utenteServices;

    @PostMapping("/check")
    public String controllaLogin(@RequestParam String username, @RequestParam String password, Model Utente, RedirectAttributes redirectAttributes) {

        boolean success = utenteServices.autenticaUtente(username, password);
        if (success) {
            Utente.addAttribute("username", username);
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/dashboard";

        } else {
            Utente.addAttribute("errorMessage", "Username o password errate.");
            return "redirect:/login";
        }

    }

    @PostMapping("/add/user")
    public String registraUtente(@RequestParam String username, @RequestParam String mail,
            @RequestParam String password, Model Utente) {

        boolean exists = utenteServices.aggiungiUtente(username, password, mail);

        // Se l'utente esiste allora procedo verso la pagina di login
        if (exists) {
            Utente.addAttribute("errorMessage", "Username o mail già usate.");
            return "registration";

        } else {
            Utente.addAttribute("successMessage", "Registrazione avvenuta con successo! Accedi.");
            return "login";
        }

    }

    //funzione che permette di cambiare la password
    @PostMapping("/change-password")
    public String CambioPassword(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
     @PostMapping("/visualizza-messaggi")
    public String VisualizzazioneMessaggi(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    



}
