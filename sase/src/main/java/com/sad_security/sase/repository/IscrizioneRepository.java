package com.sad_security.sase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.Iscrizione;

import java.util.List;
import java.util.Optional;

@Repository
public interface IscrizioneRepository extends JpaRepository<Iscrizione, Long> {

  Optional<Iscrizione> findByStudenteAndClasse(String studenteName, String classeName);

  List<Iscrizione> findByStudente(String studenteName); // restituisce una lista di classi a cui l'utente è iscritto

  List<Iscrizione> findByClasse(String classe); // restituisce la lista di studenti iscritti alla classe

}