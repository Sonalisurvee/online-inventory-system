package com.inventory.system.service;

import com.inventory.system.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

    // Create/Update
    Category saveCategory(Category category);

    // Read
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Optional<Category> getCategoryByName(String name);
    List<Category> searchCategories(String keyword);

    // Delete
    void deleteCategory(Long id);

    // Check existence
    boolean categoryExists(String name);

    // Toggle status
    Category activateCategory(Long id);
    Category deactivateCategory(Long id);
}