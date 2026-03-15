package com.inventory.system.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_transfers")
public class StockTransfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long id;

    @Column(name = "transfer_no", unique = true, nullable = false)
    private String transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id", nullable = false)
    private Store fromStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_store_id", nullable = false)
    private Store toStore;

    @Column(nullable = false)
    private Integer quantity;

    // REMOVED: private TransferStatus status;
    // We will use the status field from BaseEntity instead

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public StockTransfer() {}

    public StockTransfer(String transferNo, Product product, Store fromStore, Store toStore,
                         Integer quantity, User requestedBy, LocalDate requestDate) {
        this.transferNo = transferNo;
        this.product = product;
        this.fromStore = fromStore;
        this.toStore = toStore;
        this.quantity = quantity;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.setStatus("PENDING");  // Using BaseEntity's status
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public void setTransferNo(String transferNo) {
        this.transferNo = transferNo;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Store getFromStore() {
        return fromStore;
    }

    public void setFromStore(Store fromStore) {
        this.fromStore = fromStore;
    }

    public Store getToStore() {
        return toStore;
    }

    public void setToStore(Store toStore) {
        this.toStore = toStore;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // REMOVED: getStatus and setStatus - using BaseEntity's methods

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
        if (completionDate != null) {
            this.setStatus("COMPLETED");
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}