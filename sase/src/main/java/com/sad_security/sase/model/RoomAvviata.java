package com.sad_security.sase.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "RoomAvviata")
public class RoomAvviata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "Utente") // identifiativo dell'utente
    private String studente;

    @JoinColumn(name = "Room") // identifiativo della room
    private String room;

    @JoinColumn(name = "classe")
    private String classe;

    @Column(name = "tempoAvvio")
    private LocalDateTime timestamp;

    @Column(name = "score")
    private long score;

}
