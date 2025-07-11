package com.sad_security.sase.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sad_security.sase.model.Professore;

@Repository
public interface ProfessoreRepository extends JpaRepository<Professore, Long> {
    // Spring Data JPA fornirà automaticamente le implementazioni dei metodi CRUD
    // (save, findById, findAll, count, etc.)
    // Puoi aggiungere qui metodi personalizzati se necessario, ad esempio:
    Optional<Professore> findByUsername(String username);
    Optional<Professore> findByMail(String mail);

}
