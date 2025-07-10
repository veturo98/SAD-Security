package com.sad_security.sase.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.StudenteRepository;

@Service
public class StudenteService {

    @Autowired
    private StudenteRepository userepository;


    @Autowired 
    private PasswordEncoder passwordEncoder;

    public boolean autenticaStudente(String username, String password) {

        // Faccio la query per controllare se esiste l'studente
        Optional<Studente> query = userepository.findByUsername(username);

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
        Optional<Studente> userName = userepository.findByUsername(username);
        Optional<Studente> userMail = userepository.findByMail(mail);

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

            userepository.save(newStudente);
            System.out.println("studente creato");

            return false;
        }

    }

}
