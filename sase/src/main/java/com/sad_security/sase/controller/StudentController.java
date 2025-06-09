package com.sad_security.sase.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sad_security.sase.service.LoginCheck;

@Controller
public class StudentController {
    // TBD
    // Schermate di gestione dell'utente
    @Autowired
    private LoginCheck logincheck;

    @PostMapping("/controlla")
    public String addStudent(@RequestParam String username, @RequestParam String password, Model Utente) {

        boolean success = logincheck.autentica(username, password);
        if (success) {
            return "funziona"; // nome della view (es. dashboard.html o dashboard.jsp)
        } else {
            Utente.addAttribute("error", "Credenziali non valide");
            return "errore"; // torna alla pagina di login
        }

    }

}
