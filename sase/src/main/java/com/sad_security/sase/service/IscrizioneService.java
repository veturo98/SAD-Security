package com.sad_security.sase.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Iscrizione;
import com.sad_security.sase.repository.IscrizioneRepository;

@Service
public class IscrizioneService {

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    // Salva l'iscrizione di uno studente ad una classe
    public boolean aggiungiIscrizione(String studente, String classe) {

        Optional<Iscrizione> iscrizione = iscrizioneRepository.findByStudenteAndClasse(studente, classe);

        // Se già iscritto non fare nulla
        if (iscrizione.isPresent()) {
            System.out.println("l'utente è già iscritto");
            return false;
        }

        // Altrimenti costruisco l'oggetto iscrizione
        Iscrizione iscriviti = new Iscrizione();
        iscriviti.setStudente(studente);
        iscriviti.setClasse(classe);

        iscrizioneRepository.save(iscriviti);
        System.out.println("l'utente si è iscritto alla classe");

        return true;

    }

    // Controlla se l'iscrizione è stata già effettuata
    public boolean controllaIscrizione(String studente, String classe) {

        Optional<Iscrizione> iscrizione = iscrizioneRepository.findByStudenteAndClasse(studente, classe);

        if (iscrizione.isPresent()) {
            System.out.println("l'utente è già iscritto");
            return true;
        }
        return false;

    }

    // Restituisce lista di nomi di classi a cui l'utente è iscritto
    public List<String> getNomiClassiIscritte(String studente) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByStudente(studente);
        return iscrizioni.stream()
                .map(Iscrizione::getClasse) // non getClasse().getNome()!
                .collect(Collectors.toList());
    }


    // Restituisce gli studenti iscritti alla classe 
    public List<Iscrizione> trovaStudenti(String classe) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByClasse(classe);
        return iscrizioni;
    }

}
