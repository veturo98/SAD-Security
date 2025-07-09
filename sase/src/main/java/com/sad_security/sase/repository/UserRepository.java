package com.sad_security.sase.repository; // Assicurati che il package sia corretto

import com.sad_security.sase.model.Studente; // Importa la tua classe Studente

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Studente, Long> {
    // Spring Data JPA fornirà automaticamente le implementazioni dei metodi CRUD (save, findById, findAll, count, etc.)
    // Puoi aggiungere qui metodi personalizzati se necessario, ad esempio:
    Optional<Studente> findByUsername(String username);
    Optional<Studente> findByMail(String mail);

    
}

