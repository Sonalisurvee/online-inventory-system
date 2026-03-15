package com.inventory.system.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stores")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    private String phone;

    private String email;

    // Relationships
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<Inventory> inventory = new HashSet<>();

    @OneToMany(mappedBy = "store")
    private Set<Purchase> purchases = new HashSet<>();

    @OneToMany(mappedBy = "store")
    private Set<Sale> sales = new HashSet<>();

    @OneToMany(mappedBy = "fromStore")
    private Set<StockTransfer> outgoingTransfers = new HashSet<>();

    @OneToMany(mappedBy = "toStore")
    private Set<StockTransfer> incomingTransfers = new HashSet<>();

    // Constructors
    public Store() {}

    public Store(String name, String location, String phone, String email) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Inventory> getInventory() {
        return inventory;
    }

    public void setInventory(Set<Inventory> inventory) {
        this.inventory = inventory;
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

    public Set<StockTransfer> getOutgoingTransfers() {
        return outgoingTransfers;
    }

    public void setOutgoingTransfers(Set<StockTransfer> outgoingTransfers) {
        this.outgoingTransfers = outgoingTransfers;
    }

    public Set<StockTransfer> getIncomingTransfers() {
        return incomingTransfers;
    }

    public void setIncomingTransfers(Set<StockTransfer> incomingTransfers) {
        this.incomingTransfers = incomingTransfers;
    }
}