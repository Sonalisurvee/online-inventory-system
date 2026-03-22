package com.inventory.system.service.impl;

import com.inventory.system.model.Product;
import com.inventory.system.model.Category;
import com.inventory.system.repository.ProductRepository;
import com.inventory.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByStatus("ACTIVE");
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> getLowStockProducts() {
        // For now, return products with reorder level > 0
        // Later we'll implement proper inventory-based low stock
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(p -> p.getReorderLevel() != null && p.getReorderLevel() > 0 && p.getReorderLevel() < 10)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public boolean productExists(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public Product updateStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // We'll handle inventory separately later
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public Product activateProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus("ACTIVE");
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public Product deactivateProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus("INACTIVE");
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public List<Product> getExpiringProducts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return productRepository.findProductsExpiringBetween(today, endDate);
    }

}