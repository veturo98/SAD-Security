package com.sad_security.sase.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.RoomAvviata;




@Repository
public interface RoomAvviataRepository extends JpaRepository<RoomAvviata, Long> {

    Optional<RoomAvviata> findByRoomAndStudente( String room, String studente);
    LocalDateTime findTimeByRoomAndStudente (String room,String studente);
    
}
