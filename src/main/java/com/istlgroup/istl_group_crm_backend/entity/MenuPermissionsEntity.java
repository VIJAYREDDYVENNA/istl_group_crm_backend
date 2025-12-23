package com.istlgroup.istl_group_crm_backend.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "menu_permissions")
@Data
public class MenuPermissionsEntity {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    @Column(name = "users_id")
	    private Long usersId;

	    private Integer dashboard;
	    public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getUsersId() {
			return usersId;
		}
		public void setUsersId(Long usersId) {
			this.usersId = usersId;
		}
		public Integer getDashboard() {
			return dashboard;
		}
		public void setDashboard(Integer dashboard) {
			this.dashboard = dashboard;
		}
		public Integer getAnalytics() {
			return analytics;
		}
		public void setAnalytics(Integer analytics) {
			this.analytics = analytics;
		}
		public Integer getDocuments() {
			return documents;
		}
		public void setDocuments(Integer documents) {
			this.documents = documents;
		}
		public Integer getSettings() {
			return settings;
		}
		public void setSettings(Integer settings) {
			this.settings = settings;
		}
		public Integer getFollow_ups() {
			return follow_ups;
		}
		public void setFollow_ups(Integer follow_ups) {
			this.follow_ups = follow_ups;
		}
		public Integer getReports() {
			return reports;
		}
		public void setReports(Integer reports) {
			this.reports = reports;
		}
		public Integer getInvoices() {
			return invoices;
		}
		public void setInvoices(Integer invoices) {
			this.invoices = invoices;
		}
		public Integer getSales_clients() {
			return sales_clients;
		}
		public void setSales_clients(Integer sales_clients) {
			this.sales_clients = sales_clients;
		}
		public Integer getSales_leads() {
			return sales_leads;
		}
		public void setSales_leads(Integer sales_leads) {
			this.sales_leads = sales_leads;
		}
		public Integer getSales_estimation() {
			return sales_estimation;
		}
		public void setSales_estimation(Integer sales_estimation) {
			this.sales_estimation = sales_estimation;
		}
		public Integer getProcurement_venders() {
			return procurement_venders;
		}
		public void setProcurement_venders(Integer procurement_venders) {
			this.procurement_venders = procurement_venders;
		}
		public Integer getProcurement_cotations_recived() {
			return procurement_cotations_recived;
		}
		public void setProcurement_cotations_recived(Integer procurement_cotations_recived) {
			this.procurement_cotations_recived = procurement_cotations_recived;
		}
		public Integer getProcurement_purcharge_orders() {
			return procurement_purcharge_orders;
		}
		public void setProcurement_purcharge_orders(Integer procurement_purcharge_orders) {
			this.procurement_purcharge_orders = procurement_purcharge_orders;
		}
		public Integer getProcurement_bills_recived() {
			return procurement_bills_recived;
		}
		public void setProcurement_bills_recived(Integer procurement_bills_recived) {
			this.procurement_bills_recived = procurement_bills_recived;
		}
		private Integer analytics;
	    private Integer documents;
	    private Integer settings;
	    private Integer follow_ups;
	    private Integer reports;
	    private Integer invoices;
	    private Integer sales_clients;
	    private Integer sales_leads;
	    private Integer sales_estimation;
	    private Integer procurement_venders;
	    private Integer procurement_cotations_recived;
	    private Integer procurement_purcharge_orders;
	    private Integer procurement_bills_recived;
}
