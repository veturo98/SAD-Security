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

    
    @JoinColumn(name = "classe") // perché "nome" è la @Id di Classe
    private String classe;

    
    @JoinColumn(name = "room") // supponiamo che Room abbia @Id Long id
    private String room;
}
