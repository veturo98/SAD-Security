package com.sad_security.sase.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "RoomStudente")
public class RoomStudente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utente_nome") // identifiativo dell'utente
    private Studente studente;

    @ManyToOne
    @JoinColumn(name = "room_nome") // identifiativo della room
    private Room room;
}
