package com.sad_security.sase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
// Controllo del database che evita che un utente si iscriva ad una stessa classe
@Table(name = "Iscrizione", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "studente", "classe" })
})
public class Iscrizione {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iscrizioneId;

   /**studente iscritto alla classe**/
    @JoinColumn(name = "studente", nullable = false)
    private String studente;

    /**classe a cui lo studente è iscritto**/
    @JoinColumn(name = "classe", nullable = false)
    private String classe;
}
