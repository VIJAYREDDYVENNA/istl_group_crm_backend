package com.istlgroup.istl_group_crm_backend.service;

import com.istlgroup.istl_group_crm_backend.entity.*;
import com.istlgroup.istl_group_crm_backend.repo.*;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.PurchaseOrderDropdownWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final QuotationRepository quotationRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final VendorRepository vendorRepository;
    

/**
 * ✅ UPDATED: Get purchase orders with PAYMENT STATUS filter support
 */
@Transactional(readOnly = true)
public Page<PurchaseOrderEntity> getPurchaseOrders(
        String groupName,
        String subGroupName,
        String projectId,
        String status,
        String paymentStatus, // ✅ ADDED parameter
        String searchTerm,
        Long userId,
        String userRole,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    Pageable pageable = PageRequest.of(
            page, 
            size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
    );
    
    boolean isAdmin = isAdmin(userRole);
    
    // Search takes priority
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
        return purchaseOrderRepository.search(searchTerm, pageable);
    }
    
    // ✅ NEW: Combined filtering logic
    if (isAdmin) {
        // Handle combined filters
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            // Payment status + other filters
            if (projectId != null && !projectId.isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    // Project + Status + Payment
                    return purchaseOrderRepository.findByProjectIdAndStatusAndPaymentStatus(
                        projectId, status, paymentStatus, pageable
                    );
                }
                // Project + Payment
                return purchaseOrderRepository.findByProjectIdAndPaymentStatus(
                    projectId, paymentStatus, pageable
                );
            }
            
            if (subGroupName != null && !subGroupName.isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    // Group + SubGroup + Status + Payment
                    return purchaseOrderRepository.findByGroupSubGroupStatusAndPayment(
                        groupName, subGroupName, status, paymentStatus, pageable
                    );
                }
                // Group + SubGroup + Payment
                return purchaseOrderRepository.findByGroupSubGroupAndPayment(
                    groupName, subGroupName, paymentStatus, pageable
                );
            }
            
            if (groupName != null && !groupName.isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    // Group + Status + Payment
                    return purchaseOrderRepository.findByGroupStatusAndPayment(
                        groupName, status, paymentStatus, pageable
                    );
                }
                // Group + Payment
                return purchaseOrderRepository.findByGroupAndPayment(
                    groupName, paymentStatus, pageable
                );
            }
            
            if (status != null && !status.isEmpty()) {
                // Status + Payment only
                return purchaseOrderRepository.findByStatusAndPaymentStatus(
                    status, paymentStatus, pageable
                );
            }
            
            // Payment status only
            return purchaseOrderRepository.findByPaymentStatus(paymentStatus, pageable);
        }
        
        // Existing logic for other filters (without payment status)
        if (projectId != null && !projectId.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                return purchaseOrderRepository.findByProjectIdAndStatus(projectId, status, pageable);
            }
            return purchaseOrderRepository.findByProjectId(projectId, pageable);
        }
        
        if (subGroupName != null && !subGroupName.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                return purchaseOrderRepository.findByGroupSubGroupAndStatus(
                    groupName, subGroupName, status, pageable
                );
            }
            return purchaseOrderRepository.findByGroupAndSubGroup(groupName, subGroupName, pageable);
        }
        
        if (groupName != null && !groupName.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                return purchaseOrderRepository.findByGroupAndStatus(groupName, status, pageable);
            }
            return purchaseOrderRepository.findByGroupName(groupName, pageable);
        }
        
        if (status != null && !status.isEmpty()) {
            return purchaseOrderRepository.findByStatus(status, pageable);
        }
        
        return purchaseOrderRepository.findAllActive(pageable);
        
    } else {
        // Non-admin with user access filtering
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            if (projectId != null && !projectId.isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    return purchaseOrderRepository.findByProjectIdStatusPaymentAndUserAccess(
                        projectId, status, paymentStatus, userId, pageable
                    );
                }
                return purchaseOrderRepository.findByProjectIdPaymentAndUserAccess(
                    projectId, paymentStatus, userId, pageable
                );
            }
            
            if (subGroupName != null && !subGroupName.isEmpty()) {
                return purchaseOrderRepository.findByGroupSubGroupPaymentAndUserAccess(
                    groupName, subGroupName, paymentStatus, userId, pageable
                );
            }
            
            if (groupName != null && !groupName.isEmpty()) {
                return purchaseOrderRepository.findByGroupPaymentAndUserAccess(
                    groupName, paymentStatus, userId, pageable
                );
            }
            
            return purchaseOrderRepository.findByPaymentStatusAndUserAccess(
                paymentStatus, userId, pageable
            );
        }
        
        // Existing non-admin logic (without payment status)
        if (projectId != null && !projectId.isEmpty()) {
            return purchaseOrderRepository.findByProjectIdAndUserAccess(projectId, userId, pageable);
        }
        if (subGroupName != null && !subGroupName.isEmpty()) {
            return purchaseOrderRepository.findByGroupSubGroupAndUserAccess(
                groupName, subGroupName, userId, pageable
            );
        }
        if (groupName != null && !groupName.isEmpty()) {
            return purchaseOrderRepository.findByGroupNameAndUserAccess(groupName, userId, pageable);
        }
        return purchaseOrderRepository.findByUserAccess(userId, pageable);
    }
}
    
    /**
     * Get PO by ID
     */
    @Transactional(readOnly = true)
    public PurchaseOrderEntity getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + id));
    }
    
    /**
     * ✅ UPDATED: Create PO from quotation with custom data
     * Creates vendor IMMEDIATELY if new vendor, links vendorId right away
     */
    @Transactional
    public PurchaseOrderEntity createPOFromQuotationWithCustomData(
            Long quotationId,
            Long userId,
            Long vendorId,
            String vendorName,
            String vendorContact,
            String groupName,
            String subGroupName,
            String projectId,
            String rfqId,
            String orderDateStr,
            String expectedDeliveryStr,
            String paymentTerms,
            String shippingAddress,
            String notes,
            List<Map<String, Object>> itemsData
    ) {
        try {
            log.info("Creating PO from quotation {} with custom data", quotationId);
            
            // Get quotation
            QuotationEntity quotation = quotationRepository.findById(quotationId)
                    .orElseThrow(() -> new RuntimeException("Quotation not found"));
            
            if (!"Approved".equals(quotation.getStatus())) {
                throw new RuntimeException("Only approved quotations can be converted to PO");
            }
            
            // ✅ CREATE VENDOR IMMEDIATELY if new vendor
            Long finalVendorId = vendorId;
            if (vendorId == null && vendorName != null && !vendorName.trim().isEmpty()) {
                log.info("Creating new vendor immediately: {}", vendorName);
                finalVendorId = createVendorNow(vendorName, vendorContact, groupName, subGroupName, projectId, userId);
                log.info("✅ Created vendor with ID: {}", finalVendorId);
            }
            
            // Parse dates
            LocalDate orderDate = LocalDate.parse(orderDateStr);
            LocalDate expectedDelivery = LocalDate.parse(expectedDeliveryStr);
            
            // Create PO WITHOUT items first
            PurchaseOrderEntity po = PurchaseOrderEntity.builder()
                    .poNo(generatePONumber())
                    .vendorId(finalVendorId) // ✅ Use the newly created vendorId
                    .vendorName(vendorName)
                    .vendorContact(vendorContact)
                    .quotationId(quotationId)
                    .rfqId(rfqId != null ? rfqId : quotation.getRfqId())
                    .orderDate(orderDate.atStartOfDay())
                    .expectedDelivery(expectedDelivery.atStartOfDay())
                    .status("Draft")
                    .paymentStatus("Pending")
                    .groupName(groupName != null ? groupName : quotation.getGroupName())
                    .subGroupName(subGroupName != null ? subGroupName : quotation.getSubGroupName())
                    .projectId(projectId != null ? projectId : quotation.getProjectId())
                    .deliveryAddress(shippingAddress)
                    .paymentTerms(paymentTerms)
                    .notes(notes)
                    .category(quotation.getCategory())
                    .createdBy(userId)
                    .totalItemsOrdered(0)
                    .totalItemsDelivered(0)
                    .build();
            
            // Save PO FIRST to get ID
            PurchaseOrderEntity savedPO = purchaseOrderRepository.save(po);
            log.info("Saved PO with ID: {} and vendorId: {}", savedPO.getId(), savedPO.getVendorId());
            
            // Create PO items
            BigDecimal totalValue = BigDecimal.ZERO;
            int totalItemsOrdered = 0;
            List<PurchaseOrderItemEntity> poItems = new ArrayList<>();
            
            for (int i = 0; i < itemsData.size(); i++) {
                Map<String, Object> itemData = itemsData.get(i);
                
                BigDecimal quantity = new BigDecimal(itemData.get("quantity").toString());
                BigDecimal unitPrice = new BigDecimal(itemData.get("unitPrice").toString());
                BigDecimal gst = new BigDecimal(itemData.get("gst").toString());
                BigDecimal discount = itemData.containsKey("discount") 
                    ? new BigDecimal(itemData.get("discount").toString()) 
                    : BigDecimal.ZERO;
                
                // Calculate line total
                BigDecimal lineSubtotal = quantity.multiply(unitPrice);
                BigDecimal discountAmount = lineSubtotal.multiply(discount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal taxableAmount = lineSubtotal.subtract(discountAmount);
                BigDecimal gstAmount = taxableAmount.multiply(gst)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal lineTotal = taxableAmount.add(gstAmount);
                
                PurchaseOrderItemEntity poItem = PurchaseOrderItemEntity.builder()
                        .purchaseOrder(savedPO)
                        .lineNo(i + 1)
                        .itemName(itemData.get("itemName").toString())
                        .description(itemData.containsKey("itemDescription") 
                            ? itemData.get("itemDescription").toString() 
                            : "")
                        .quantity(quantity)
                        .deliveredQty(BigDecimal.ZERO)
                        .unitPrice(unitPrice)
                        .taxPercent(gst)
                        .build();
                
                poItems.add(poItem);
                totalValue = totalValue.add(lineTotal);
                totalItemsOrdered += quantity.intValue();
            }
            
            // Save all items
            List<PurchaseOrderItemEntity> savedItems = purchaseOrderItemRepository.saveAll(poItems);
            log.info("Saved {} PO items", savedItems.size());
            
            // Update PO totals
            savedPO.setTotalValue(totalValue.setScale(2, RoundingMode.HALF_UP));
            savedPO.setTotalItemsOrdered(totalItemsOrdered);
            savedPO.setTotalItemsDelivered(0);
            savedPO.setItems(savedItems);
            purchaseOrderRepository.save(savedPO);
            
            log.info("✅ Created PO {} with vendorId {} from quotation with {} items, total: {}", 
                savedPO.getPoNo(), savedPO.getVendorId(), itemsData.size(), totalValue);
            
            return savedPO;
            
        } catch (Exception e) {
            log.error("Error creating PO from quotation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PO: " + e.getMessage());
        }
    }

    /**
     * ✅ UPDATED: Create PO from order books (no quotation)
     * Creates vendor IMMEDIATELY if new vendor
     */
    @Transactional
    public PurchaseOrderEntity createPOFromOrderBooks(
            Long userId,
            Long vendorId,
            String vendorName,
            String vendorContact,
            String groupName,
            String subGroupName,
            String projectId,
            String orderDateStr,
            String expectedDeliveryStr,
            String paymentTerms,
            String shippingAddress,
            String notes,
            List<Map<String, Object>> itemsData
    ) {
        try {
            log.info("Creating PO from order books for project {}", projectId);
            
            // ✅ CREATE VENDOR IMMEDIATELY if new vendor
            Long finalVendorId = vendorId;
            if (vendorId == null && vendorName != null && !vendorName.trim().isEmpty()) {
                log.info("Creating new vendor immediately: {}", vendorName);
                finalVendorId = createVendorNow(vendorName, vendorContact, groupName, subGroupName, projectId, userId);
                log.info("✅ Created vendor with ID: {}", finalVendorId);
            }
            
            // Parse dates
            LocalDate orderDate = LocalDate.parse(orderDateStr);
            LocalDate expectedDelivery = LocalDate.parse(expectedDeliveryStr);
            
            // Create PO WITHOUT items first
            PurchaseOrderEntity po = PurchaseOrderEntity.builder()
                    .poNo(generatePONumber())
                    .vendorId(finalVendorId) // ✅ Use the newly created vendorId
                    .vendorName(vendorName)
                    .vendorContact(vendorContact)
                    .quotationId(null) // No quotation
                    .rfqId(null)
                    .orderDate(orderDate.atStartOfDay())
                    .expectedDelivery(expectedDelivery.atStartOfDay())
                    .status("Draft")
                    .paymentStatus("Pending")
                    .groupName(groupName)
                    .subGroupName(subGroupName)
                    .projectId(projectId)
                    .deliveryAddress(shippingAddress)
                    .paymentTerms(paymentTerms)
                    .notes(notes)
                    .createdBy(userId)
                    .totalItemsOrdered(0)
                    .totalItemsDelivered(0)
                    .build();
            
            // Save PO FIRST
            PurchaseOrderEntity savedPO = purchaseOrderRepository.save(po);
            log.info("Saved PO from order books with ID: {} and vendorId: {}", savedPO.getId(), savedPO.getVendorId());
            
            // Create PO items
            BigDecimal totalValue = BigDecimal.ZERO;
            int totalItemsOrdered = 0;
            List<PurchaseOrderItemEntity> poItems = new ArrayList<>();
            
            for (int i = 0; i < itemsData.size(); i++) {
                Map<String, Object> itemData = itemsData.get(i);
                
                BigDecimal quantity = new BigDecimal(itemData.get("quantity").toString());
                BigDecimal unitPrice = new BigDecimal(itemData.get("unitPrice").toString());
                BigDecimal gst = new BigDecimal(itemData.get("gst").toString());
                BigDecimal discount = itemData.containsKey("discount") 
                    ? new BigDecimal(itemData.get("discount").toString()) 
                    : BigDecimal.ZERO;
                
                // Calculate line total
                BigDecimal lineSubtotal = quantity.multiply(unitPrice);
                BigDecimal discountAmount = lineSubtotal.multiply(discount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal taxableAmount = lineSubtotal.subtract(discountAmount);
                BigDecimal gstAmount = taxableAmount.multiply(gst)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal lineTotal = taxableAmount.add(gstAmount);
                
                PurchaseOrderItemEntity poItem = PurchaseOrderItemEntity.builder()
                        .purchaseOrder(savedPO)
                        .lineNo(i + 1)
                        .itemName(itemData.get("itemName").toString())
                        .description(itemData.containsKey("itemDescription") 
                            ? itemData.get("itemDescription").toString() 
                            : "")
                        .quantity(quantity)
                        .deliveredQty(BigDecimal.ZERO)
                        .unitPrice(unitPrice)
                        .taxPercent(gst)
                        .build();
                
                poItems.add(poItem);
                totalValue = totalValue.add(lineTotal);
                totalItemsOrdered += quantity.intValue();
            }
            
            // Save all items
            List<PurchaseOrderItemEntity> savedItems = purchaseOrderItemRepository.saveAll(poItems);
            log.info("Saved {} PO items from order books", savedItems.size());
            
            // Update PO totals
            savedPO.setTotalValue(totalValue.setScale(2, RoundingMode.HALF_UP));
            savedPO.setTotalItemsOrdered(totalItemsOrdered);
            savedPO.setTotalItemsDelivered(0);
            savedPO.setItems(savedItems);
            purchaseOrderRepository.save(savedPO);
            
            log.info("✅ Created PO {} with vendorId {} from order books with {} items, total: {}", 
                savedPO.getPoNo(), savedPO.getVendorId(), itemsData.size(), totalValue);
            
            return savedPO;
            
        } catch (Exception e) {
            log.error("Error creating PO from order books: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PO: " + e.getMessage());
        }
    }

    /**
     * ✅ NEW METHOD: Create vendor immediately and return its ID
     */
    private Long createVendorNow(
            String vendorName,
            String vendorContact,
            String groupName,
            String subGroupName,
            String projectId,
            Long userId
    ) {
        try {
            // Check if vendor already exists with this contact
            Optional<VendorEntity> existingVendor = vendorRepository.findByPhone(vendorContact);
            if (existingVendor.isPresent()) {
                log.info("Vendor already exists with contact {}, returning existing vendor ID: {}", 
                    vendorContact, existingVendor.get().getId());
                return existingVendor.get().getId();
            }
            
            // Handle null project_id
            String finalProjectId = projectId;
            if (projectId == null || projectId.trim().isEmpty()) {
                finalProjectId = "DEFAULT";
            }
            
            VendorEntity vendor = VendorEntity.builder()
                    .name(vendorName)
                    .email("vendor_" + System.currentTimeMillis() + "@temp.com")
                    .phone(vendorContact)
                    
                    .rating(0)
                    .status("Active")
                    .groupName(groupName != null ? groupName : "Others")
                    .subGroupName(subGroupName != null ? subGroupName : "General")
                    .projectId(finalProjectId)
                    .category("General")
                    .totalOrders(0)
                    .totalPurchaseValue(BigDecimal.ZERO)
                    .createdBy(userId)
                    .build();
            
            VendorEntity savedVendor = vendorRepository.save(vendor);
            log.info("✅ Created new vendor {} with ID: {}", vendorName, savedVendor.getId());
            
            return savedVendor.getId();
            
        } catch (Exception e) {
            log.error("Error creating vendor immediately: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create vendor: " + e.getMessage());
        }
    }
    
    /**
     * Update PO status - REMOVED vendor creation logic (now done at PO creation)
     */
    @Transactional
    public PurchaseOrderEntity updateStatus(Long id, String newStatus, Long userId) {
        PurchaseOrderEntity po = getPurchaseOrderById(id);
        
        String oldStatus = po.getStatus();
        po.setStatus(newStatus);
        
        if ("Approved".equals(newStatus)) {
            po.setApprovedBy(userId);
        }
        
        if ("Delivered".equals(newStatus) && !"Delivered".equals(oldStatus)) {
            updateVendorStatsAfterDelivery(po);
        }
        
        log.info("Updated PO {} status from {} to {}", po.getPoNo(), oldStatus, newStatus);
        return purchaseOrderRepository.save(po);
    }
    
    /**
     * Soft delete PO
     */
    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrderEntity po = getPurchaseOrderById(id);
        po.setDeletedAt(LocalDateTime.now());
        po.setStatus("Cancelled");
        purchaseOrderRepository.save(po);
        
        log.info("Soft deleted PO: {}", po.getPoNo());
    }
    
    /**
     * Get POs by vendor
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderEntity> getPurchaseOrdersByVendor(Long vendorId) {
        return purchaseOrderRepository.findByVendorIdOrderByOrderDateDesc(vendorId);
    }

    /**
     * Get POs by vendor (supports both vendorId and vendorName)
     */
    public List<Map<String, Object>> getPurchaseOrdersByVendor1(
            Long vendorId,
            String vendorName,
            String groupName,
            String subGroupName,
            String projectId
    ) {
        List<PurchaseOrderEntity> pos = new ArrayList<>();
        
        // Find POs by vendorId OR vendorName
        if (vendorId != null) {
            if (projectId != null && !projectId.isEmpty()) {
                pos = purchaseOrderRepository.findByVendorIdAndProjectId(vendorId, projectId);
            } else {
                pos = purchaseOrderRepository.findByVendorIdOrderByOrderDateDesc(vendorId);
            }
        } else if (vendorName != null && !vendorName.trim().isEmpty()) {
            if (projectId != null && !projectId.isEmpty()) {
                pos = purchaseOrderRepository.findByVendorNameAndProjectId(vendorName, projectId);
            } else {
                pos = purchaseOrderRepository.findByVendorNameOrderByOrderDateDesc(vendorName);
            }
        }
        
        // Convert to map
        return pos.stream()
            .filter(po -> po.getDeletedAt() == null)
            .map(po -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", po.getId());
                map.put("poNo", po.getPoNo());
                map.put("vendorId", po.getVendorId());
                map.put("vendorName", po.getVendorDisplayName());
                map.put("orderDate", po.getOrderDate());
                map.put("totalValue", po.getTotalValue());
                map.put("status", po.getStatus());
                return map;
            })
            .collect(Collectors.toList());
    }

    /**
     * Mark item as delivered
     */
    @Transactional
    public PurchaseOrderEntity markItemDelivered(Long poId, Long itemId, BigDecimal deliveredQty) {
        PurchaseOrderItemEntity item = purchaseOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("PO Item not found"));
        
        if (!item.getPurchaseOrder().getId().equals(poId)) {
            throw new RuntimeException("Item does not belong to this PO");
        }
        
        // Update delivered quantity
        BigDecimal newDelivered = item.getDeliveredQty().add(deliveredQty);
        if (newDelivered.compareTo(item.getQuantity()) > 0) {
            throw new RuntimeException("Delivered quantity exceeds ordered quantity");
        }
        
        item.setDeliveredQty(newDelivered);
        purchaseOrderItemRepository.save(item);
        
        // Recalculate PO totals
        PurchaseOrderEntity po = getPurchaseOrderById(poId);
        recalculateTotals(po);
        
        // If all items delivered, change status and update vendor stats
        if (po.getTotalItemsPending() != null && po.getTotalItemsPending() == 0) {
            po.setStatus("Delivered");
            updateVendorStatsAfterDelivery(po);
        }
        
        return purchaseOrderRepository.save(po);
    }
    
    /**
     * Update PO
     */
    @Transactional
    public PurchaseOrderEntity updatePurchaseOrder(Long id, PurchaseOrderEntity updatedPO) {
        PurchaseOrderEntity existing = getPurchaseOrderById(id);
        
        // Update fields
        existing.setExpectedDelivery(updatedPO.getExpectedDelivery());
        existing.setDeliveryAddress(updatedPO.getDeliveryAddress());
        existing.setDeliveryTerms(updatedPO.getDeliveryTerms());
        existing.setPaymentTerms(updatedPO.getPaymentTerms());
        existing.setTrackingNumber(updatedPO.getTrackingNumber());
        existing.setNotes(updatedPO.getNotes());
        
        return purchaseOrderRepository.save(existing);
    }
    
    /**
     * Create PO manually (without quotation)
     */
    @Transactional
    public PurchaseOrderEntity createPurchaseOrder(PurchaseOrderEntity po, Long userId) {
        po.setPoNo(generatePONumber());
        po.setCreatedBy(userId);
        po.setOrderDate(LocalDateTime.now());
        po.setStatus("Draft");
        po.setPaymentStatus("Pending");
        
        // Calculate totals from items
        if (po.getItems() != null && !po.getItems().isEmpty()) {
            int totalOrdered = po.getItems().stream()
                    .mapToInt(item -> item.getQuantity().intValue())
                    .sum();
            po.setTotalItemsOrdered(totalOrdered);
            po.setTotalItemsDelivered(0);
            
            BigDecimal totalValue = po.getItems().stream()
                    .map(item -> item.getQuantity().multiply(item.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            po.setTotalValue(totalValue);
        }
        
        PurchaseOrderEntity savedPO = purchaseOrderRepository.save(po);
        
        // Save items
        if (po.getItems() != null) {
            for (int i = 0; i < po.getItems().size(); i++) {
                PurchaseOrderItemEntity item = po.getItems().get(i);
                item.setPurchaseOrder(savedPO);
                item.setLineNo(i + 1);
            }
            purchaseOrderItemRepository.saveAll(po.getItems());
        }
        
        log.info("Created manual PO: {}", savedPO.getPoNo());
        return savedPO;
    }
    
    /**
     * Create PO from approved quotation
     */
    @Transactional
    public PurchaseOrderEntity createFromQuotation(Long quotationId, Long userId) {
        QuotationEntity quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        if (!"Approved".equals(quotation.getStatus())) {
            throw new RuntimeException("Only approved quotations can be converted to PO");
        }
        
        List<QuotationItemEntity> quotationItems = quotationItemRepository.findByQuotationId(quotationId);
        
        // Ensure vendor exists
        Long vendorId = quotation.getVendorId();
        if (vendorId == null) {
            vendorId = createVendorNow(
                "Vendor-" + quotation.getVendorContact(),
                quotation.getVendorContact(),
                quotation.getGroupName(),
                quotation.getSubGroupName(),
                quotation.getProjectId(),
                userId
            );
            quotation.setVendorId(vendorId);
            quotationRepository.save(quotation);
        }
        
        PurchaseOrderEntity po = PurchaseOrderEntity.builder()
                .poNo(generatePONumber())
                .vendorId(vendorId)
                .quotationId(quotationId)
                .rfqId(quotation.getRfqId())
                .orderDate(LocalDateTime.now())
                .expectedDelivery(quotation.getValidTill().atStartOfDay())
                .status("Draft")
                .paymentStatus("Pending")
                .totalValue(quotation.getTotalValue())
                .totalItemsOrdered(quotationItems.stream()
                        .mapToInt(item -> item.getQuantity().intValue())
                        .sum())
                .totalItemsDelivered(0)
                .groupName(quotation.getGroupName())
                .subGroupName(quotation.getSubGroupName())
                .projectId(quotation.getProjectId())
                .deliveryTerms(quotation.getDeliveryTime())
                .paymentTerms(quotation.getPaymentTerms())
                .notes(quotation.getNotes())
                .category(quotation.getCategory())
                .createdBy(userId)
                .build();
        
        PurchaseOrderEntity savedPO = purchaseOrderRepository.save(po);
        
        for (int i = 0; i < quotationItems.size(); i++) {
            QuotationItemEntity qItem = quotationItems.get(i);
            
            PurchaseOrderItemEntity poItem = PurchaseOrderItemEntity.builder()
                    .purchaseOrder(savedPO)
                    .lineNo(i + 1)
                    .itemName(qItem.getItemName())
                    .description(qItem.getDescription())
                    .quantity(qItem.getQuantity())
                    .deliveredQty(BigDecimal.ZERO)
                    .unitPrice(qItem.getUnitPrice())
                    .taxPercent(qItem.getTaxPercent())
                    .deliverySchedule(qItem.getDeliveryLeadTime())
                    .build();
            
            purchaseOrderItemRepository.save(poItem);
        }
        
        log.info("Created PO {} from quotation {}", savedPO.getPoNo(), quotation.getQuoteNo());
        return savedPO;
    }
    
    /**
     * Recalculate PO totals from items
     */
    private void recalculateTotals(PurchaseOrderEntity po) {
        Integer totalOrdered = purchaseOrderItemRepository.getTotalOrderedItems(po.getId());
        Integer totalDelivered = purchaseOrderItemRepository.getTotalDeliveredItems(po.getId());
        
        po.setTotalItemsOrdered(totalOrdered != null ? totalOrdered : 0);
        po.setTotalItemsDelivered(totalDelivered != null ? totalDelivered : 0);
    }
    
    /**
     * Get PO statistics
     */
    @Transactional(readOnly = true)
    public POStats getStatistics(String groupName, String subGroupName, String projectId, Long userId, String userRole) {
        boolean isAdmin = isAdmin(userRole);
        
        Page<PurchaseOrderEntity> allPOs = getPurchaseOrders(
                groupName, subGroupName, projectId, null, null,null,
                userId, userRole, 0, Integer.MAX_VALUE, "orderDate", "DESC"
        );
        
        List<PurchaseOrderEntity> pos = allPOs.getContent();
        
        long total = pos.size();
        long draft = pos.stream().filter(p -> "Draft".equals(p.getStatus())).count();
        long approved = pos.stream().filter(p -> "Approved".equals(p.getStatus())).count();
        long ordered = pos.stream().filter(p -> "Ordered".equals(p.getStatus())).count();
        long inTransit = pos.stream().filter(p -> "In-Transit".equals(p.getStatus())).count();
        long delivered = pos.stream().filter(p -> "Delivered".equals(p.getStatus())).count();
        long cancelled = pos.stream().filter(p -> "Cancelled".equals(p.getStatus())).count();
        
        Double totalValue = pos.stream()
                .map(PurchaseOrderEntity::getTotalValue)
                .filter(v -> v != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        
        return POStats.builder()
                .totalPOs(total)
                .draft(draft)
                .approved(approved)
                .ordered(ordered)
                .inTransit(inTransit)
                .delivered(delivered)
                .cancelled(cancelled)
                .totalValue(totalValue)
                .build();
    }
    
    // Helper methods
    
    private boolean isAdmin(String userRole) {
        return "ADMIN".equalsIgnoreCase(userRole) || "SUPERADMIN".equalsIgnoreCase(userRole);
    }
    
    private String generatePONumber() {
        long count = purchaseOrderRepository.countActivePOs();
        return String.format("PO-2024-%03d", count + 1);
    }
    
    /**
     * ✅ RENAMED: Update vendor stats after delivery (no longer creates vendor)
     */
    private void updateVendorStatsAfterDelivery(PurchaseOrderEntity po) {
        if (po.getVendorId() == null) {
            log.warn("Cannot update vendor stats - vendorId is null for PO: {}", po.getPoNo());
            return;
        }
        
        vendorRepository.findById(po.getVendorId()).ifPresent(vendor -> {
            vendor.setLastPurchaseAmount(po.getTotalValue());
            vendor.setLastPurchaseDate(LocalDateTime.now());
            
            BigDecimal currentTotal = vendor.getTotalPurchaseValue() != null
                    ? vendor.getTotalPurchaseValue()
                    : BigDecimal.ZERO;
            vendor.setTotalPurchaseValue(currentTotal.add(po.getTotalValue()));
            
            Integer currentOrders = vendor.getTotalOrders() != null ? vendor.getTotalOrders() : 0;
            vendor.setTotalOrders(currentOrders + 1);
            
            vendorRepository.save(vendor);
            log.info("Updated vendor {} stats after delivery", vendor.getName());
        });
    }
    
    @lombok.Data
    @lombok.Builder
    public static class POStats {
        private long totalPOs;
        private long draft;
        private long approved;
        private long ordered;
        private long inTransit;
        private long delivered;
        private long cancelled;
        private Double totalValue;
    }
    
    /**
     * Get POs for dropdown with filters
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderDropdownWrapper> getPurchaseOrdersForDropdown(
            String groupName, 
            String subGroupName, 
            String projectId
    ) {
        List<PurchaseOrderEntity> pos;
        
        // Fetch filtered or all POs
        if (groupName != null || subGroupName != null || projectId != null) {
            pos = purchaseOrderRepository.findAllForDropdownFiltered(groupName, subGroupName, projectId);
        } else {
            pos = purchaseOrderRepository.findAllForDropdown();
        }
        
        // Convert to dropdown wrapper
        return pos.stream()
                .map(this::convertToDropdownWrapper)
                .collect(Collectors.toList());
    }

    /**
     * Convert PO entity to dropdown wrapper
     */
    private PurchaseOrderDropdownWrapper convertToDropdownWrapper(PurchaseOrderEntity po) {
        String vendorName = "Unknown Vendor";
        
        // Get vendor name if vendor exists
        if (po.getVendorId() != null) {
            vendorName = vendorRepository.findById(po.getVendorId())
                    .map(VendorEntity::getName)
                    .orElse("Unknown Vendor");
        }
        
        return PurchaseOrderDropdownWrapper.builder()
                .id(po.getId())
                .poNo(po.getPoNo())
                .vendorName(vendorName)
                .status(po.getStatus())
                .projectId(po.getProjectId())
                .groupName(po.getGroupName())
                .subGroupName(po.getSubGroupName())
                .build();
    }

    /**
     * Get POs by vendor for dropdown
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderDropdownWrapper> getPurchaseOrdersByVendorForDropdown(Long vendorId) {
        List<PurchaseOrderEntity> pos = purchaseOrderRepository.findByVendorId(vendorId);
        
        return pos.stream()
                .map(this::convertToDropdownWrapper)
                .collect(Collectors.toList());
    }
}