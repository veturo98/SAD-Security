package com.sad_security.sase.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "RoomAvviata")
public class RoomAvviata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Utente") // identifiativo dell'utente
    private Studente studente;

    @ManyToOne
    @JoinColumn(name = "Room") // identifiativo della room
    private Room room;
}
