package com.inventory.system.model;

import com.inventory.system.model.UserRole;  // This should be at top
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    // Relationships
    @OneToMany(mappedBy = "manager")
    private Set<Store> managedStores = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Purchase> purchases = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Sale> sales = new HashSet<>();

    @OneToMany(mappedBy = "requestedBy")
    private Set<StockTransfer> requestedTransfers = new HashSet<>();

    @OneToMany(mappedBy = "approvedBy")
    private Set<StockTransfer> approvedTransfers = new HashSet<>();

    // Constructors
    public User() {}

    public User(String username, String password, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.enabled = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Store> getManagedStores() {
        return managedStores;
    }

    public void setManagedStores(Set<Store> managedStores) {
        this.managedStores = managedStores;
    }

    public Set<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(Set<Purchase> purchases) {
        this.purchases = purchases;
    }

    public Set<Sale> getSales() {
        return sales;
    }

    public void setSales(Set<Sale> sales) {
        this.sales = sales;
    }

    public Set<StockTransfer> getRequestedTransfers() {
        return requestedTransfers;
    }

    public void setRequestedTransfers(Set<StockTransfer> requestedTransfers) {
        this.requestedTransfers = requestedTransfers;
    }

    public Set<StockTransfer> getApprovedTransfers() {
        return approvedTransfers;
    }

    public void setApprovedTransfers(Set<StockTransfer> approvedTransfers) {
        this.approvedTransfers = approvedTransfers;
    }
}