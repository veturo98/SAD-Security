package com.sad_security.sase.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.StudenteRepository;


@Service("studenteDetailsService")
public class StudenteService implements UserDetailsService {

    @Autowired
    private StudenteRepository studenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Funzioni di Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Studente> optionalStudente = studenteRepository.findByUsername(username);
        if (optionalStudente.isEmpty()) {
            throw new UsernameNotFoundException("Studente non trovato");
        }

        Studente studente = optionalStudente.get();

        return User.withUsername(studente.getUsername())
                .password(studente.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENTE")))
                .build();
    }


    // Restituisce lo studente dato lo username
    public Optional<Studente> findByUsername(String username) {
        return studenteRepository.findByUsername(username);
    }

    // Funzione di Spring Security
    public boolean autenticaStudente(String username, String password) {

        Studente studente = studenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome studente non trovato"));

        // Verifica la vecchia password
        if (!passwordEncoder.matches(password, studente.getPassword())) {
            return false; // password errata
        }
        return true;
    }

    // Registrazione di un nuovo studente
    public boolean aggiungiStudente(String username, String password, String mail) {

        // Cerco se l'studente esiste già (mail o username già utilizzato)
        Optional<Studente> userName = studenteRepository.findByUsername(username);
        Optional<Studente> userMail = studenteRepository.findByMail(mail);

        // Se l'studente esiste allora dico che già esiste
        if (userName.isPresent() || userMail.isPresent()) {
            System.out.println("lo studente esiste già");
            return true;
        } else {

            // Creazione studente con credenziali inserite
            Studente newStudente = new Studente();

            // creo l'hash della password prima di salvare nel database
            String encodedPassword = passwordEncoder.encode(password);

            newStudente.setUsername(username);
            newStudente.setMail(mail);
            newStudente.setPassword(encodedPassword);
            // newStudente.setRoles(List.of("STUDENTE"));

            studenteRepository.save(newStudente);
            System.out.println("studente creato");

            return false;
        }

    }

    // FUNZIONI DI VALIDAZIONE
    public Map<String, String> validaRegistrazione(String password, String mail) {

        // Esito della validazione
        Map<String, String> result = new HashMap<>();

        // Controllo la password e nel caso esco
        Map<String, String> validazione = validaPassword(password);
        if (!validazione.isEmpty()) {
            result = validazione;
            return result;
        }

        // Controllo la mail - Pattern: qualcosa@qualcosa.dominio
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        if (!mail.matches(regex)) {
            result.put("msg", "L'email non è valida.");
            result.put("type", "error");

            return result;
        }

        // Nessun errore
        return Collections.emptyMap();

    }

    // Validazione della password
    public Map<String, String> validaPassword(String password) {

        Map<String, String> result = new HashMap<>();

        // Vuota
        if (password == null || password.isEmpty()) {
            result.put("msg", "La password non può essere vuota.");
            result.put("type", "error");

            return result;
        }

        // Troppo corta
        if (password.length() < 8) {
            result.put("msg", "La password deve contenere almeno 8 caratteri.");
            result.put("type", "error");

            return result;
        }

        // Senza maiuscole
        if (!password.matches(".*[A-Z].*")) {
            result.put("msg", "La password deve contenere almeno una lettera maiuscola.");
            result.put("type", "error");

            return result;
        }

        // Senza numeri
        if (!password.matches(".*\\d.*")) {
            result.put("msg", "La password deve contenere almeno un numero.");
            result.put("type", "error");

            return result;
        }

        // Senza caratteri speciali
        if (!password.matches(".*[!@#$%^&*()\\-+=\\[\\]{};:,.<>/?].*")) {
            result.put("msg", "La password deve contenere almeno un carattere speciale.");
            result.put("type", "error");

            return result;
        }

        // Contiene spazi
        if (password.matches(".*\\s.*")) {
            result.put("msg", "La password non può contenere spazi.");
            result.put("type", "error");

            return result;
        }

        // Nessun errore
        return Collections.emptyMap();

    }

    // Controlla che esiste la password nel database
    public boolean checkpassowrd(String password, String username) {

        Studente studente = studenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome Studente non trovato"));

        // Verifica la vecchia password
        if (!passwordEncoder.matches(password, studente.getPassword())) {
            return false; // password errata
        }
        return true;
    }

    // Cambia la password per lo studente
    public boolean cambiaPasswordStudente(String username, String oldpassword, String newpassword) {

        Optional<Studente> optional = studenteRepository.findByUsername(username);

        // Controllo se la vecchia password è già usata
        Boolean controllocredenziali = checkpassowrd(oldpassword, username);

        // Se l'utente esiste e la vecchia password è corretta
        if (optional.isPresent() && controllocredenziali) {

            // Costruisce studente e password e le salva
            Studente studente = optional.get();
            studente.setPassword(passwordEncoder.encode(newpassword));
            
            studenteRepository.save(studente);

            return true;
        }
        return false;
    }

}
