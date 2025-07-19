package com.sad_security.sase.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sad_security.sase.service.ProfessorService;
import com.sad_security.sase.service.StudenteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private StudenteService studenteServices;

    @Autowired
    private ProfessorService professorService;

    // inserisce nuovo studente al database
    @PostMapping("/add/user")
    @ResponseBody
    public Map<String, String> registraStudente(@RequestParam("username") String username,
            @RequestParam("mail") String mail,
            @RequestParam("password") String password) {

        // Controllo se le proposta di credenziali è valida
        Map<String, String> msg = studenteServices.validaRegistrazione(password, mail);

        System.out.println("Risultato di validaRegistrazione" + msg);
        // Se è nullo il check restituisco il messaggio di errore
        if (!msg.isEmpty()) {
            if (msg != null) {
                return msg;
            }
        }

        // Controllo se lo studente è già registrato
        boolean exists = studenteServices.aggiungiStudente(username, password, mail);

        // Rispondo con il messaggio appropriato
        Map<String, String> res = new HashMap<>();

        // Se lo studente esiste allora procedo verso la pagina di login
        if (!exists) {
            res.put("redirect", "/login");
            res.put("msg", "Registrazione avvenuta con successo");
            res.put("type", "success");
        } else {
            res.put("msg", "Errore nella registrazione (utente già esistente?)");
            res.put("type", "error");
        }

        return res;

    }

    // funzione che permette di cambiare la password allo studente
    @PostMapping({ "/studente/change-password", "/professore/change-password" })
    @ResponseBody
    public Map<String, String> CambioPassword(@RequestParam String newPassword, @RequestParam String oldPassword,
            HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> res = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            res.put("msg", "Professore non autenticato");
            res.put("type", "error");
            System.out.println("auth" + res);
            return res;
        }

        // Controllo se la password vecchia è corretta
        boolean isOldCorrect = checkOldPassword(oldPassword, request);

        // Se la password da cambiare è sbagliata allora restituisco errore
        if (!isOldCorrect) {
            res.put("msg", "La vecchia password è sbagliata");
            res.put("type", "error");
            System.out.println("checkold" + res);
            return res;
        }

        // Decido come cambiare la password in base a chi mi ha fatto la richiesta
        String requestPath = request.getRequestURI();

        // Nome dello studente/professore che ha fatto la richiesta
        String username = authentication.getName();

        // Validazione della proposta di cambio della password
        Map<String, String> validazione = studenteServices.validaPassword(newPassword);

        if (validazione != null && !validazione.isEmpty()) {
            res.putAll(validazione);
            return res;
        }

        // Se ci sono errori allora restituisco l'errore
        if (!res.isEmpty()) {
            if (res != null) {
                System.out.println("empty" + res);
                return res;

            }
        }

        boolean changePassowrd;
        // In base a chi ha fatto la richiesta scelgo quale database modificare
        if (requestPath.equals("/account/studente/change-password")) {
            changePassowrd = studenteServices.cambiaPasswordStudente(username, oldPassword, newPassword);
        } else {
            changePassowrd = professorService.cambiaPasswordProfessore(username, oldPassword, newPassword);
        }

        // Restituisco il messaggio in funzione delll'esito dell'operazione di cambio
        // password
        if (changePassowrd) {
            // Effettua il logout manuale dopo il cambio password
            SecurityContextHolder.clearContext();

            // Invalida la sessione per rimuovere i dati di autenticazione
            HttpSession session = request.getSession(false);
            System.out.println("sessione" + session);
            if (session != null) {
                session.invalidate();
            }
            res.put("msg", "credenziali cambiate con successo");
            res.put("type", "success");
        } else {
            res.put("msg", "errore nel cambio delle credenziali");
            res.put("type", "error");
        }

        System.out.println("final" + res);

        return res;
    }

    // Utile per verificare se la password vecchia è corretta
    @GetMapping({ "/professore/checkOldPassword", "/studente/checkOldPassword" })
    @ResponseBody
    public boolean checkOldPassword(@RequestParam String oldPassword, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            response.put("msg", "Utente non autenticato");
            response.put("type", "error");

            return false;
        }
        String username = authentication.getName();

        String requestPath = request.getRequestURI(); // Esempio: "/path1"
        System.out.println(requestPath);
        boolean exists;

        // Logica condizionata
        if (requestPath.equals("/account/studente/change-password")) {
            exists = studenteServices.checkpassowrd(oldPassword, username);

        } else {
            exists = professorService.checkpassowrd(oldPassword, username);
        }

        if (exists) {
            response.put("msg", "credenziali vecchie valide");
            response.put("type", "success");

            return true;
        } else {
            response.put("msg", "password errata");
            response.put("type", "error");

            return false;
        }

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
