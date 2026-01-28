package com.istlgroup.istl_group_crm_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_book_items")
@Data
public class OrderBookItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_book_id", nullable = false)
    private Long orderBookId;
    
    @Column(name = "line_no")
    private Integer lineNo = 1;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "specification", columnDefinition = "TEXT")
    private String specification;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "proposal_item_id")
    private Long proposalItemId;
    
    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "unit")
    private String unit = "Nos";
    
    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "tax_percent", precision = 5, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "item_remarks", columnDefinition = "TEXT")
    private String itemRemarks;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Note: Calculated fields (line_subtotal, discount_amount, etc.) are handled by database
    // We'll fetch them when reading from DB
    @Transient
    private BigDecimal lineSubtotal;
    
    @Transient
    private BigDecimal discountAmount;
    
    @Transient
    private BigDecimal taxableAmount;
    
    @Transient
    private BigDecimal taxAmount;
    
    @Transient
    private BigDecimal lineTotal;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}