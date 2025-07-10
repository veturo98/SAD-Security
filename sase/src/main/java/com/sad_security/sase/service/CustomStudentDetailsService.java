package com.sad_security.sase.service;

import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.StudenteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomStudentDetailsService implements UserDetailsService {

   

    @Autowired
    private StudenteRepository studenteRepository;

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

}
