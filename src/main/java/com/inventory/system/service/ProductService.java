package com.inventory.system.service;

import com.inventory.system.model.Product;
import com.inventory.system.model.Category;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    // Create/Update
    Product saveProduct(Product product);

    // Read
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    List<Product> getProductsByCategory(Category category);
    List<Product> getProductsByCategoryId(Long categoryId);
    List<Product> searchProducts(String keyword);
    List<Product> getLowStockProducts();

    // Delete
    void deleteProduct(Long id);

    // Check existence
    boolean productExists(String name);

    // Update stock
    Product updateStock(Long productId, int quantity);

    // Toggle status
    Product activateProduct(Long id);
    Product deactivateProduct(Long id);
}