package com.istlgroup.istl_group_crm_backend.repo;

import com.istlgroup.istl_group_crm_backend.entity.VendorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    
    // ========== Basic Queries ==========
    
    Optional<VendorEntity> findByVendorCode(String vendorCode);
    
    Optional<VendorEntity> findByEmail(String email);
    
    List<VendorEntity> findByDeletedAtIsNull();
    
    boolean existsById(Long id);
    
    // ========== Project-based Filtering (Admin) ==========
    
    @Query("SELECT v FROM VendorEntity v WHERE v.deletedAt IS NULL")
    Page<VendorEntity> findAllActiveVendors(Pageable pageable);
    
    @Query("SELECT v FROM VendorEntity v WHERE v.groupName = :groupName AND v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupName(@Param("groupName") String groupName, Pageable pageable);
    
    @Query("SELECT v FROM VendorEntity v WHERE v.groupName = :groupName AND v.subGroupName = :subGroupName AND v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupAndSubGroup(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE v.projectId = :projectId AND v.deletedAt IS NULL")
    Page<VendorEntity> findByProjectId(@Param("projectId") String projectId, Pageable pageable);
    
    // ========== Project-based Filtering (User) ==========
    
    @Query("SELECT v FROM VendorEntity v WHERE (v.createdBy = :userId OR v.assignedTo = :userId) AND v.deletedAt IS NULL")
    Page<VendorEntity> findByUserAccess(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT v FROM VendorEntity v WHERE v.groupName = :groupName AND (v.createdBy = :userId OR v.assignedTo = :userId) AND v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupNameAndUserAccess(
        @Param("groupName") String groupName,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE v.groupName = :groupName AND v.subGroupName = :subGroupName AND (v.createdBy = :userId OR v.assignedTo = :userId) AND v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupSubGroupAndUserAccess(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE v.projectId = :projectId AND (v.createdBy = :userId OR v.assignedTo = :userId) AND v.deletedAt IS NULL")
    Page<VendorEntity> findByProjectIdAndUserAccess(
        @Param("projectId") String projectId,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    // ========== Combined Filters (Admin) ==========
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> findByFilters(
        @Param("category") String category,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "v.groupName = :groupName AND " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupNameAndFilters(
        @Param("groupName") String groupName,
        @Param("category") String category,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "v.groupName = :groupName AND " +
           "v.subGroupName = :subGroupName AND " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> findByGroupSubGroupAndFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("category") String category,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "v.projectId = :projectId AND " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> findByProjectIdAndFilters(
        @Param("projectId") String projectId,
        @Param("category") String category,
        @Param("status") String status,
        Pageable pageable
    );
    
    // ========== Search ==========
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "(LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> searchVendors(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT v FROM VendorEntity v WHERE " +
           "(LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(v.createdBy = :userId OR v.assignedTo = :userId) AND " +
           "v.deletedAt IS NULL")
    Page<VendorEntity> searchVendorsWithUserAccess(
        @Param("searchTerm") String searchTerm,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    // ========== Filter by Category/Type ==========
    
    @Query("SELECT v FROM VendorEntity v WHERE v.category = :category AND v.deletedAt IS NULL")
    List<VendorEntity> findByCategory(@Param("category") String category);
    
    @Query("SELECT v FROM VendorEntity v WHERE v.vendorType = :vendorType AND v.deletedAt IS NULL")
    List<VendorEntity> findByVendorType(@Param("vendorType") String vendorType);
    
    // ========== Statistics Queries - Dynamic based on filters ==========
    
    @Query("SELECT COUNT(v) FROM VendorEntity v WHERE v.deletedAt IS NULL")
    long countActiveVendors();
    
    @Query("SELECT COUNT(v) FROM VendorEntity v WHERE " +
           "(:groupName IS NULL OR v.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR v.subGroupName = :subGroupName) AND " +
           "(:projectId IS NULL OR v.projectId = :projectId) AND " +
           "v.deletedAt IS NULL")
    long countByFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
    
    @Query("SELECT COUNT(v) FROM VendorEntity v WHERE " +
           "v.status = 'Active' AND " +
           "(:groupName IS NULL OR v.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR v.subGroupName = :subGroupName) AND " +
           "(:projectId IS NULL OR v.projectId = :projectId) AND " +
           "v.deletedAt IS NULL")
    long countActiveByFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
    
    @Query("SELECT AVG(v.rating) FROM VendorEntity v WHERE " +
           "v.rating IS NOT NULL AND v.rating > 0 AND " +
           "(:groupName IS NULL OR v.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR v.subGroupName = :subGroupName) AND " +
           "(:projectId IS NULL OR v.projectId = :projectId) AND " +
           "v.deletedAt IS NULL")
    Double getAverageRatingByFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
    
    @Query("SELECT SUM(v.totalPurchaseValue) FROM VendorEntity v WHERE " +
           "(:groupName IS NULL OR v.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR v.subGroupName = :subGroupName) AND " +
           "(:projectId IS NULL OR v.projectId = :projectId) AND " +
           "v.deletedAt IS NULL")
    Double getTotalPurchaseValueByFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
    
    // Get count of pending quotations for vendors
    @Query("SELECT COUNT(DISTINCT q.id) FROM QuotationEntity q WHERE " +
           "q.status IN ('New', 'Under Review') AND " +
           "(:groupName IS NULL OR q.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR q.subGroupName = :subGroupName) AND " +
           "(:projectId IS NULL OR q.projectId = :projectId) AND " +
           "q.deletedAt IS NULL")
    long countPendingQuotationsByFilters(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("projectId") String projectId
    );
}