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

    /**
     * Salva l'iscrizione di uno studente ad una classe.
     *
     * @param studente nome dello studente da iscrivere
     * @param classe nome della classe a cui iscrivere lo studente
     * @return true se l'iscrizione è stata salvata con successo, false se lo studente è già iscritto
     */
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

    /**
     * Controlla se l'iscrizione di uno studente ad una classe è già stata effettuata.
     *
     * @param studente nome dello studente da controllare
     * @param classe nome della classe da controllare
     * @return true se lo studente è già iscritto alla classe, false altrimenti
     */
    public boolean controllaIscrizione(String studente, String classe) {

        Optional<Iscrizione> iscrizione = iscrizioneRepository.findByStudenteAndClasse(studente, classe);

        if (iscrizione.isPresent()) {
            System.out.println("l'utente è già iscritto");
            return true;
        }
        return false;

    }

    /**
     * Restituisce la lista dei nomi delle classi a cui uno studente è iscritto.
     *
     * @param studente nome dello studente
     * @return lista di nomi delle classi a cui lo studente è iscritto
     */
    public List<String> getNomiClassiIscritte(String studente) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByStudente(studente);
        return iscrizioni.stream()
                .map(Iscrizione::getClasse) // non getClasse().getNome()!
                .collect(Collectors.toList());
    }

    /**
     * Restituisce la lista degli studenti iscritti ad una classe.
     *
     * @param classe nome della classe
     * @return lista di oggetti Iscrizione relativi agli studenti iscritti alla classe
     */
    public List<Iscrizione> trovaStudenti(String classe) {
        List<Iscrizione> iscrizioni = iscrizioneRepository.findByClasse(classe);
        return iscrizioni;
    }

}
