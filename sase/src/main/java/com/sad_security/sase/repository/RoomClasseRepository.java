package com.sad_security.sase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.model.RoomClasse;

@Repository
public interface RoomClasseRepository extends JpaRepository<RoomClasse, Long>{
    
    Optional<RoomClasse> findByClasseAndRoom(Optional<Classe> classe, Optional<Room> room);
    List<RoomClasse> findByClasse(String classe);
    
}                        
