package com.inventory.system.controller;

import com.inventory.system.model.Category;
import com.inventory.system.service.AuditService;
import com.inventory.system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuditService auditService;

    // List all categories
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("title", "Category Management");
        return "categories/list";
    }

    // Show form to create new category
    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("title", "Add New Category");
        return "categories/form";
    }

    // Save new category
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (category.getId() == null);
            Category oldCategory = null;
            if (!isNew) {
                oldCategory = categoryService.getCategoryById(category.getId()).orElse(null);
            }

            // Check if category name already exists
            if (isNew && categoryService.categoryExists(category.getName())) {
                redirectAttributes.addFlashAttribute("error",
                        "Category name already exists!");
                return "redirect:/categories/new";
            }

            categoryService.saveCategory(category);

            // Audit log
            if (isNew) {
                auditService.log("CREATE", "categories", category.getId(),
                        null,
                        "Name: " + category.getName() + ", Description: " + (category.getDescription() != null ? category.getDescription() : ""));
            } else {
                String oldDesc = oldCategory != null ?
                        "Name: " + oldCategory.getName() + ", Description: " + (oldCategory.getDescription() != null ? oldCategory.getDescription() : "") : "N/A";
                String newDesc = "Name: " + category.getName() + ", Description: " + (category.getDescription() != null ? category.getDescription() : "");
                auditService.log("UPDATE", "categories", category.getId(), oldDesc, newDesc);
            }

            redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving category: " + e.getMessage());
        }
        return "redirect:/categories";
    }


    // Show form to edit category
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            model.addAttribute("category", category);
            model.addAttribute("title", "Edit Category");
            return "categories/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Category not found!");
            return "redirect:/categories";
        }
    }

    // Delete category

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id).orElse(null);
            if (category != null) {
                String categoryInfo = "Name: " + category.getName() + ", Description: " + (category.getDescription() != null ? category.getDescription() : "");
                categoryService.deleteCategory(id);
                auditService.log("DELETE", "categories", id, categoryInfo, null);
                redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Category not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting category: " + e.getMessage());
        }
        return "redirect:/categories";
    }


    // View category details
    @GetMapping("/view/{id}")
    public String viewCategory(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            model.addAttribute("category", category);
            model.addAttribute("title", "Category Details");
            return "categories/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Category not found!");
            return "redirect:/categories";
        }
    }

    // Search categories
    @GetMapping("/search")
    public String searchCategories(@RequestParam String keyword, Model model) {
        List<Category> categories = categoryService.searchCategories(keyword);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "categories/list";
    }
}