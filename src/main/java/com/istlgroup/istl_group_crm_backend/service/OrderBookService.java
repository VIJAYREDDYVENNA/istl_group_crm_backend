package com.istlgroup.istl_group_crm_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.OrderBookEntity;
import com.istlgroup.istl_group_crm_backend.entity.OrderBookItemEntity;
import com.istlgroup.istl_group_crm_backend.entity.ProposalItemEntity;
import com.istlgroup.istl_group_crm_backend.entity.ProposalsEntity;
import com.istlgroup.istl_group_crm_backend.repo.OrderBookRepo;
import com.istlgroup.istl_group_crm_backend.repo.OrderBookItemRepo;
import com.istlgroup.istl_group_crm_backend.repo.ProposalItemRepo;
import com.istlgroup.istl_group_crm_backend.repo.CustomersRepo;
import com.istlgroup.istl_group_crm_backend.repo.ProposalsRepo;
import com.istlgroup.istl_group_crm_backend.repo.UsersRepo;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookItemWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookRequestWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookItemRequestWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.ProposalItemWrapper;

@Service
public class OrderBookService {
    
    @Autowired
    private OrderBookRepo orderBookRepo;
    
    @Autowired
    private OrderBookItemRepo orderBookItemRepo;
    
    @Autowired
    private ProposalItemRepo proposalItemRepo;
    
    @Autowired
    private CustomersRepo customersRepo;
    
    @Autowired
    private ProposalsRepo proposalsRepo;
    
    @Autowired
    private UsersRepo usersRepo;
    
    @Autowired
    private DropdownProjectService projectService; 
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get all order books with pagination
     */
    public Page<OrderBookWrapper> getAllOrderBooks(int page, int size, String groupName, String subGroupName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderBookEntity> orderBooks;
        
        if (groupName != null && !groupName.isEmpty()) {
            if (subGroupName != null && !subGroupName.isEmpty()) {
                orderBooks = orderBookRepo.searchOrderBooks(null, null, groupName, subGroupName, null, null, pageable);
            } else {
                orderBooks = orderBookRepo.searchOrderBooks(null, null, groupName, null, null, null, pageable);
            }
        } else {
            orderBooks = orderBookRepo.findByDeletedAtIsNull(pageable);
        }
        
        return orderBooks.map(this::convertToWrapper);
    }
    
    /**
     * Search and filter order books
     */
    public Page<OrderBookWrapper> searchOrderBooks(
            String searchTerm, String status, String groupName, String subGroupName,
            String fromDate, String toDate, int page, int size) {
        
        LocalDate from = null;
        LocalDate to = null;
        
        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate, DATE_FORMATTER);
        }
        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate, DATE_FORMATTER);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderBookEntity> results = orderBookRepo.searchOrderBooks(
            searchTerm, status, groupName, subGroupName, from, to, pageable
        );
        
        return results.map(this::convertToWrapper);
    }
    
    /**
     * Get order book by ID
     */
    public OrderBookWrapper getOrderBookById(Long id) throws CustomException {
        OrderBookEntity orderBook = orderBookRepo.findById(id)
            .orElseThrow(() -> new CustomException("Order book not found with ID: " + id));
        
        if (orderBook.getDeletedAt() != null) {
            throw new CustomException("Order book has been deleted");
        }
        
        return convertToWrapper(orderBook);
    }
    
    /**
     * Get order book items by order book ID
     */
    public List<OrderBookItemWrapper> getOrderBookItems(Long orderBookId) {
        List<OrderBookItemEntity> items = orderBookItemRepo.findByOrderBookIdWithCalculatedFields(orderBookId);
        return items.stream()
            .map(this::convertItemToWrapper)
            .collect(Collectors.toList());
    }
    
    /**
     * Get proposal items by proposal ID (for loading into order book)
     */
    public List<ProposalItemWrapper> getProposalItems(Long proposalId) throws CustomException {
        List<ProposalItemEntity> items = proposalItemRepo.findByProposalIdWithCalculatedFields(proposalId);
        return items.stream()
            .map(this::convertProposalItemToWrapper)
            .collect(Collectors.toList());
    }
    
    
    
    public List<Map<String, Object>> getProposalBomItems(Long proposalId) throws CustomException {
        // Find the proposal
        Optional<ProposalsEntity> proposalOpt = proposalsRepo.findById(proposalId);
        
        if (!proposalOpt.isPresent()) {
            throw new CustomException("Proposal not found with ID: " + proposalId);
        }
        
        ProposalsEntity proposal = proposalOpt.get();
        String bomItemsJson = proposal.getBomItems();
        
        // If no BOM items, return empty list
        if (bomItemsJson == null || bomItemsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Parse JSON string to List of Maps
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> bomItems = objectMapper.readValue(
                bomItemsJson, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            // Transform BOM items to OrderBook item format
            List<Map<String, Object>> orderBookItems = new ArrayList<>();
            int lineNo = 1;
            
            for (Map<String, Object> bomItem : bomItems) {
                Map<String, Object> orderItem = new HashMap<>();
                
                // Map BOM fields to OrderBook item fields
                orderItem.put("lineNo", lineNo++);
                orderItem.put("itemName", bomItem.getOrDefault("item", ""));
                orderItem.put("specification", bomItem.getOrDefault("specification", ""));
                orderItem.put("description", ""); // BOM doesn't have description
                orderItem.put("proposalItemId", null); // Not from proposal_items table
                
                // Parse quantity (could be string or number in JSON)
                Object quantityObj = bomItem.get("quantity");
                BigDecimal quantity = parseDecimal(quantityObj);
                orderItem.put("quantity", quantity);
                
                // Unit
                orderItem.put("unit", bomItem.getOrDefault("unit", "Nos"));
                
                // Parse unit price (stored as "rate" in BOM)
                Object rateObj = bomItem.get("rate");
                BigDecimal unitPrice = parseDecimal(rateObj);
                orderItem.put("unitPrice", unitPrice);
                
                // Tax and discount (not in BOM, set defaults)
                Object tax = bomItem.get("tax");
                orderItem.put("taxPercent", tax);
                orderItem.put("discountPercent", BigDecimal.ZERO);
                
                orderItem.put("itemRemarks", "");
                
                orderBookItems.add(orderItem);
            }
            
            return orderBookItems;
            
        } catch (Exception e) {
            throw new CustomException("Error parsing BOM items JSON: " + e.getMessage());
        }
    }

    /**
     * Helper method to parse decimal values from Object (handles both String and Number)
     */
    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        if (value instanceof String) {
            try {
                String strValue = ((String) value).trim();
                if (strValue.isEmpty()) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(strValue);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Create order book
     */
    @Transactional
    public OrderBookWrapper createOrderBook(OrderBookRequestWrapper request, Long createdBy) throws CustomException {
        // Validate customer exists
        if (!customersRepo.existsById(request.getCustomerId())) {
            throw new CustomException("Customer not found");
        }
        
        // Generate order book number
        String orderBookNo = generateOrderBookNo();
        
        OrderBookEntity orderBook = new OrderBookEntity();
        orderBook.setOrderBookNo(orderBookNo);
        orderBook.setCustomerId(request.getCustomerId());
        orderBook.setProposalId(request.getProposalId());
        orderBook.setLeadId(request.getLeadId());
        orderBook.setGroupName(request.getGroupName());
        orderBook.setSubGroupName(request.getSubGroupName());
        orderBook.setOrderTitle(request.getOrderTitle());
        orderBook.setOrderDescription(request.getOrderDescription());
        orderBook.setOrderDate(LocalDate.parse(request.getOrderDate(), DATE_FORMATTER));
        
        if (request.getExpectedDeliveryDate() != null && !request.getExpectedDeliveryDate().isEmpty()) {
            orderBook.setExpectedDeliveryDate(LocalDate.parse(request.getExpectedDeliveryDate(), DATE_FORMATTER));
        }
        
        orderBook.setPoNumber(request.getPoNumber());
        if (request.getPoDate() != null && !request.getPoDate().isEmpty()) {
            orderBook.setPoDate(LocalDate.parse(request.getPoDate(), DATE_FORMATTER));
        }
        
        orderBook.setAdvanceAmount(request.getAdvanceAmount() != null ? request.getAdvanceAmount() : BigDecimal.ZERO);
        orderBook.setStatus(request.getStatus() != null ? request.getStatus() : "Draft");
        orderBook.setRemarks(request.getRemarks());
        orderBook.setCreatedBy(createdBy);
        
        String projectId = projectService.getProjectIdByCustomerid(request.getCustomerId());
        
        orderBook.setProjectId(projectId);
        System.err.println(projectId);
        
        OrderBookEntity savedOrderBook = orderBookRepo.save(orderBook);
        
        // Save items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            saveOrderBookItems(savedOrderBook.getId(), request.getItems());
            
            // Calculate totals
            calculateAndUpdateTotals(savedOrderBook.getId());
        }
        
        return convertToWrapper(orderBookRepo.findById(savedOrderBook.getId()).get());
    }
    
    /**
     * Update order book
     */
    @Transactional
    public OrderBookWrapper updateOrderBook(Long id, OrderBookRequestWrapper request, Long userId) throws CustomException {
        OrderBookEntity orderBook = orderBookRepo.findById(id)
            .orElseThrow(() -> new CustomException("Order book not found with ID: " + id));
        
        if (orderBook.getDeletedAt() != null) {
            throw new CustomException("Cannot update deleted order book");
        }
        
        // Update fields
        if (request.getOrderTitle() != null) {
            orderBook.setOrderTitle(request.getOrderTitle());
        }
        if (request.getOrderDescription() != null) {
            orderBook.setOrderDescription(request.getOrderDescription());
        }
        if (request.getOrderDate() != null) {
            orderBook.setOrderDate(LocalDate.parse(request.getOrderDate(), DATE_FORMATTER));
        }
        if (request.getExpectedDeliveryDate() != null && !request.getExpectedDeliveryDate().isEmpty()) {
            orderBook.setExpectedDeliveryDate(LocalDate.parse(request.getExpectedDeliveryDate(), DATE_FORMATTER));
        }
        if (request.getPoNumber() != null) {
            orderBook.setPoNumber(request.getPoNumber());
        }
        if (request.getPoDate() != null && !request.getPoDate().isEmpty()) {
            orderBook.setPoDate(LocalDate.parse(request.getPoDate(), DATE_FORMATTER));
        }
        if (request.getAdvanceAmount() != null) {
            orderBook.setAdvanceAmount(request.getAdvanceAmount());
        }
        if (request.getStatus() != null) {
            orderBook.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            orderBook.setRemarks(request.getRemarks());
        }
        String projectId = projectService.getProjectIdByCustomerid(request.getCustomerId());
        orderBook.setProjectId(projectId);
        System.err.println(projectId);

        // Update items if provided
        if (request.getItems() != null) {
            // Delete existing items
            orderBookItemRepo.deleteByOrderBookId(id);
            
            // Save new items
            saveOrderBookItems(id, request.getItems());
            
            // Recalculate totals
            calculateAndUpdateTotals(id);
        }
        
        OrderBookEntity updated = orderBookRepo.save(orderBook);
        return convertToWrapper(updated);
    }
    
    /**
     * Upload PO file
     */
    @Transactional
    public OrderBookWrapper uploadPOFile(Long id, MultipartFile file, String poNumber, String poDate) throws CustomException {
        OrderBookEntity orderBook = orderBookRepo.findById(id)
            .orElseThrow(() -> new CustomException("Order book not found with ID: " + id));
        
        if (orderBook.getDeletedAt() != null) {
            throw new CustomException("Cannot update deleted order book");
        }
        
        try {
            // Save file logic here - implement based on your file storage system
            // For now, we'll just store the filename
            String fileName = file.getOriginalFilename();
            String filePath = "/uploads/po/" + orderBook.getOrderBookNo() + "_" + fileName;
            
            // TODO: Implement actual file saving logic
            // Example: Files.copy(file.getInputStream(), Paths.get(uploadDir + filePath));
            
            orderBook.setPoFileName(fileName);
            orderBook.setPoFilePath(filePath);
            orderBook.setPoNumber(poNumber);
            
            if (poDate != null && !poDate.isEmpty()) {
                orderBook.setPoDate(LocalDate.parse(poDate, DATE_FORMATTER));
            }
            
            OrderBookEntity updated = orderBookRepo.save(orderBook);
            return convertToWrapper(updated);
            
        } catch (Exception e) {
            throw new CustomException("Failed to upload PO file: " + e.getMessage());
        }
    }
    
    /**
     * Delete order book (soft delete)
     */
    @Transactional
    public void deleteOrderBook(Long id) throws CustomException {
        OrderBookEntity orderBook = orderBookRepo.findById(id)
            .orElseThrow(() -> new CustomException("Order book not found with ID: " + id));
        
        if (orderBook.getDeletedAt() != null) {
            throw new CustomException("Order book already deleted");
        }
        
        orderBook.setDeletedAt(LocalDateTime.now());
        orderBookRepo.save(orderBook);
    }
    
    /**
     * Save order book items
     */
    private void saveOrderBookItems(Long orderBookId, List<OrderBookItemRequestWrapper> itemRequests) {
        int lineNo = 1;
        for (OrderBookItemRequestWrapper itemRequest : itemRequests) {
            OrderBookItemEntity item = new OrderBookItemEntity();
            item.setOrderBookId(orderBookId);
            item.setLineNo(itemRequest.getLineNo() != null ? itemRequest.getLineNo() : lineNo++);
            item.setItemName(itemRequest.getItemName());
            item.setSpecification(itemRequest.getSpecification());
            item.setDescription(itemRequest.getDescription());
            item.setProposalItemId(itemRequest.getProposalItemId());
            item.setQuantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : BigDecimal.ONE);
            item.setUnit(itemRequest.getUnit() != null ? itemRequest.getUnit() : "Nos");
            item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : BigDecimal.ZERO);
            item.setTaxPercent(itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO);
            item.setDiscountPercent(itemRequest.getDiscountPercent() != null ? itemRequest.getDiscountPercent() : BigDecimal.ZERO);
            item.setItemRemarks(itemRequest.getItemRemarks());
            
            orderBookItemRepo.save(item);
        }
    }
    
    /**
     * Calculate and update order book totals
     */
    private void calculateAndUpdateTotals(Long orderBookId) {
        List<OrderBookItemEntity> items = orderBookItemRepo.findByOrderBookIdWithCalculatedFields(orderBookId);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;
        
        for (OrderBookItemEntity item : items) {
            BigDecimal itemSubtotal = item.getQuantity().multiply(item.getUnitPrice());
            BigDecimal itemDiscount = itemSubtotal.multiply(item.getDiscountPercent()).divide(new BigDecimal("100"));
            BigDecimal itemTaxable = itemSubtotal.subtract(itemDiscount);
            BigDecimal itemTax = itemTaxable.multiply(item.getTaxPercent()).divide(new BigDecimal("100"));
            BigDecimal itemTotal = itemTaxable.add(itemTax);
            
            subtotal = subtotal.add(itemSubtotal);
            totalTax = totalTax.add(itemTax);
            grandTotal = grandTotal.add(itemTotal);
        }
        
        OrderBookEntity orderBook = orderBookRepo.findById(orderBookId).get();
        orderBook.setSubtotal(subtotal);
        orderBook.setTaxAmount(totalTax);
        orderBook.setTotalAmount(grandTotal);
        orderBook.setBalanceAmount(grandTotal.subtract(orderBook.getAdvanceAmount() != null ? orderBook.getAdvanceAmount() : BigDecimal.ZERO));
        
        orderBookRepo.save(orderBook);
    }
    
    /**
     * Generate unique order book number
     */
    private String generateOrderBookNo() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String prefix = "ORD-" + year + "-";
        
        long count = orderBookRepo.countByOrderBookNoStartingWith(prefix);
        long nextSequence = count + 1;
        
        return String.format("%s%04d", prefix, nextSequence);
    }
    
    /**
     * Convert Entity to Wrapper
     */
    private OrderBookWrapper convertToWrapper(OrderBookEntity entity) {
        OrderBookWrapper wrapper = new OrderBookWrapper();
        wrapper.setId(entity.getId());
        wrapper.setOrderBookNo(entity.getOrderBookNo());
        wrapper.setCustomerId(entity.getCustomerId());
        wrapper.setProposalId(entity.getProposalId());
        wrapper.setLeadId(entity.getLeadId());
        wrapper.setGroupName(entity.getGroupName());
        wrapper.setSubGroupName(entity.getSubGroupName());
        wrapper.setOrderTitle(entity.getOrderTitle());
        wrapper.setOrderDescription(entity.getOrderDescription());
        wrapper.setOrderDate(entity.getOrderDate() != null ? entity.getOrderDate().toString() : null);
        wrapper.setExpectedDeliveryDate(entity.getExpectedDeliveryDate() != null ? entity.getExpectedDeliveryDate().toString() : null);
        wrapper.setPoNumber(entity.getPoNumber());
        wrapper.setPoDate(entity.getPoDate() != null ? entity.getPoDate().toString() : null);
        wrapper.setPoFilePath(entity.getPoFilePath());
        wrapper.setPoFileName(entity.getPoFileName());
        wrapper.setSubtotal(entity.getSubtotal());
        wrapper.setTaxAmount(entity.getTaxAmount());
        wrapper.setTotalAmount(entity.getTotalAmount());
        wrapper.setAdvanceAmount(entity.getAdvanceAmount());
        wrapper.setBalanceAmount(entity.getBalanceAmount());
        wrapper.setStatus(entity.getStatus());
        wrapper.setRemarks(entity.getRemarks());
        wrapper.setCreatedBy(entity.getCreatedBy());
        wrapper.setApprovedBy(entity.getApprovedBy());
        wrapper.setApprovedAt(entity.getApprovedAt() != null ? entity.getApprovedAt().toString() : null);
        wrapper.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        wrapper.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        
        // Fetch related data
        if (entity.getCustomerId() != null) {
            customersRepo.findById(entity.getCustomerId()).ifPresent(customer -> {
                wrapper.setCustomerCode(customer.getCustomerCode());
                wrapper.setCustomerName(customer.getName());
            });
        }
        
        if (entity.getProposalId() != null) {
            proposalsRepo.findById(entity.getProposalId()).ifPresent(proposal -> {
                wrapper.setProposalNo(proposal.getProposalNo());
            });
        }
        
        if (entity.getCreatedBy() != null) {
            usersRepo.findById(entity.getCreatedBy()).ifPresent(user -> 
                wrapper.setCreatedByName(user.getName())
            );
        }
        
        if (entity.getApprovedBy() != null) {
            usersRepo.findById(entity.getApprovedBy()).ifPresent(user -> 
                wrapper.setApprovedByName(user.getName())
            );
        }
        
        return wrapper;
    }
    
    /**
     * Convert Item Entity to Wrapper
     */
    private OrderBookItemWrapper convertItemToWrapper(OrderBookItemEntity entity) {
        OrderBookItemWrapper wrapper = new OrderBookItemWrapper();
        wrapper.setId(entity.getId());
        wrapper.setOrderBookId(entity.getOrderBookId());
        wrapper.setLineNo(entity.getLineNo());
        wrapper.setItemName(entity.getItemName());
        wrapper.setSpecification(entity.getSpecification());
        wrapper.setDescription(entity.getDescription());
        wrapper.setProposalItemId(entity.getProposalItemId());
        wrapper.setQuantity(entity.getQuantity());
        wrapper.setUnit(entity.getUnit());
        wrapper.setUnitPrice(entity.getUnitPrice());
        wrapper.setTaxPercent(entity.getTaxPercent());
        wrapper.setDiscountPercent(entity.getDiscountPercent());
        wrapper.setItemRemarks(entity.getItemRemarks());
        
        // Calculated fields
        wrapper.setLineSubtotal(entity.getLineSubtotal());
        wrapper.setDiscountAmount(entity.getDiscountAmount());
        wrapper.setTaxableAmount(entity.getTaxableAmount());
        wrapper.setTaxAmount(entity.getTaxAmount());
        wrapper.setLineTotal(entity.getLineTotal());
        
        wrapper.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        wrapper.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        
        return wrapper;
    }
    
    /**
     * Convert Proposal Item Entity to Wrapper
     */
    private ProposalItemWrapper convertProposalItemToWrapper(ProposalItemEntity entity) {
        ProposalItemWrapper wrapper = new ProposalItemWrapper();
        wrapper.setId(entity.getId());
        wrapper.setProposalId(entity.getProposalId());
        wrapper.setLineNo(entity.getLineNo());
        wrapper.setItemName(entity.getItemName());
        wrapper.setSpecification(entity.getSpecification());
        wrapper.setDescription(entity.getDescription());
        wrapper.setQuantity(entity.getQuantity());
        wrapper.setUnit(entity.getUnit());
        wrapper.setUnitPrice(entity.getUnitPrice());
        wrapper.setTaxPercent(entity.getTaxPercent());
        wrapper.setLineTotal(entity.getLineTotal());
        wrapper.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        
        return wrapper;
    }
}