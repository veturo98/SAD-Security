package com.sad_security.sase.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="Room")
public class Room {

    // Dati della room
    @Id
    @Column(name = "lab")
    private String lab;

    @Column(name = "timestamp")
    private String studente;

    @Column(name = "score")
    private String password;


}

