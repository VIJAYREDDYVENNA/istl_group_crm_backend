package com.istlgroup.istl_group_crm_backend.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.istlgroup.istl_group_crm_backend.entity.VendorEntity;
import com.istlgroup.istl_group_crm_backend.repo.VendorRepository;
import com.istlgroup.istl_group_crm_backend.service.BillService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.BillDTO;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.BillItemDTO;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.BillStatsDTO;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.PaymentDTO;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
public class BillController {
    
	 private final BillService billService;
	    private final VendorRepository vendorRepository;
    
    /**
     * Get all bills with role-based filtering
     * GET /api/bills
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBills(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String subGroupId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long poId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "billDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestHeader("X-User-Role") String userRole
    ) {
        try {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole) || 
                            "SUPERADMIN".equalsIgnoreCase(userRole);
            
            Page<BillDTO> bills = billService.getBills(
                    projectId, groupId, subGroupId, status, vendorId, poId,
                    search, page, size, sortBy, sortDirection, isAdmin
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("bills", bills.getContent());
            response.put("currentPage", bills.getNumber());
            response.put("totalPages", bills.getTotalPages());
            response.put("totalItems", bills.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching bills", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get bill by ID
     * GET /api/bills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBillById(@PathVariable Long id) {
        try {
            BillDTO bill = billService.getBillById(id);
            return ResponseEntity.ok(bill);
        } catch (RuntimeException e) {
            log.error("Error fetching bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Create new bill
     * POST /api/bills
     */
      /**
     * POST /api/bills
     * Create new bill - handles both existing vendors and PO vendors
     */
   @PostMapping
    public ResponseEntity<?> createBill(
            @RequestBody Map<String, Object> billRequest,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            log.info("Creating bill - User: {}, Request keys: {}", userId, billRequest.keySet());
            
            // ========== STEP 1: Extract and validate vendor info ==========
            Object vendorIdObj = billRequest.get("vendorId");
            if (vendorIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vendor ID is required"));
            }
            
            Long actualVendorId = null;
            String vendorIdStr = vendorIdObj.toString().trim();
            
            if (vendorIdStr.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vendor ID cannot be empty"));
            }
            
            if (vendorIdStr.startsWith("PO_")) {
                // This is a PO vendor (new vendor from PO)
                String vendorName = vendorIdStr.substring(3).trim(); // Remove "PO_" prefix
                log.info("Processing PO vendor: {}", vendorName);
                
                if (vendorName.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid PO vendor format"));
                }
                
                // Try to find existing vendor by name
                VendorEntity vendor = vendorRepository.findByName(vendorName)
                        .orElseGet(() -> {
                            // Vendor doesn't exist, create it
                            log.info("Creating new vendor: {}", vendorName);
                            
                            VendorEntity newVendor = new VendorEntity();
                            newVendor.setName(vendorName);
                            
                            // Extract contact if provided
                            Object contactObj = billRequest.get("vendorContact");
                            if (contactObj != null && !contactObj.toString().trim().isEmpty()) {
                                newVendor.setPhone(contactObj.toString().trim());
                            }
                            
                            // Set project info
                            Object groupIdObj = billRequest.get("groupId");
                            if (groupIdObj != null && !groupIdObj.toString().trim().isEmpty()) {
                                newVendor.setGroupName(groupIdObj.toString().trim());
                            }
                            
                            Object subGroupIdObj = billRequest.get("subGroupId");
                            if (subGroupIdObj != null && !subGroupIdObj.toString().trim().isEmpty()) {
                                newVendor.setSubGroupName(subGroupIdObj.toString().trim());
                            }
                            
                            Object projectIdObj = billRequest.get("projectId");
                            if (projectIdObj != null && !projectIdObj.toString().trim().isEmpty()) {
                                newVendor.setProjectId(projectIdObj.toString().trim());
                            }
                            
                            newVendor.setStatus("Active");
                            newVendor.setTotalOrders(0);
                            newVendor.setTotalPurchaseValue(BigDecimal.ZERO);
                            newVendor.setCreatedBy(userId);
                            
                            return vendorRepository.save(newVendor);
                        });
                
                actualVendorId = vendor.getId();
                log.info("Using vendor ID: {} for vendor: {}", actualVendorId, vendorName);
                
            } else {
                // Regular vendor ID (numeric)
                try {
                    actualVendorId = Long.parseLong(vendorIdStr);
                    
                    // Validate vendor exists
                    if (!vendorRepository.existsById(actualVendorId)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Vendor not found with ID: " + actualVendorId));
                    }
                    
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid vendor ID format: " + vendorIdStr));
                }
            }
            
            // ========== STEP 2: Extract PO ID ==========
            Long poId = null;
            Object poIdObj = billRequest.get("poId");
            if (poIdObj != null && !poIdObj.toString().trim().isEmpty()) {
                try {
                    poId = Long.parseLong(poIdObj.toString().trim());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid PO ID format"));
                }
            }
            
            // ========== STEP 3: Extract and validate dates ==========
            Object billDateObj = billRequest.get("billDate");
            if (billDateObj == null || billDateObj.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bill date is required"));
            }
            
            Object dueDateObj = billRequest.get("dueDate");
            if (dueDateObj == null || dueDateObj.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Due date is required"));
            }
            
            LocalDate billDate = null;
            LocalDate dueDate = null;
            
            try {
                billDate = LocalDate.parse(billDateObj.toString().trim());
                dueDate = LocalDate.parse(dueDateObj.toString().trim());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid date format. Use YYYY-MM-DD"));
            }
            
            // ========== STEP 4: Extract project info ==========
            String projectId = billRequest.get("projectId") != null 
                    ? billRequest.get("projectId").toString().trim() 
                    : null;
            String groupId = billRequest.get("groupId") != null 
                    ? billRequest.get("groupId").toString().trim() 
                    : null;
            String subGroupId = billRequest.get("subGroupId") != null 
                    ? billRequest.get("subGroupId").toString().trim() 
                    : null;
            String notes = billRequest.get("notes") != null 
                    ? billRequest.get("notes").toString().trim() 
                    : null;
            String billNo = billRequest.get("billNo") != null 
                    ? billRequest.get("billNo").toString().trim() 
                    : null;
            
            // ========== STEP 5: Extract and validate items ==========
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = 
                    (List<Map<String, Object>>) billRequest.get("items");
            
            if (itemsData == null || itemsData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one item is required"));
            }
            
            // Convert items to BillItemDTO
            List<BillItemDTO> items;
            try {
                items = itemsData.stream()
                        .map(itemMap -> {
                            BillItemDTO item = new BillItemDTO();
                            
                            // PO Item ID (required)
                            Object poItemIdObj = itemMap.get("poItemId");
                            if (poItemIdObj == null) {
                                throw new RuntimeException("PO Item ID is required for all items");
                            }
                            item.setPoItemId(Long.parseLong(poItemIdObj.toString()));
                            
                            // Quantity (required)
                            Object quantityObj = itemMap.get("quantity");
                            if (quantityObj == null) {
                                throw new RuntimeException("Quantity is required for all items");
                            }
                            BigDecimal quantity = new BigDecimal(quantityObj.toString());
                            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                                throw new RuntimeException("Quantity must be greater than zero");
                            }
                            item.setQuantity(quantity);
                            
                            // Description (optional)
                            Object descriptionObj = itemMap.get("description");
                            if (descriptionObj != null) {
                                item.setDescription(descriptionObj.toString());
                            }
                            
                            // Unit price (optional - will be set from PO item)
                            Object unitPriceObj = itemMap.get("unitPrice");
                            if (unitPriceObj != null) {
                                item.setUnitPrice(new BigDecimal(unitPriceObj.toString()));
                            }
                            
                            // Tax percent (optional - will be set from PO item)
                            Object taxPercentObj = itemMap.get("taxPercent");
                            if (taxPercentObj != null) {
                                item.setTaxPercent(new BigDecimal(taxPercentObj.toString()));
                            }
                            
                            return item;
                        }).collect(Collectors.toList());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid item data: " + e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Error parsing item data: " + e.getMessage()));
            }
            
            // ========== STEP 6: Create BillDTO ==========
            BillDTO dto = new BillDTO();
            dto.setBillNo(billNo);
            dto.setVendorId(actualVendorId);
            dto.setPoId(poId);
            dto.setBillDate(billDate);
            dto.setDueDate(dueDate);
            dto.setProjectId(projectId);
            dto.setGroupId(groupId);
            dto.setSubGroupId(subGroupId);
            dto.setNotes(notes);
            dto.setItems(items);
            
            // ========== STEP 7: Create bill via service ==========
            log.info("Calling billService.createBill with vendor ID: {}, PO ID: {}, Items: {}", 
                     actualVendorId, poId, items.size());
            
            BillDTO createdBill = billService.createBill(dto, userId);
            
            // ========== STEP 8: Return success response ==========
            log.info("Bill created successfully: {}", createdBill.getBillNo());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
            
        } catch (RuntimeException e) {
            log.error("Runtime error creating bill", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating bill", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Update bill
     * PUT /api/bills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBill(
            @PathVariable Long id,
            @RequestBody BillDTO billDTO,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            BillDTO updatedBill = billService.updateBill(id, billDTO, userId);
            return ResponseEntity.ok(updatedBill);
        } catch (RuntimeException e) {
            log.error("Error updating bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Delete bill (soft delete)
     * DELETE /api/bills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            billService.deleteBill(id, userId);
            return ResponseEntity.ok(Map.of("message", "Bill deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Upload bill file
     * POST /api/bills/{id}/upload
     */
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadBillFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            String filePath = billService.uploadBillFile(id, file, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "filePath", filePath
            ));
        } catch (RuntimeException e) {
            log.error("Error uploading file for bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Error uploading file for bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "File upload failed"));
        }
    }
    
    /**
     * Download bill file
     * GET /api/bills/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadBillFile(@PathVariable Long id) {
        try {
            BillDTO bill = billService.getBillById(id);
            
            if (bill.getBillFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get(bill.getBillFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + bill.getBillFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading file for bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * View bill file (inline)
     * GET /api/bills/{id}/view
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewBillFile(@PathVariable Long id) {
        try {
            BillDTO bill = billService.getBillById(id);
            
            if (bill.getBillFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get(bill.getBillFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + bill.getBillFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error viewing file for bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Add payment to bill
     * POST /api/bills/{id}/payments
     */
    @PostMapping("/{id}/payments")
    public ResponseEntity<?> addPayment(
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            BillDTO updatedBill = billService.addPayment(id, paymentDTO, userId);
            return ResponseEntity.ok(updatedBill);
        } catch (RuntimeException e) {
            log.error("Error adding payment to bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding payment to bill: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Mark bill as fully paid
     * POST /api/bills/{id}/mark-paid
     */
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            BillDTO updatedBill = billService.markAsPaid(id, userId);
            return ResponseEntity.ok(updatedBill);
        } catch (RuntimeException e) {
            log.error("Error marking bill as paid: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking bill as paid: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Get bill statistics
     * GET /api/bills/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String subGroupId
    ) {
        try {
            BillStatsDTO stats = billService.getStatistics(projectId, groupId, subGroupId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching bill statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}
