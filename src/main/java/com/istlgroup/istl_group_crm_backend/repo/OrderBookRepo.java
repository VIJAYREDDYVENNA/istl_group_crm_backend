package com.istlgroup.istl_group_crm_backend.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.istlgroup.istl_group_crm_backend.entity.OrderBookEntity;

public interface OrderBookRepo extends JpaRepository<OrderBookEntity, Long> {
    
    Optional<OrderBookEntity> findByOrderBookNo(String orderBookNo);
    
    List<OrderBookEntity> findByDeletedAtIsNull();
    
    Page<OrderBookEntity> findByDeletedAtIsNull(Pageable pageable);
    
    List<OrderBookEntity> findByCustomerIdAndDeletedAtIsNull(Long customerId);
    
    List<OrderBookEntity> findByProposalIdAndDeletedAtIsNull(Long proposalId);
    
    List<OrderBookEntity> findByGroupNameAndDeletedAtIsNull(String groupName);
    
    List<OrderBookEntity> findByStatusAndDeletedAtIsNull(String status);
    
    long countByOrderBookNoStartingWith(String prefix);
    
    @Query("SELECT o FROM OrderBookEntity o WHERE o.deletedAt IS NULL " +
           "AND (:searchTerm IS NULL OR " +
           "LOWER(o.orderBookNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.orderTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.poNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:groupName IS NULL OR o.groupName = :groupName) " +
           "AND (:subGroupName IS NULL OR o.subGroupName = :subGroupName) " +
           "AND (:fromDate IS NULL OR o.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR o.orderDate <= :toDate)")
    Page<OrderBookEntity> searchOrderBooks(
        @Param("searchTerm") String searchTerm,
        @Param("status") String status,
        @Param("groupName") String groupName,
        @Param("subGroupName") String subGroupName,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );
}