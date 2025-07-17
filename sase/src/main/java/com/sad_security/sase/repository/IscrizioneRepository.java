package com.sad_security.sase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Iscrizione;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.model.Studente;

import java.util.List;
import java.util.Optional;


@Repository
public interface IscrizioneRepository  extends JpaRepository<Iscrizione, Long>{

  Optional <Iscrizione> findByStudenteAndClasse(Studente studenteName, Classe classeName);  
  List <Iscrizione> findByStudente(Studente studenteName);  // restituisce una lista di classi a cui l'utente è iscritto
} 