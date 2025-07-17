package com.sad_security.sase.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sad_security.sase.model.Classe;
import com.sad_security.sase.model.Room;
import com.sad_security.sase.model.RoomClasse;
import com.sad_security.sase.repository.RoomClasseRepository;

@Service
public class RoomClasseService {
    
    @Autowired
    private RoomClasseRepository roomClasseRepository;


    public boolean aggiungiassociazione(Optional <Classe> classe, Optional<Room> room){
     // Cerco se l'associazione  
        Optional<RoomClasse> roomClass = roomClasseRepository.findByClasseAndRoom(classe,room);
         // Se la classe esiste non faccio niente
        if (roomClass.isPresent() ) {
            System.out.println("l'associazione già");
            return true;
        } else {

            // Creazione associazione 
            RoomClasse rc = new RoomClasse();
            rc.setClasse(classe.get());
            rc.setRoom(room.get());
    

            roomClasseRepository.save(rc);
            System.out.println("Associazione room class creata");

            return false;
        }
}
}
