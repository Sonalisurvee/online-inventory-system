package com.inventory.system.service;

import com.inventory.system.model.User;
import com.inventory.system.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // Create/Update
    User saveUser(User user);

    // Read
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    List<User> getUsersByRole(UserRole role);

    // Delete
    void deleteUser(Long id);

    // Authentication
    boolean authenticate(String username, String password);

    // Check existence
    boolean usernameExists(String username);
    boolean emailExists(String email);

    // Toggle status
    User enableUser(Long id);
    User disableUser(Long id);
}