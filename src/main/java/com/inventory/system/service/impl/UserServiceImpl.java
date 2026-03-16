package com.inventory.system.service.impl;

import com.inventory.system.model.User;
import com.inventory.system.model.UserRole;
import com.inventory.system.repository.UserRepository;
import com.inventory.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
// IMPORTANT: REMOVE this import
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    // REMOVE this field completely
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        // We'll handle password encoding in a separate service later
        // For now, just save without encoding
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Simple password check for now
            if (user.isEnabled() && password.equals(user.getPassword())) {
                return userOpt;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User enableUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(true);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User disableUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(false);
            return userRepository.save(user);
        }
        return null;
    }
}