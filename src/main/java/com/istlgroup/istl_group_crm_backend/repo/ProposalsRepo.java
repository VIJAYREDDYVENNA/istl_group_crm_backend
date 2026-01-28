package com.istlgroup.istl_group_crm_backend.repo;

import com.istlgroup.istl_group_crm_backend.entity.ProposalsEntity;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProposalsRepo extends JpaRepository<ProposalsEntity, Long> {
    
    /**
     * Count proposals by number prefix (for auto-generation)
     */
    long countByProposalNoStartingWith(String prefix);
    
    /**
     * Find by proposal number
     */
    Optional<ProposalsEntity> findByProposalNo(String proposalNo);
    
    /**
     * Find all active proposals (not deleted)
     */
    List<ProposalsEntity> findByDeletedAtIsNull();
    
    /**
     * Find all active proposals with pagination
     */
    Page<ProposalsEntity> findByDeletedAtIsNull(Pageable pageable);
    
    /**
     * Find proposals prepared by user OR where user created associated lead/customer
     * (For regular users - they see proposals they prepared)
     */
    @Query("""
        SELECT p FROM ProposalsEntity p
        WHERE p.deletedAt IS NULL
          AND p.leadId IN (
              SELECT l.id
              FROM LeadsEntity l
              WHERE l.deletedAt IS NULL
                AND (l.createdBy = :userId OR l.assignedTo = :userId)
          )
    """)
    Page<ProposalsEntity> findProposalsForUserLeadsWithoutGroup(
        @Param("userId") Long userId,
        Pageable pageable
    );

    
    /**
     * Find by group name with pagination
     */
    @Query("SELECT p FROM ProposalsEntity p WHERE " +
           "p.deletedAt IS NULL AND " +
           "p.groupName = :groupName")
    Page<ProposalsEntity> findByGroupNameAndDeletedAtIsNull(@Param("groupName") String groupName, Pageable pageable);
    
    /**
     * NEW: Find by group and subgroup name with pagination
     */
    @Query("SELECT p FROM ProposalsEntity p WHERE " +
           "p.deletedAt IS NULL AND " +
           "p.groupName = :groupName AND " +
           "p.subGroupName = :subGroupName")
    Page<ProposalsEntity> findByGroupNameAndSubGroupNameAndDeletedAtIsNull(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        Pageable pageable
    );
    
    /**
     * Find by group and prepared by
     */
    @Query("""
        SELECT p FROM ProposalsEntity p
        WHERE p.deletedAt IS NULL
          AND p.groupName = :groupName
          AND p.leadId IN (
              SELECT l.id
              FROM LeadsEntity l
              WHERE l.deletedAt IS NULL
                AND (l.createdBy = :userId OR l.assignedTo = :userId)
          )
    """)
    Page<ProposalsEntity> findProposalsForUserLeads(
        @Param("groupName") String groupName,
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * NEW: Find by group, subgroup and prepared by
     */
    @Query("""
        SELECT p FROM ProposalsEntity p
        WHERE p.deletedAt IS NULL
          AND p.groupName = :groupName
          AND p.subGroupName = :subGroupName
          AND p.leadId IN (
              SELECT l.id
              FROM LeadsEntity l
              WHERE l.deletedAt IS NULL
                AND (l.createdBy = :userId OR l.assignedTo = :userId)
          )
    """)
    Page<ProposalsEntity> findProposalsForUserLeadsByGroupAndSubGroup(
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    /**
     * Search proposals for ADMIN/SUPERADMIN with pagination
     */
    @Query("SELECT p FROM ProposalsEntity p WHERE " +
           "p.deletedAt IS NULL AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(p.proposalNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:groupName IS NULL OR p.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR p.subGroupName = :subGroupName) AND " +
           "(:preparedBy IS NULL OR p.preparedBy = :preparedBy) AND " +
           "(:leadId IS NULL OR p.leadId = :leadId) AND " +
           "(:customerId IS NULL OR p.customerId = :customerId) AND " +
           "(:fromDate IS NULL OR p.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR p.createdAt <= :toDate)")
    Page<ProposalsEntity> searchProposalsPaginated(
        @Param("searchTerm") String searchTerm,
        @Param("status") String status,
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("preparedBy") Long preparedBy,
        @Param("leadId") Long leadId,
        @Param("customerId") Long customerId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
    
    /**
     * Search proposals for regular users (only prepared by them) with pagination
     */
    @Query("SELECT p FROM ProposalsEntity p WHERE " +
           "p.deletedAt IS NULL AND " +
           "p.preparedBy = :userId AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(p.proposalNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:groupName IS NULL OR p.groupName = :groupName) AND " +
           "(:subGroupName IS NULL OR p.subGroupName = :subGroupName) AND " +
           "(:leadId IS NULL OR p.leadId = :leadId) AND " +
           "(:customerId IS NULL OR p.customerId = :customerId) AND " +
           "(:fromDate IS NULL OR p.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR p.createdAt <= :toDate)")
    Page<ProposalsEntity> searchProposalsForUserPaginated(
        @Param("userId") Long userId,
        @Param("searchTerm") String searchTerm,
        @Param("status") String status,
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("leadId") Long leadId,
        @Param("customerId") Long customerId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
    
    /**
     * Find proposals by lead ID
     */
    List<ProposalsEntity> findByLeadIdAndDeletedAtIsNull(Long leadId);
    
    /**
     * Find proposals by customer ID
     */
    List<ProposalsEntity> findByCustomerIdAndDeletedAtIsNull(Long customerId);
    
    /**
     *to check edit permission
     */
    
    @Query("""
            SELECT COUNT(p) > 0
            FROM PagePermissionsEntity p
            WHERE p.user_id = :userId
              AND p.proposals_edit = 1
        """)
        boolean hasProposalEditPermission(@Param("userId") Long userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ProposalsEntity p
        SET p.customerId = :customerId
        WHERE p.leadId = :leadId
    """)
    int updateCustomerId(@Param("customerId") Long customerId,
                         @Param("leadId") Long leadId);
}