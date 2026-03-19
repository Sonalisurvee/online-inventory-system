package com.inventory.system.dto;

import java.time.LocalDateTime;

public class StockMovement {
    private LocalDateTime dateTime;
    private String type;        // "PURCHASE", "SALE"
    private int quantity;
    private String reference;
    private String notes;

    public StockMovement(LocalDateTime dateTime, String type, int quantity, String reference, String notes) {
        this.dateTime = dateTime;
        this.type = type;
        this.quantity = quantity;
        this.reference = reference;
        this.notes = notes;
    }

    // Getters and setters
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}