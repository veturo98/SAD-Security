package com.sad_security.sase.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Utente;
import com.sad_security.sase.repository.UserRepository;

@Service
public class UtenteServices {

    @Autowired
    private UserRepository userepository;

    public boolean autenticaUtente(String username, String password) {

        // Faccio la query per controllare se esiste l'utente
        Optional<Utente> query = userepository.findByUsername(username);

        // Se l'utente è presente effettuo i controlli
        if (query.isPresent()) {
            Utente utente = query.get();

            // Se la password corrisponde allora l'utente è autenticato
            if (utente.getPassword() == password)
                return true;

        }

        return false;
    }

    public boolean aggiungiUtente(String username, String password, String mail) {

        // Cerco se l'utente esiste già (mail o username già utilizzato)
        Optional<Utente> userName = userepository.findByUsername(username);
        Optional<Utente> userMail = userepository.findByMail(mail);

        // Se l'utente esiste allora dico che già esiste
        if (userName.isPresent() || userMail.isPresent()) {
            System.out.println("l'utente esiste già");
            return true;
        } else {

            // Creazione utente con credenziali inserite
            Utente newUtente = new Utente();

            newUtente.setUsername(username);
            newUtente.setMail(mail);
            newUtente.setPassword(password);

            userepository.save(newUtente);
            System.out.println("utente creato");

            return false;
        }

    }

}
