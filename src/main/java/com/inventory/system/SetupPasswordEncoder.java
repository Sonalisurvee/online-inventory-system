package com.inventory.system;

import com.inventory.system.model.User;
import com.inventory.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SetupPasswordEncoder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("🔐 SETTING UP PASSWORDS FOR LOGIN");
        System.out.println("========================================");

        // Update admin password
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("✅ Admin password updated");
        }

        // Update manager password
        User manager = userRepository.findByUsername("manager1").orElse(null);
        if (manager != null) {
            manager.setPassword(passwordEncoder.encode("manager123"));
            userRepository.save(manager);
            System.out.println("✅ Manager password updated");
        }

        // Update staff password
        User staff = userRepository.findByUsername("staff1").orElse(null);
        if (staff != null) {
            staff.setPassword(passwordEncoder.encode("staff123"));
            userRepository.save(staff);
            System.out.println("✅ Staff password updated");
        }

        System.out.println("========================================\n");
    }
}