package com.istlgroup.istl_group_crm_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.service.OrderBookService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookItemWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.OrderBookRequestWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.ProposalItemWrapper;

@RestController
@RequestMapping("/order-book")
public class OrderBookController {
    
    @Autowired
    private OrderBookService orderBookService;
    
    /**
     * Get all order books with pagination
     */
    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllOrderBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String subGroupName,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            Page<OrderBookWrapper> orderBooks = orderBookService.getAllOrderBooks(page, size, groupName, subGroupName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderBooks.getContent());
            response.put("currentPage", orderBooks.getNumber());
            response.put("totalItems", orderBooks.getTotalElements());
            response.put("totalPages", orderBooks.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Search and filter order books
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchOrderBooks(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String subGroupName,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            Page<OrderBookWrapper> results = orderBookService.searchOrderBooks(
                searchTerm, status, groupName, subGroupName, fromDate, toDate, page, size
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.getContent());
            response.put("currentPage", results.getNumber());
            response.put("totalItems", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get order book by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderBookById(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            OrderBookWrapper orderBook = orderBookService.getOrderBookById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orderBook);
            
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get order book items
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> getOrderBookItems(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            List<OrderBookItemWrapper> items = orderBookService.getOrderBookItems(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", items);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get proposal items (for loading into order book)
     */
    // UPDATE YOUR OrderBookController.java

/**
 * Get proposal BOM items by proposal ID (for loading into order book)
 * Fetches from proposals.bom_items JSON column
 * GET /order-book/proposal-items/{proposalId}
 */
@GetMapping("/proposal-items/{proposalId}")
public ResponseEntity<Map<String, Object>> getProposalItems(
        @PathVariable Long proposalId,
        @RequestHeader("User-Id") Long userId,
        @RequestHeader("User-Role") String userRole) {
    try {
        List<Map<String, Object>> items = orderBookService.getProposalBomItems(proposalId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", items);
        
        return ResponseEntity.ok(response);
    } catch (CustomException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
    
    /**
     * Create order book
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrderBook(
            @RequestBody OrderBookRequestWrapper request,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            OrderBookWrapper created = orderBookService.createOrderBook(request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order book created successfully");
            response.put("data", created);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CustomException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create order book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update order book
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateOrderBook(
            @PathVariable Long id,
            @RequestBody OrderBookRequestWrapper request,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            OrderBookWrapper updated = orderBookService.updateOrderBook(id, request, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order book updated successfully");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update order book");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Upload PO file
     */
    @PostMapping("/{id}/upload-po")
    public ResponseEntity<Map<String, Object>> uploadPOFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("poNumber") String poNumber,
            @RequestParam(value = "poDate", required = false) String poDate,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            OrderBookWrapper updated = orderBookService.uploadPOFile(id, file, poNumber, poDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PO file uploaded successfully");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload PO file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete order book
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrderBook(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("User-Role") String userRole) {
        try {
            orderBookService.deleteOrderBook(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order book deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete order book");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}