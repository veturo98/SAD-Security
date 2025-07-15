package com.sad_security.sase.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sad_security.sase.repository.ProfessoreRepository;
import com.sad_security.sase.service.ProfessorService;
import com.sad_security.sase.service.StudenteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {
    // TBD
    // Schermate di gestione dello studente
    @Autowired
    private StudenteService studenteServices;

    @Autowired
    private ProfessorService professorService;


    // CON SPRING SECURITY QUESTA FUNZIONE RISULTA INUTILE DATO CHE LUI FA TUTTI I CONTROLLI ALL'ATTO DELL'INVIO DEL FORM.
    // POTEBBE TORNARE UTILE CON IL CAMBIO PASSWORD 
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
    // @PostMapping("/change-password")
    // public String CambioPassword(@RequestBody String entity) {
        
    //     Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        
    //     return entity;
    // }

     //funzione che permette di cambiare la password al professore
    @PostMapping("/professore/change-password")
    @ResponseBody
    public Map<String, String> CambioProfessorePassword(@RequestParam String newPassword, @RequestParam String oldPassword, HttpServletRequest request, HttpServletResponse response) {
     
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> res = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
        authentication instanceof AnonymousAuthenticationToken) {
        res.put("message", "Professore non autenticato");
        res.put("type", "error");
        return res;
    }

    // nome del professore che ha fatto la richiesta
    String username = authentication.getName();
    boolean changePassowrd = professorService.cambiaPasswordProfessore(username,oldPassword ,newPassword);

    if (changePassowrd) {
        // Effettua il logout manuale dopo il cambio password
        SecurityContextHolder.clearContext();

        // Invalida la sessione per rimuovere i dati di autenticazione
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        res.put("message", "credenziali cambiate con successo");
        res.put("type", "success");
    } else {
        res.put("message", "errore nel cambio delle credenziali");
        res.put("type", "error");
    }


        return res;
    }
    
    // Utile per verificare se la password vecchia è corretta
    @GetMapping("/professore/checkOldPassword")
    @ResponseBody
    public Map<String, String> checkOldPassword(@RequestParam String oldPassword) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
        authentication instanceof AnonymousAuthenticationToken) {
        response.put("message", "Utente non autenticato");
        response.put("type", "error");
        return response;
    }
        String username = authentication.getName();

        boolean exists = professorService.checkpassowrd(oldPassword, username);

        
        
        if (exists) {
            response.put("message", "credenziali vecchie valide");
            response.put("type", "success");
        } else {
            response.put("message", "password errata");
            response.put("type", "error");
        }

        return response;
    }


    
     @PostMapping("/visualizza-messaggi")
    public String VisualizzazioneMessaggi(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
    @PostMapping("/logout")
    public String VisualizzazioneMessaggi(HttpServletRequest request, HttpServletResponse response) {
        // Effettua il logout manuale dopo il cambio password
        SecurityContextHolder.clearContext();

        // Invalida la sessione per rimuovere i dati di autenticazione
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        return "redirect:/login";
    }
    
   

}
