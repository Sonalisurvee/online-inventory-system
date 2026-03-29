package com.inventory.system.controller;

import com.inventory.system.model.Product;
import com.inventory.system.model.Category;
import com.inventory.system.service.AuditService;
import com.inventory.system.service.ProductService;
import com.inventory.system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuditService auditService;

    @Value("${file.upload-dir}")
    private String uploadDir;

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
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (product.getId() == null);
            Product oldProduct = null;
            if (!isNew) {
                oldProduct = productService.getProductById(product.getId()).orElse(null);
            }

            // Set category
            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            }

            // Check for duplicate name only for new products
            if (isNew && productService.productExists(product.getName())) {
                redirectAttributes.addFlashAttribute("error", "Product name already exists!");
                return "redirect:/products/new";
            }

            // --- HANDLE IMAGE UPLOAD ---
            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image if exists (for updates)
                if (!isNew && oldProduct != null && oldProduct.getImagePath() != null) {
                    Path oldPath = Paths.get(System.getProperty("user.dir"), uploadDir, oldProduct.getImagePath());
                    try {
                        Files.deleteIfExists(oldPath);
                    } catch (IOException e) {
                        // Log error but continue
                    }
                }

                // Build absolute path to upload directory (e.g., C:/project/uploads/products)
                Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);  // Create if missing
                }

                // Generate unique filename
                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                // Save file
                Path filePath = uploadPath.resolve(filename);
                imageFile.transferTo(filePath.toFile());

                // Store only the filename (relative part) in database
                product.setImagePath(filename);
            } else if (!isNew && oldProduct != null) {
                // No new image uploaded – keep existing
                product.setImagePath(oldProduct.getImagePath());
            }

            // Save product
            productService.saveProduct(product);

            // Audit logging
            if (isNew) {
                auditService.log("CREATE", "products", product.getId(),
                        null,
                        "Name: " + product.getName() + ", Price: " + product.getUnitPrice());
            } else {
                String oldDesc = oldProduct != null ?
                        "Name: " + oldProduct.getName() + ", Price: " + oldProduct.getUnitPrice() : "N/A";
                String newDesc = "Name: " + product.getName() + ", Price: " + product.getUnitPrice();
                auditService.log("UPDATE", "products", product.getId(), oldDesc, newDesc);
            }

            redirectAttributes.addFlashAttribute("success", "Product saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
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
            // Fetch product details before deletion
            Product product = productService.getProductById(id).orElse(null);
            if (product != null) {
                String productInfo = "Name: " + product.getName() + ", Price: " + product.getUnitPrice();
                productService.deleteProduct(id);
                auditService.log("DELETE", "products", id, productInfo, null);
                redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Product not found!");
            }
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