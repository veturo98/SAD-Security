package com.sad_security.sase.service;

import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.StudenteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class StudenteService implements UserDetailsService {

    @Autowired
    private StudenteRepository studenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public boolean autenticaStudente(String username, String password) {

        // Faccio la query per controllare se esiste l'studente
        Optional<Studente> query = studenteRepository.findByUsername(username);

        // Se l'studente è presente effettuo i controlli
        if (query.isPresent()) {
            Studente studente = query.get();

            // Se la password corrisponde allora l'studente è autenticato
            if (studente.getPassword().equals(password))
                return true;

        }

        return false;
    }

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

            studenteRepository.save(newStudente);
            System.out.println("studente creato");

            return false;
        }

    }

}
