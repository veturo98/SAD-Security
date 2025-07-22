package com.sad_security.sase.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sad_security.sase.model.Professore;
import com.sad_security.sase.repository.ProfessoreRepository;

@Service("professoreDetailsService")
public class ProfessorService implements UserDetailsService {

    @Autowired
    private ProfessoreRepository professoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Funzione chiamata da Spring Security durante il login del professore.
     *
     * @param username username del professore
     * @return UserDetails contenente username, password e ruoli dell'utente
     * @throws UsernameNotFoundException se il professore non è trovato nel database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Professore> optionalprofessore = professoreRepository.findByUsername(username);
        if (optionalprofessore.isEmpty()) {
            throw new UsernameNotFoundException("professore non trovato");
        }

        Professore professore = optionalprofessore.get();

        return User.withUsername(professore.getUsername())
                .password(professore.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSORE")))
                .build();
    }

    /**
     * Controlla che la password fornita corrisponda a quella salvata per l'utente.
     *
     * @param password password da verificare (in chiaro)
     * @param username username dell'utente
     * @return true se la password corrisponde, false altrimenti
     * @throws UsernameNotFoundException se il professore non è trovato nel database
     */
    public boolean checkpassowrd(String password, String username) {

        Professore professore = professoreRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome Professore non trovato"));

        // Verifica la vecchia password
        if (!passwordEncoder.matches(password, professore.getPassword())) {
            return false; // password errata
        }

        return true;
    }

    /**
     * Cambia la password del professore se le credenziali attuali sono corrette.
     *
     * @param username username del professore
     * @param oldpassword vecchia password da verificare
     * @param newpassword nuova password da impostare
     * @return true se il cambio password è avvenuto con successo, false altrimenti
     */
    public boolean cambiaPasswordProfessore(String username, String oldpassword, String newpassword) {

        Optional<Professore> account_professore = professoreRepository.findByUsername(username);

        // Controlla che le credenziali vecchie siano corrette
        Boolean controllocredenziali = checkpassowrd(oldpassword, username);

        // Se l'account esiste e le credenziali sono corrette allora confermo il cambio
        if (account_professore.isPresent() && controllocredenziali) {

            // Costruisco l'oggetto professore
            Professore professore = account_professore.get();
            professore.setPassword(passwordEncoder.encode(newpassword));

            professoreRepository.save(professore);

            return true;
        }
        
        return false;
    }

}
