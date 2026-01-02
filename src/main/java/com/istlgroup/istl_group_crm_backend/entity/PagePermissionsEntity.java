package com.istlgroup.istl_group_crm_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="page_permissions")
@Data
public class PagePermissionsEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user_id;

    // USERS
    private Integer users_view;
    private Integer users_create;
    private Integer users_edit;
    private Integer users_delete;

    // ROLES
    private Integer roles_manage;

    // CUSTOMERS
    private Integer customers_view;
    private Integer customers_create;
    private Integer customers_edit;
    private Integer customers_delete;

    // VENDORS
    private Integer vendors_view;
    private Integer vendors_create;
    private Integer vendors_edit;
    private Integer vendors_delete;

    // LEADS
    private Integer leads_view;
    private Integer leads_create;
    private Integer leads_edit;
    private Integer leads_delete;
    private Integer leads_assign;

    // PROPOSALS
    private Integer proposals_view;
    private Integer proposals_create;
    private Integer proposals_edit;
    private Integer proposals_delete;
    private Integer proposals_approve;

    // QUOTATIONS SALES
    private Integer quotations_sales_view;
    private Integer quotations_sales_create;
    private Integer quotations_sales_edit;
    private Integer quotations_sales_delete;
    private Integer quotations_sales_approve;

    // SALES ORDERS
    private Integer sales_orders_view;
    private Integer sales_orders_create;
    private Integer sales_orders_edit;
    private Integer sales_orders_delete;
    private Integer sales_orders_approve;

    // INVOICES
    private Integer invoices_view;
    private Integer invoices_create;
    private Integer invoices_edit;
    private Integer invoices_delete;
    private Integer invoices_send;

    // QUOTATIONS PROCUREMENT
    private Integer quotations_procurement_view;
    private Integer quotations_procurement_create;
    private Integer quotations_procurement_edit;
    private Integer quotations_procurement_delete;
    private Integer quotations_procurement_approve;

    // PURCHASE ORDERS
    private Integer purchase_orders_view;
    private Integer purchase_orders_create;
    private Integer purchase_orders_edit;
    private Integer purchase_orders_delete;
    private Integer purchase_orders_approve;

    // BILLS
    private Integer bills_view;
    private Integer bills_create;
    private Integer bills_edit;
    private Integer bills_delete;
    private Integer bills_approve;

    // PAYMENTS
    private Integer payments_view;
    private Integer payments_record;
    private Integer payments_approve;

    // REPORTS
    private Integer reports_sales;
    private Integer reports_procurement;
    private Integer reports_financial;
    private Integer reports_analytics;

    // FOLLOWUPS
    private Integer followups_view;
    private Integer followups_create;
    private Integer followups_edit;
    private Integer followups_delete;

    // SETTINGS
    private Integer settings_view;
    private Integer settings_edit;

    // ACTIVITY LOGS
    private Integer activity_logs_view;

    // ATTACHMENTS
    private Integer attachments_upload;
    private Integer attachments_delete;

    // AUDIT
  
    private LocalDateTime created_at;

    private LocalDateTime updated_at;
}
