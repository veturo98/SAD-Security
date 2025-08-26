package com.sad_security.sase.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.service.ProfessorService;
import com.sad_security.sase.service.StudenteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller per le richieste di account management.
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private StudenteService studenteServices;

    @Autowired
    private ProfessorService professorService;

    /**
     * Gestisce la richiesta di registrazione di nuovi utenti.
     *
     * @param username nome utente da registrare
     * @param mail     email dell'utente
     * @param password password scelta dall'utente
     * @return una mappa con il messaggio di esito
     */
    @PostMapping("/registrati")
    @ResponseBody
    public Map<String, String> registraStudente(@RequestParam("username") String username,
            @RequestParam("mail") String mail,
            @RequestParam("password") String password) {

        /**
         * Controlla se la proposta di credenziali è valida.
         */
        Map<String, String> msg = studenteServices.validaRegistrazione(password, mail);
        System.out.println("Risultato di validaRegistrazione " + msg);

        /**
         * Se è nullo il check restituisce il messaggio di errore.
         */
        if (!msg.isEmpty()) {
            if (msg != null) {
                return msg;
            }
        }

        /**
         * Controlla se lo studente è già registrato.
         */
        boolean exists = studenteServices.aggiungiStudente(username, password, mail);

        /**
         * Risponde con il messaggio appropriato.
         */
        Map<String, String> res = new HashMap<>();

        /**
         * Se lo studente esiste allora procede verso la pagina di login.
         */
        if (!exists) {
            res.put("redirect", "/login");
            res.put("msg", "Registrazione avvenuta con successo");
            res.put("type", "success");
        } else {
            res.put("msg", "Errore nella registrazione. Username o mail già utilizzate.");
            res.put("type", "error");
        }

        return res;
    }

    /**
     * Gestione delle richieste di cambio della password.
     *
     * @param newPassword nuova password scelta
     * @param oldPassword password attuale
     * @param request     oggetto HTTP request
     * @param response    oggetto HTTP response
     * @return una mappa con il risultato del cambio password
     */
    @PostMapping({ "/studente/changePassword", "/professore/changePassword" })
    @ResponseBody
    public Map<String, String> cambioPassword(@RequestParam String newPassword, @RequestParam String oldPassword,
            HttpServletRequest request, HttpServletResponse response) {

        /**
         * Controlla se l'utente è autenticato.
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, String> res = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            res.put("msg", "Professore non autenticato");
            res.put("type", "error");
            System.out.println("auth" + res);
            return res;
        }

        /**
         * Decide come cambiare la password in base a chi ha fatto la richiesta.
         */
        String requestPath = request.getRequestURI();

        /**
         * Nome dello studente/professore che ha fatto la richiesta.
         */
        String username = authentication.getName();

        /**
         * Validazione della proposta di cambio della password.
         */
        Map<String, String> validazione = studenteServices.validaPassword(newPassword);
        System.out.println("ho validato la password" + res);

        if (validazione != null && !validazione.isEmpty()) {
            res.putAll(validazione);
            System.out.println("validazione " + res);
            return res;
        }

        /**
         * Se ci sono errori allora restituisce l'errore.
         */
        if (!res.isEmpty()) {
            if (res != null) {
                System.out.println("empty" + res);
                res.put("msg", "Errore di autenticazione");
                res.put("type", "error");
                return res;
            }
        }

        boolean changePassowrd;

        /**
         * If dello studente.
         */
        if (requestPath.equals("/account/studente/changePassword")) {

            /**
             * Controlla se la password vecchia è corretta.
             */
            boolean isOldCorrect = studenteServices.checkpassowrd(oldPassword, username);
            if (!isOldCorrect) {
                System.out.println("password vecchia rotta " + res);
                res.put("msg", "La vecchia password è sbagliata");
                res.put("type", "error");
                return res;
            }

            /**
             * Altrimenti effettua il cambio persistente.
             */
            changePassowrd = studenteServices.cambiaPasswordStudente(username, oldPassword, newPassword);

        } else { // Else del professore

            /**
             * Controlla se la password vecchia è corretta.
             */
            boolean isOldCorrect = professorService.checkpassowrd(oldPassword, username);
            if (!isOldCorrect) {
                System.out.println("password vecchia rotta " + res);
                res.put("msg", "La vecchia password è sbagliata");
                res.put("type", "error");
                return res;
            }

            /**
             * Altrimenti effettua il cambio persistente.
             */
            changePassowrd = professorService.cambiaPasswordProfessore(username, oldPassword, newPassword);
        }

        /**
         * Restituisce il messaggio in funzione dell'esito dell'operazione di cambio
         * password.
         */
        if (changePassowrd) {
            /**
             * Effettua il logout manuale dopo il cambio password.
             */
            SecurityContextHolder.clearContext();

            /**
             * Invalida la sessione per rimuovere i dati di autenticazione.
             */
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

    /**
     * Gestione della richiesta di logout.
     *
     * @param request  la richiesta HTTP
     * @return redirect alla pagina di login
     */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        // Pulisce il contesto di sicurezza
        SecurityContextHolder.clearContext();

        // Invalida la sessione
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Risposta JSON
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("redirectUrl", "/login");

        return response;
    }
}
