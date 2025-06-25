package com.sad_security.sase.repository; // Assicurati che il package sia corretto

import com.sad_security.sase.model.Utente; // Importa la tua classe Utente

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Utente, Long> {
    // Spring Data JPA fornirà automaticamente le implementazioni dei metodi CRUD (save, findById, findAll, count, etc.)
    // Puoi aggiungere qui metodi personalizzati se necessario, ad esempio:
    Optional<Utente> findByUsername(String username);
    Optional<Utente> findByMail(String mail);

    
}

