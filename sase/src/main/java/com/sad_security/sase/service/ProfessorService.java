package com.sad_security.sase.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;


import com.sad_security.sase.model.Professore;
import com.sad_security.sase.repository.ProfessoreRepository;

@Service("professoreDetailsService")
public class ProfessorService implements UserDetailsService {
    
 @Autowired
    private ProfessoreRepository professoreRepository;


       @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<Professore> optionalprofessore = professoreRepository.findByUsername(username);
        if (optionalprofessore.isEmpty()) {
            throw new UsernameNotFoundException("Studente non trovato");
        }

        Professore professore = optionalprofessore.get();

        return User.withUsername(professore.getUsername())
                .password(professore.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSORE")))
                .build();
    }

    public boolean autentica(String username, String password) {

        // Faccio la query per controllare se esiste il professore
        Optional<Professore> query = professoreRepository.findByUsername(username);

        // Se il professore è presente effettuo i controlli
        if (query.isPresent()) {
            Professore studente = query.get();

            // Se la password corrisponde allora l'studente è autenticato
            if (studente.getPassword().equals(password))
                return true;

        }

        return false;
    }


}
