package kr.luxsoft.mdnote.config;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private kr.luxsoft.mdnote.repository.CategoryRepository categoryRepository;


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
            kr.luxsoft.mdnote.model.Category engineering = new kr.luxsoft.mdnote.model.Category();
            engineering.setName("Engineering");
            categoryRepository.save(engineering);
            
            kr.luxsoft.mdnote.model.Category backend = new kr.luxsoft.mdnote.model.Category();
            backend.setName("Backend");
            backend.setParent(engineering);
            categoryRepository.save(backend);
            
            kr.luxsoft.mdnote.model.Category frontend = new kr.luxsoft.mdnote.model.Category();
            frontend.setName("Frontend");
            frontend.setParent(engineering);
            categoryRepository.save(frontend);
            
            kr.luxsoft.mdnote.model.Category hr = new kr.luxsoft.mdnote.model.Category();
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
