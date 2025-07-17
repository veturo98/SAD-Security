package com.sad_security.sase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
// Controllo del database che evita che un utente si iscriva ad una stessa classe
@Table(name = "Iscrizione", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "studente_id", "classe_nome" })
})
public class Iscrizione {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iscrizioneId;

   
    @JoinColumn(name = "studente", nullable = false)
    private String studente;

    
    @JoinColumn(name = "classe", nullable = false)
    private String classe;
}
