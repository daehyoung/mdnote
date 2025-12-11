package com.example.mdnote.config;

import com.example.mdnote.model.User;
import com.example.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.mdnote.repository.CategoryRepository categoryRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createUserIfNotFound("admin", "admin", "ADMIN");
        createUserIfNotFound("test", "test", "USER");
        createUserIfNotFound("admin", "admin", "ADMIN");
        createUserIfNotFound("test", "test", "USER");
        
        createCategories();
    }

    private void createCategories() {
        if (categoryRepository.count() == 0) {
            com.example.mdnote.model.Category engineering = new com.example.mdnote.model.Category();
            engineering.setName("Engineering");
            categoryRepository.save(engineering);
            
            com.example.mdnote.model.Category backend = new com.example.mdnote.model.Category();
            backend.setName("Backend");
            backend.setParent(engineering);
            categoryRepository.save(backend);
            
            com.example.mdnote.model.Category frontend = new com.example.mdnote.model.Category();
            frontend.setName("Frontend");
            frontend.setParent(engineering);
            categoryRepository.save(frontend);
            
            com.example.mdnote.model.Category hr = new com.example.mdnote.model.Category();
            hr.setName("Human Resources");
            categoryRepository.save(hr);
            
            System.out.println("Seeded categories");
        }
    }

    private void createUserIfNotFound(String username, String password, String role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setName(username); // Set name as username for simplicity
            user.setRole(role);
            user.setStatus("ACTIVE");
            userRepository.save(user);
            System.out.println("Created user: " + username);
        }
    }
}
