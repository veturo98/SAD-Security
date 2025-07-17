package com.sad_security.sase.service;


import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.StudenteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service("studenteDetailsService")
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

      // Metodo per ritornare un oggetto utile all'iscrizione dello studente alla classe
    public Optional<Studente> findByUsername(String username) {
        return studenteRepository.findByUsername(username);
    }

    public boolean autenticaStudente(String username, String password) {

        Studente studente = studenteRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Nome studente non trovato"));

        // Verifica la vecchia password
    if (!passwordEncoder.matches(password, studente.getPassword())) {
        return false; // password errata
    }
    return true;
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
            // newStudente.setRoles(List.of("STUDENTE"));

            studenteRepository.save(newStudente);
            System.out.println("studente creato");

            return false;
        }

    }

    //Controlla che esiste la password nel database
    public boolean checkpassowrd(String password, String username) {

        Studente studente = studenteRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Nome Studente non trovato"));

        // Verifica la vecchia password
    if (!passwordEncoder.matches(password, studente.getPassword())) {
        return false; // password errata
    }
      return true;
    }

       public boolean cambiaPasswordStudente(String username,String oldpassword, String newpassword) {
    
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
