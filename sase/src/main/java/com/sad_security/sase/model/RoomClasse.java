package com.sad_security.sase.model;

import jakarta.persistence.*;
import lombok.Data;

/** Relazione tra classi e room appartenenti ad esse**/
@Data
@Entity
@Table(name = "RoomClasse")
public class RoomClasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @JoinColumn(name = "classe")
    private String classe;

    
    @JoinColumn(name = "room") 
    private String room;
}
