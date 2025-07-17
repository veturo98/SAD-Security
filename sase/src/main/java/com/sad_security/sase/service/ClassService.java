package com.sad_security.sase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.repository.ClasseRepository;

@Service
public class ClassService {

    @Autowired
    private ClasseRepository ClasseRepository;

    public boolean aggiungiClasse(String classe) {

        // La classe esiste già
        Optional<Classe> clas = ClasseRepository.findBynome(classe);

        // Se la classe esiste non faccio niente
        if (clas.isPresent()) {
            System.out.println("la classe esiste già");
            return true;
        } else {

            // Creazione classe
            Classe newcClasse = new Classe();

            newcClasse.setNome(classe);

            ClasseRepository.save(newcClasse);
            System.out.println("Classe creata");

            return false;
        }
    }

    public List<Classe> trovaTutteLeClassi() {

        return ClasseRepository.findAll();

    }

    public Optional<Classe> cercaClasse(String classe) {

        return ClasseRepository.findBynome(classe);

    }

}
