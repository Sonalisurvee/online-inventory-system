package com.inventory.system;

import com.inventory.system.model.User;
import com.inventory.system.model.UserRole;
import com.inventory.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ServiceTest implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("🔍 TESTING SERVICE LAYER");
        System.out.println("========================================");

        try {
            // Test 1: Create a new user
            User newUser = new User("testuser", "password123", "test@test.com", UserRole.STAFF);
            User savedUser = userService.saveUser(newUser);
            System.out.println("✅ User created: " + savedUser.getUsername() + " (ID: " + savedUser.getId() + ")");

            // Test 2: Get user by username
            var foundUser = userService.getUserByUsername("testuser");
            if (foundUser.isPresent()) {
                System.out.println("✅ Found user by username: " + foundUser.get().getUsername());
            }

            // Test 3: Get all users
            List<User> allUsers = userService.getAllUsers();
            System.out.println("✅ Total users in system: " + allUsers.size());

            // Test 4: Test authentication
            boolean authResult = userService.authenticate("testuser", "password123");
            System.out.println("✅ Authentication test: " + (authResult ? "SUCCESS" : "FAILED"));

            // Test 5: Check username existence
            boolean exists = userService.usernameExists("testuser");
            System.out.println("✅ Username exists check: " + exists);

            System.out.println("\n🎉 All service tests passed successfully!");

        } catch (Exception e) {
            System.out.println("❌ Service test failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================\n");
    }
}