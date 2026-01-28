package com.istlgroup.istl_group_crm_backend.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.istlgroup.istl_group_crm_backend.entity.OrderBookItemEntity;

public interface OrderBookItemRepo extends JpaRepository<OrderBookItemEntity, Long> {
    
    List<OrderBookItemEntity> findByOrderBookIdOrderByLineNo(Long orderBookId);
    
    void deleteByOrderBookId(Long orderBookId);
    
    @Query(value = "SELECT *, " +
           "quantity * unit_price AS line_subtotal, " +
           "(quantity * unit_price) * (discount_percent / 100) AS discount_amount, " +
           "(quantity * unit_price) - ((quantity * unit_price) * (discount_percent / 100)) AS taxable_amount, " +
           "((quantity * unit_price) - ((quantity * unit_price) * (discount_percent / 100))) * (tax_percent / 100) AS tax_amount, " +
           "((quantity * unit_price) - ((quantity * unit_price) * (discount_percent / 100))) + " +
           "(((quantity * unit_price) - ((quantity * unit_price) * (discount_percent / 100))) * (tax_percent / 100)) AS line_total " +
           "FROM order_book_items WHERE order_book_id = :orderBookId ORDER BY line_no",
           nativeQuery = true)
    List<OrderBookItemEntity> findByOrderBookIdWithCalculatedFields(@Param("orderBookId") Long orderBookId);
}