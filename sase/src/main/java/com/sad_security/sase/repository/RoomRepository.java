package com.sad_security.sase.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sad_security.sase.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findBynome(String nome);
}
