package com.istlgroup.istl_group_crm_backend.repo;

import com.istlgroup.istl_group_crm_backend.entity.PurchaseOrderEntity;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    
    // ========== Basic Queries ==========
    
    Optional<PurchaseOrderEntity> findByPoNo(String poNo);
    
    List<PurchaseOrderEntity> findByDeletedAtIsNull();
    
    // ========== Project-based Filtering (Admin) ==========
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findAllActive(Pageable pageable);
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.groupName = :groupName AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByGroupName(@Param("groupName") String groupName, Pageable pageable);
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.groupName = :groupName AND po.subGroupName = :subGroupName AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByGroupAndSubGroup(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        Pageable pageable
    );
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByProjectId(@Param("projectId") String projectId, Pageable pageable);
    
    // ========== Project-based Filtering (User) ==========
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE (po.createdBy = :userId OR po.approvedBy = :userId) AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByUserAccess(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.groupName = :groupName AND (po.createdBy = :userId OR po.approvedBy = :userId) AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByGroupNameAndUserAccess(
        @Param("groupName") String groupName,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.groupName = :groupName AND po.subGroupName = :subGroupName AND (po.createdBy = :userId OR po.approvedBy = :userId) AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByGroupSubGroupAndUserAccess(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND (po.createdBy = :userId OR po.approvedBy = :userId) AND po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> findByProjectIdAndUserAccess(
        @Param("projectId") String projectId,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    // ========== Status-based Queries ==========
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.status = :status AND po.deletedAt IS NULL")
    List<PurchaseOrderEntity> findByStatus(@Param("status") String status);
    
    // ========== Vendor-based Queries ==========
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.vendorId = :vendorId AND po.deletedAt IS NULL ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findByVendorIdOrderByOrderDateDesc(@Param("vendorId") Long vendorId);
    
    // ========== Quotation-based ==========
    
    Optional<PurchaseOrderEntity> findByQuotationId(Long quotationId);
    
    // ========== Search ==========
    
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE " +
           "(LOWER(po.poNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(po.rfqId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "po.deletedAt IS NULL")
    Page<PurchaseOrderEntity> search(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ========== Statistics ==========
    
    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po WHERE po.deletedAt IS NULL")
    long countActivePOs();
    
    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po WHERE po.status = :status AND po.deletedAt IS NULL")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(po.totalValue) FROM PurchaseOrderEntity po WHERE po.deletedAt IS NULL")
    Double getTotalPOValue();
    
    
    
    
    /**
     * Get dropdown list of POs with vendor names
     */
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.deletedAt IS NULL ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findAllForDropdown();
    
    /**
     * Get POs filtered by group/subgroup/project for dropdown
     */
    @Query("SELECT po FROM PurchaseOrderEntity po " +
           "WHERE po.deletedAt IS NULL " +
           "AND (:groupName IS NULL OR po.groupName = :groupName) " +
           "AND (:subGroupName IS NULL OR po.subGroupName = :subGroupName) " +
           "AND (:projectId IS NULL OR po.projectId = :projectId) " +
           "ORDER BY po.orderDate DESC")
    List<PurchaseOrderEntity> findAllForDropdownFiltered(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
    
    /**
     * Find POs by vendor
     */
    @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.vendorId = :vendorId AND po.deletedAt IS NULL")
    List<PurchaseOrderEntity> findByVendorId(@Param("vendorId") Long vendorId);
    
    
    
    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.deletedAt IS NULL")
    Long countByProjectId(@Param("projectId") String projectId);

    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.status = :status AND po.deletedAt IS NULL")
    Long countByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(po.totalValue), 0) FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.deletedAt IS NULL")
    Optional<BigDecimal> sumTotalValueByProjectId(@Param("projectId") String projectId);

    @Query("SELECT COALESCE(SUM(po.totalValue), 0) FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.status = :status AND po.deletedAt IS NULL")
    Optional<BigDecimal> sumTotalValueByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") String status);



    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.paymentStatus = :paymentStatus AND po.deletedAt IS NULL")
    Long countByProjectIdAndPaymentStatus(@Param("projectId") String projectId, @Param("paymentStatus") String paymentStatus);
    /**
     * Count POs by project and group by status
     */
    @Query("SELECT po.status, COUNT(po) FROM PurchaseOrderEntity po " +
           "WHERE po.projectId = :projectId AND po.deletedAt IS NULL " +
           "GROUP BY po.status")
    List<Object[]> countByProjectIdAndGroupByStatus(@Param("projectId") String projectId);
    @Query("SELECT po.category, SUM(po.totalValue) FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId AND po.deletedAt IS NULL " +
            "GROUP BY po.category " +
            "ORDER BY SUM(po.totalValue) DESC")
     List<Object[]> sumTotalValueByProjectIdGroupByCategory(@Param("projectId") String projectId);
     
     /**
      * Sum total items ordered
      */
     @Query("SELECT COALESCE(SUM(po.totalItemsOrdered), 0) FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId AND po.deletedAt IS NULL")
     Integer sumTotalItemsOrderedByProjectId(@Param("projectId") String projectId);
     
     /**
      * Sum total items delivered
      */
     @Query("SELECT COALESCE(SUM(po.totalItemsDelivered), 0) FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId AND po.deletedAt IS NULL")
     Integer sumTotalItemsDeliveredByProjectId(@Param("projectId") String projectId);
     
     /**
      * Find top 5 recent POs for a project
      */
     @Query("SELECT po FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId AND po.deletedAt IS NULL " +
            "ORDER BY po.createdAt DESC")
     List<PurchaseOrderEntity> findTop5ByProjectIdOrderByCreatedAtDesc(@Param("projectId") String projectId);
     
     /**
      * Sum total value by project and date range
      */
     @Query("SELECT COALESCE(SUM(po.totalValue), 0) FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId " +
            "AND po.orderDate BETWEEN :startDate AND :endDate " +
            "AND po.deletedAt IS NULL")
     BigDecimal sumTotalValueByProjectIdAndDateRange(
         @Param("projectId") String projectId,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate
     );
     
     /**
      * Count POs by project and date range
      */
     @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po " +
            "WHERE po.projectId = :projectId " +
            "AND po.orderDate BETWEEN :startDate AND :endDate " +
            "AND po.deletedAt IS NULL")
     Long countByProjectIdAndDateRange(
         @Param("projectId") String projectId,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate
     );
     
     /**
      * Count POs by vendor
      */
     @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po " +
            "WHERE po.vendorId = :vendorId AND po.deletedAt IS NULL")
     Long countByVendorId(@Param("vendorId") Long vendorId);

     @Query("SELECT po FROM PurchaseOrderEntity po " +
    	       "WHERE po.projectId = :projectId " +
    	       "AND po.status = :status " +
    	       "AND po.deletedAt IS NULL")
    	List<PurchaseOrderEntity> findByProjectIdAndStatus(
    	    @Param("projectId") String projectId, 
    	    @Param("status") String status
    	);

	 List<PurchaseOrderEntity> findByProjectId(String projectId);

	 List<PurchaseOrderEntity> findByGroupNameAndSubGroupName(String groupName, String subGroupName);

	 List<PurchaseOrderEntity> findByGroupName(String groupName);
	// Find POs by vendorId and project
	 @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.vendorId = :vendorId AND po.projectId = :projectId AND po.deletedAt IS NULL ORDER BY po.orderDate DESC")
	 List<PurchaseOrderEntity> findByVendorIdAndProjectId(@Param("vendorId") Long vendorId, @Param("projectId") String projectId);

	 // Find POs by vendorName and project (for new vendors)
	 @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.vendorName = :vendorName AND po.projectId = :projectId AND po.deletedAt IS NULL ORDER BY po.orderDate DESC")
	 List<PurchaseOrderEntity> findByVendorNameAndProjectId(@Param("vendorName") String vendorName, @Param("projectId") String projectId);

	 @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.vendorName = :vendorName AND po.deletedAt IS NULL ORDER BY po.orderDate DESC")
	 List<PurchaseOrderEntity> findByVendorNameOrderByOrderDateDesc(@Param("vendorName") String vendorName);

	 // Find POs by project (excluding cancelled)
	 @Query("SELECT po FROM PurchaseOrderEntity po WHERE po.projectId = :projectId AND po.status != :status AND po.deletedAt IS NULL ORDER BY po.orderDate DESC")
	 List<PurchaseOrderEntity> findByProjectIdAndStatusNot(@Param("projectId") String projectId, @Param("status") String status);
	 /**
	  * Update vendor ID for all POs with matching vendor name
	  */
	 @Modifying
	 @Query("UPDATE PurchaseOrderEntity po " +
	        "SET po.vendorId = :vendorId " +
	        "WHERE po.vendorName = :vendorName " +
	        "AND po.vendorId IS NULL " +
	        "AND po.projectId = :projectId " +
	        "AND po.deletedAt IS NULL")
	 int updateVendorIdForPOs(
	     @Param("vendorId") Long vendorId,
	     @Param("vendorName") String vendorName,
	     @Param("projectId") String projectId
	 );

	 @Modifying
	 @Transactional
	 @Query("""
	     UPDATE PurchaseOrderEntity po
	     SET po.vendorId = :vendorId
	     WHERE po.vendorName = :vendorName
	       AND po.vendorContact = :contactNumber
	       AND po.vendorId IS NULL
	       AND po.projectId = :projectId
	       AND po.deletedAt IS NULL
	 """)
	 int updateVendorIdForPO(
	         @Param("vendorId") Long vendorId,
	         @Param("vendorName") String vendorName,
	         @Param("contactNumber") String contactNumber,
	         @Param("projectId") String projectId
	 );

}




