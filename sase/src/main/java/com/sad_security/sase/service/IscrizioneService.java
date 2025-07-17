package com.sad_security.sase.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Iscrizione;
import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.IscrizioneRepository;

@Service
public class IscrizioneService {
    
    @Autowired
    private IscrizioneRepository iscrizioneRepository;


    public void aggiungiIscrizione (Optional <Studente> studente, Optional<Classe> classe){

        Optional <Iscrizione> iscrizione = iscrizioneRepository.findByStudenteAndClasse(studente.get(), classe.get());

        if (iscrizione.isPresent()){
            System.out.println("l'utente è già iscritto");
           
        }
        Iscrizione iscriviti = new Iscrizione();
        iscriviti.setStudente(studente.get());
        iscriviti.setClasse(classe.get());

        iscrizioneRepository.save(iscriviti);
        System.out.println("l'utente si è iscritto alla classe");
        
    }


    public boolean controllaIscrizione (Optional <Studente> studente, Optional<Classe> classe){

        Optional <Iscrizione> iscrizione = iscrizioneRepository.findByStudenteAndClasse(studente.get(), classe.get());

        if (iscrizione.isPresent()){
            System.out.println("l'utente è già iscritto");
            return true;
        }
        return false;

    }

    
    // Restituisce lista di nomi di classi a cui l'utente è iscritto
    public List<String> getNomiClassiIscritte(Studente studente) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByStudente(studente);
        return iscrizioni.stream()
                         .map(iscrizione -> iscrizione.getClasse().getNome()) 
                         .collect(Collectors.toList());
    }

}
