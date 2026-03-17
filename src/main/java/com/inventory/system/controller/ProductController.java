package com.inventory.system.controller;

import com.inventory.system.model.Product;
import com.inventory.system.model.Category;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // List all products
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Product Management");
        return "products/list";
    }

    // Show form to create new product
    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("title", "Add New Product");
        return "products/form";
    }

    // Save new product
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "category.id", required = false) Long categoryId,
                              RedirectAttributes redirectAttributes) {
        try {
            // Set the category if ID is provided
            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            }

            // Check if product name already exists (for new products)
            if (product.getId() == null && productService.productExists(product.getName())) {
                redirectAttributes.addFlashAttribute("error",
                        "Product name already exists!");
                return "redirect:/products/new";
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success",
                    "Product saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error saving product: " + e.getMessage());
            return "redirect:/products/new";
        }
        return "redirect:/products";
    }

    // Show form to edit product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            // Ensure category is loaded
            if (product.getCategory() != null) {
                // Force load the category
                product.getCategory().getName();
            }

            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("title", "Edit Product");
            return "products/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Product not found! " + e.getMessage());
            return "redirect:/products";
        }
    }

    // Delete product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success",
                    "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // View product details
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            model.addAttribute("product", product);
            model.addAttribute("title", "Product Details");
            return "products/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
            return "redirect:/products";
        }
    }

    // Search products
    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results");
        return "products/list";
    }

    // Filter by category
    @GetMapping("/category/{categoryId}")
    public String filterByCategory(@PathVariable Long categoryId, Model model) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        Category category = categoryService.getCategoryById(categoryId).orElse(null);
        model.addAttribute("products", products);
        model.addAttribute("title", "Products in " + (category != null ? category.getName() : "Category"));
        return "products/list";
    }

    // Low stock products
    @GetMapping("/low-stock")
    public String lowStockProducts(Model model) {
        List<Product> products = productService.getLowStockProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Low Stock Products");
        return "products/list";
    }
}