package com.sad_security.sase.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomClasse> roomClassi = new ArrayList<>();
}

