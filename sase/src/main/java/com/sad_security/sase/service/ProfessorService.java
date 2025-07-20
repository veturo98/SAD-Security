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

    // Funzione chiamata da springSecurity durante il login del professore
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

    // Controlla che esiste la password nel database
    public boolean checkpassowrd(String password, String username) {

        Professore professore = professoreRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome Professore non trovato"));

        // Verifica la vecchia password
        if (!passwordEncoder.matches(password, professore.getPassword())) {
            return false; // password errata

        }

        return true;
    }

    // Funzione di cambio password per il professore
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
