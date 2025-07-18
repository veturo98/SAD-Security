package com.sad_security.sase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.Classe;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, String> {
    // Spring Data JPA fornirà automaticamente le implementazioni dei metodi CRUD
    // (save, findById, findAll, count, etc.)
    // Puoi aggiungere qui metodi personalizzati se necessario, ad esempio:
    Optional<Classe> findBynome(String Classe);

}
