package com.sad_security.sase;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sad_security.sase.model.Studente;
import com.sad_security.sase.repository.UserRepository;


@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DatabaseInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Popola il DB solo se è vuoto per evitare duplicati ad ogni riavvio
        if (userRepository.count() == 0) {
            System.out.println("Populating database with initial data...");

            Studente user1 = new Studente();
            user1.setUsername("alice");
            user1.setMail("alice@example.com");
            user1.setPassword("alicepassword");
            userRepository.save(user1);

            Studente user2 = new Studente();
            user2.setUsername("bob");
            user2.setMail("bob@example.com");
            user2.setPassword("bobpassword");
            userRepository.save(user2);

            Studente user3 = new Studente();
            user3.setUsername("charlie");
            user3.setMail("charlie@example.com");
            user3.setPassword("charliepassword");
            userRepository.save(user3);

            System.out.println("Database population complete.");
        } else {
            System.out.println("Database already contains data. Skipping initial population.");
        }
    }
}