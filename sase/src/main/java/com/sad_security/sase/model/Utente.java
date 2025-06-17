package com.sad_security.sase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="Utente")
public class Utente {

    // Dati dell'account utente
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "mail")
    private String mail;

    @Column(name = "password")
    private String password;


    

}
