package com.sad_security.sase.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sad_security.sase.service.StudenteService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {
    // TBD
    // Schermate di gestione dello studente
    @Autowired
    private StudenteService studenteServices;


    // CON SPRING SECURITY QUESTA FUNZIONE RISULTA INUTILE DATO CHE LUI FA TUTTI I CONTROLLI ALL'ATTO DELL'INVIO DEL FORM.
    // controlla dati inseriti nel form di login
    // @PostMapping("/check")
    // public String controllaLogin(@RequestParam String username, @RequestParam String password, Model Studente, RedirectAttributes redirectAttributes) {

    //     boolean success = studenteServices.autenticaStudente(username, password);
    //     if (success) {
    //         Studente.addAttribute("username", username);
    //         redirectAttributes.addFlashAttribute("username", username);
    //         return "redirect:/dashboard";

    //     } else {
    //         Studente.addAttribute("errorMessage", "Username o password errate.");
    //         return "redirect:/login";
    //     }

    // }

    // inserisce nuovo studente al database
    @PostMapping("/add/user")
    public String registraStudente(@RequestParam String username, @RequestParam String mail,
            @RequestParam String password, Model Studente) {

        boolean exists = studenteServices.aggiungiStudente(username, password, mail);

        // Se lo studente esiste allora procedo verso la pagina di login
        if (exists) {
            Studente.addAttribute("errorMessage", "Username o mail già usate.");
            return "registration";

        } else {
            Studente.addAttribute("successMessage", "Registrazione avvenuta con successo! Accedi.");
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
