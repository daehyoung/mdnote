package kr.luxsoft.mdnote.service;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User updateUser(Long id, User updatedInfo) {
        return userRepository.findById(id).map(user -> {
            if (updatedInfo.getName() != null) user.setName(updatedInfo.getName());
            if (updatedInfo.getRole() != null) user.setRole(updatedInfo.getRole());
            if (updatedInfo.getStatus() != null) user.setStatus(updatedInfo.getStatus());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void resetPassword(Long id, String newPassword) {
        userRepository.findById(id).map(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
