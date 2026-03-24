package com.inventory.system.controller;

import com.inventory.system.model.User;
import com.inventory.system.model.UserRole;
import com.inventory.system.service.AuditService;
import com.inventory.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    // List all users
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("title", "User Management");
        return "users/list";  // We'll create this template
    }

    // Show form to create new user
    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("title", "Add New User");
        return "users/form";
    }



    // Save new user
    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Determine if this is a new user or an update
            boolean isNew = (user.getId() == null);

            // For updates, retrieve the old user to capture previous values
            User oldUser = null;
            if (!isNew) {
                oldUser = userService.getUserById(user.getId()).orElse(null);
            }

            // Validation (username/email existence)
            if (isNew && userService.usernameExists(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Username already exists!");
                return "redirect:/users/new";
            }
            if (isNew && userService.emailExists(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email already exists!");
                return "redirect:/users/new";
            }

            // Save the user (the service handles password encoding)
            userService.saveUser(user);

            // Prepare audit log details
            if (isNew) {
                auditService.log("CREATE", "users", user.getId(),
                        null,
                        "Username: " + user.getUsername() + ", Role: " + user.getRole());
            } else {
                // Build a meaningful description of changes (optional)
                String oldDesc = oldUser != null ?
                        "Username: " + oldUser.getUsername() + ", Role: " + oldUser.getRole() :
                        "N/A";
                String newDesc = "Username: " + user.getUsername() + ", Role: " + user.getRole();
                auditService.log("UPDATE", "users", user.getId(), oldDesc, newDesc);
            }

            redirectAttributes.addFlashAttribute("success", "User saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    // Show form to edit user
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("user", user);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("title", "Edit User");
            return "users/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/users";
        }
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get user details before deletion for audit log
            User user = userService.getUserById(id).orElse(null);
            if (user != null) {
                String userInfo = "Username: " + user.getUsername() + ", Role: " + user.getRole();
                userService.deleteUser(id);
                auditService.log("DELETE", "users", id, userInfo, null);
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    // Enable/Disable user
    @GetMapping("/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user != null) {
                if (user.isEnabled()) {
                    userService.disableUser(id);
                    redirectAttributes.addFlashAttribute("success", "User disabled successfully!");
                } else {
                    userService.enableUser(id);
                    redirectAttributes.addFlashAttribute("success", "User enabled successfully!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error toggling user status: " + e.getMessage());
        }
        return "redirect:/users";
    }

    // View user details
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("user", user);
            model.addAttribute("title", "User Details");
            return "users/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/users";
        }
    }
}