package com.sad_security.sase;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sad_security.sase.model.Professore;
import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.ProfessoreRepository;
import com.sad_security.sase.repository.StudenteRepository;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final StudenteRepository userRepository;
    private final ProfessoreRepository profRepository;

    public DatabaseInitializer(StudenteRepository userRepository, ProfessoreRepository profRepository) {
        this.userRepository = userRepository;
        this.profRepository = profRepository;
    }

    // Tutte le password hanno il formato "nome_utente"+"password"
    @Override
    public void run(String... args) throws Exception {
        // Popola il DB solo se è vuoto per evitare duplicati ad ogni riavvio
        if (userRepository.count() == 0) {
            System.out.println("Populating database with initial data...");


            Studente user1 = new Studente();
            user1.setUsername("alice");
            user1.setMail("alice@example.com");
            user1.setPassword(passwordEncoder.encode("alicepassword"));
            userRepository.save(user1);

            Studente user2 = new Studente();
            user2.setUsername("bob");
            user2.setMail("bob@example.com");
            user2.setPassword(passwordEncoder.encode("bobpassword"));       
            userRepository.save(user2);

            Studente user3 = new Studente();
            user3.setUsername("charlie");
            user3.setMail("charlie@example.com");
            user3.setPassword(passwordEncoder.encode("charliepassword"));
            userRepository.save(user3);

            System.out.println("Students added.");
        } else {
            System.out.println("Students already added. Skipping initial population.");
        }

        // aggiungo gli admin
        if (profRepository.count() == 0) {
            System.out.println("Populating database with initial data...");

            Professore prof1 = new Professore();
            prof1.setUsername("fasolino");
            prof1.setMail("fasolino@unina.it");
            prof1.setPassword(passwordEncoder.encode("fasolinopassword"));
            
            profRepository.save(prof1);

            Professore prof2 = new Professore();
            prof2.setUsername("natella");
            prof2.setMail("natella@unina.it");
            prof2.setPassword(passwordEncoder.encode("natellapassword"));
            profRepository.save(prof2);

            System.out.println("Prof added.");
        } else {
            System.out.println("Prof already added. Skipping initial population.");
        }
    }
}