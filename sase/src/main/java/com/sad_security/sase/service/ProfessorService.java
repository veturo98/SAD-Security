package com.sad_security.sase.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        return professoreRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Professore non trovato"));
    }

    public boolean autentica(String username, String password) {

        // Faccio la query per controllare se esiste l'studente
        Optional<Professore> query = professoreRepository.findByUsername(username);

        // Se l'studente è presente effettuo i controlli
        if (query.isPresent()) {
            Professore studente = query.get();

            // Se la password corrisponde allora l'studente è autenticato
            if (studente.getPassword().equals(password))
                return true;

        }

        return false;
    }


}
