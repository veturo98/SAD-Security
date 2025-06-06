package com.sad_security.sase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="person")
public class Persona {
    @Id
    private long id;
    private String name;

}
