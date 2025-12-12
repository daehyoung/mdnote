package kr.luxsoft.mdnote.config;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
        createCategories();
    }

    private void createCategories() {
        if (categoryRepository.count() == 0) {
            kr.luxsoft.mdnote.model.Category docs = new kr.luxsoft.mdnote.model.Category();
            docs.setName("Document");
            categoryRepository.save(docs);
            
            kr.luxsoft.mdnote.model.Category reports = new kr.luxsoft.mdnote.model.Category();
            reports.setName("Report"); 
            categoryRepository.save(reports);
            
            kr.luxsoft.mdnote.model.Category hr = new kr.luxsoft.mdnote.model.Category();
            hr.setName("Resources");
            categoryRepository.save(hr);
            
            log.debug("Seeded categories");
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
            log.debug("Created user: {}", username);
        }
    }
}
