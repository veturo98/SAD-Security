package com.sad_security.sase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="Utente")
public class Utente {

    // Id dell'utente
    @Id
    private long Id;

    // Dati dell'account utente
    private String Username;
    private String Mail;
    private String Password;

    // 
    

}
