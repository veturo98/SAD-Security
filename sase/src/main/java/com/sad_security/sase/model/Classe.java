package com.sad_security.sase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="Classe")
public class Classe {

    // Dati della classe
    @Id
    @Column(name = "nome")
    private String nome;

    @Column(name = "lab")
    private String lab;

    @Column(name = "studente")
    private String studente;

}
