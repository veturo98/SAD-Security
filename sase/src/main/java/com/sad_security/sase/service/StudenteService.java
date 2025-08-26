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

    /**
     * Carica un utente in base allo username per Spring Security.
     * 
     * @param username lo username dello studente
     * @return UserDetails contenente username, password e ruoli
     * @throws UsernameNotFoundException se lo studente non è presente nel database
     */
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

    /**
     * Cerca uno studente tramite username.
     * 
     * @param username lo username dello studente
     * @return Optional contenente lo studente se trovato
     */
    public Optional<Studente> findByUsername(String username) {
        return studenteRepository.findByUsername(username);
    }

    /**
     * Autentica uno studente verificando username e password.
     * 
     * @param username lo username dello studente
     * @param password la password in chiaro da verificare
     * @return true se le credenziali sono corrette, false altrimenti
     * @throws UsernameNotFoundException se lo studente non esiste
     */
    public boolean autenticaStudente(String username, String password) {
        Studente studente = studenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome studente non trovato"));

        return passwordEncoder.matches(password, studente.getPassword());
    }

    /**
     * Registra un nuovo studente con username, password e mail.
     * 
     * @param username lo username desiderato
     * @param password la password in chiaro
     * @param mail     l'indirizzo email dello studente
     * @return true se lo studente esiste già, false se la registrazione è andata a
     *         buon fine
     */
    public boolean aggiungiStudente(String username, String password, String mail) {
        Optional<Studente> userName = studenteRepository.findByUsername(username);
        Optional<Studente> userMail = studenteRepository.findByMail(mail);

        if (userName.isPresent() || userMail.isPresent()) {
            System.out.println("lo studente esiste già");
            return true;
        } else {
            Studente newStudente = new Studente();
            String encodedPassword = passwordEncoder.encode(password);

            newStudente.setUsername(username);
            newStudente.setMail(mail);
            newStudente.setPassword(encodedPassword);

            studenteRepository.save(newStudente);
            System.out.println("studente creato");

            return false;
        }
    }

    /**
     * Valida i parametri di registrazione: password e mail.
     * 
     * @param password la password da validare
     * @param mail     l'indirizzo email da validare
     * @return mappa con messaggi di errore, o vuota se nessun errore
     */
    public Map<String, Object> validaRegistrazione(String password, String mail) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> validazione = validaPassword(password);
        if (!validazione.isEmpty()) {
            result = validazione;
            return result;
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        if (!mail.matches(regex)) {
            result.put("message", "L'email non è valida.");
            result.put("type", "error");
            result.put("data", "");
            return result;
        }

        return Collections.emptyMap();
    }

    /**
     * Valida la password secondo criteri di sicurezza.
     * 
     * @param password la password da validare
     * @return mappa con messaggi di errore, o vuota se nessun errore
     */
    public Map<String, Object> validaPassword(String password) {
        Map<String, Object> result = new HashMap<>();

        if (password == null || password.isEmpty()) {
            result.put("message", "La password non può essere vuota.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        if (password.length() < 8) {
            result.put("message", "La password deve contenere almeno 8 caratteri.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        if (!password.matches(".*[A-Z].*")) {
            result.put("message", "La password deve contenere almeno una lettera maiuscola.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        if (!password.matches(".*\\d.*")) {
            result.put("message", "La password deve contenere almeno un numero.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        if (!password.matches(".*[!@#$%^&*()\\-+=\\[\\]{};:,.<>/?].*")) {
            result.put("message", "La password deve contenere almeno un carattere speciale.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        if (password.matches(".*\\s.*")) {
            result.put("message", "La password non può contenere spazi.");
            result.put("type", "error");
            result.put("data", "");

            return result;
        }

        result.put("message", "La password proposta è valida");
        result.put("type", "success");
        result.put("data", "");

        return result;
    }

    /**
     * Verifica che la password corrisponda a quella salvata per lo username dato.
     * 
     * @param password la password da controllare (in chiaro)
     * @param username lo username dello studente
     * @return true se la password è corretta, false altrimenti
     * @throws UsernameNotFoundException se lo studente non esiste
     */
    public boolean checkpassowrd(String password, String username) {
        Studente studente = studenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome Studente non trovato"));

        return passwordEncoder.matches(password, studente.getPassword());
    }

    /**
     * Cambia la password dello studente se la vecchia password è corretta.
     * 
     * @param username    lo username dello studente
     * @param oldpassword la vecchia password (in chiaro)
     * @param newpassword la nuova password da impostare (in chiaro)
     * @return true se il cambio password è andato a buon fine, false altrimenti
     */
    public boolean cambiaPasswordStudente(String username, String oldpassword, String newpassword) {
        Optional<Studente> optional = studenteRepository.findByUsername(username);
        Boolean controllocredenziali = checkpassowrd(oldpassword, username);

        if (optional.isPresent() && controllocredenziali) {
            Studente studente = optional.get();
            studente.setPassword(passwordEncoder.encode(newpassword));
            studenteRepository.save(studente);
            return true;
        }
        return false;
    }
}
