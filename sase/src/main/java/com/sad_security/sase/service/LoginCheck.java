package com.sad_security.sase.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Utente;
import com.sad_security.sase.repository.UserRepository;

@Service
public class LoginCheck {

    @Autowired
    private UserRepository userepository;

    public boolean autentica(String username, String password){
        Optional<Utente> utente = userepository.findByUsername(username);
        
        return utente.isPresent() && utente.get().getPassword().equals(password);
    }

}
