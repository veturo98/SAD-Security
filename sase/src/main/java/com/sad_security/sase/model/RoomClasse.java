package com.sad_security.sase.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
// in questo caso posso voler avere una stessa room in diverse classi
@Table(name = "RoomClasse")
public class RoomClasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "classe") // perché "nome" è la @Id di Classe
    private String classe;

    @ManyToOne
    @JoinColumn(name = "room") // supponiamo che Room abbia @Id Long id
    private String room;
}
