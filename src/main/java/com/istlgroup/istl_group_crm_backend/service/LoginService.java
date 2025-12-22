package com.istlgroup.istl_group_crm_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;
import com.istlgroup.istl_group_crm_backend.entity.MenuPermissionsEntity;
import com.istlgroup.istl_group_crm_backend.repo.LoginRepo;
import com.istlgroup.istl_group_crm_backend.repo.MenuPermissionsRepo;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.LoginCredentialsWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.LoginResponseWrapper;


@Service
public class LoginService {
	
	@Autowired
	private LoginRepo loginRepo;
	
	@Autowired
	private MenuPermissionsRepo menu_permissions;

	public ResponseEntity<LoginResponseWrapper> AuthenticateUser(Map<String, String> credentials) throws CustomException{
		
		String username=credentials.get("username");
		String password=credentials.get("password");
		
		LoginEntity response=loginRepo.AuthenticateUser(username,password);
		if (response == null) {
	        throw new CustomException("Invalid Credentials");
	    }
		
		LoginCredentialsWrapper wrappedData=new LoginCredentialsWrapper();
		wrappedData.setId(response.getId());
		wrappedData.setName(response.getName());
		wrappedData.setRole(response.getRole());
		wrappedData.setUser_id(response.getUser_id());
		wrappedData.setEmail(response.getEmail());
		wrappedData.setPhone(response.getPhone());
		wrappedData.setIs_active(response.getIsActive());
		
		MenuPermissionsEntity permissions=menu_permissions.findByUsersId(response.getId());
		List<String> permissionsMenu=extractPermissions(permissions);

		LoginResponseWrapper loginResponseWrapper=new LoginResponseWrapper();
		loginResponseWrapper.setUser(wrappedData);
		loginResponseWrapper.setMenuPermissions(permissionsMenu);
		
		return ResponseEntity.status(HttpStatus.OK).body(loginResponseWrapper);
	}

	private List<String> extractPermissions(MenuPermissionsEntity p) {

	    List<String> permissions = new ArrayList<>();

	    if (p.getDashboard() == 1) permissions.add("DASHBOARD");
	    if (p.getAnalytics() == 1) permissions.add("ANALYTICS");
	    if (p.getDocuments() == 1) permissions.add("DOCUMENTS");
	    if (p.getSettings() == 1) permissions.add("SETTINGS");
	    if (p.getFollow_ups() == 1) permissions.add("FOLLOW_UPS");
	    if (p.getReports() == 1) permissions.add("REPORTS");
	    if (p.getInvoices() == 1) permissions.add("INVOICES");
	    if (p.getSales_clients() == 1) permissions.add("SALES_CLIENTS");
	    if (p.getSales_leads() == 1) permissions.add("SALES_LEADS");
	    if (p.getSales_estimation() == 1) permissions.add("SALES_ESTIMATION");
	    if (p.getProcurement_venders() == 1) permissions.add("PROCUREMENT_VENDERS");
	    if (p.getProcurement_cotations_recived() == 1) permissions.add("PROCUREMENT_COTATIONS");
	    if (p.getProcurement_purcharge_orders() == 1) permissions.add("PROCUREMENT_PURCHASE_ORDERS");
	    if (p.getProcurement_bills_recived() == 1) permissions.add("PROCUREMENT_BILLS");

	    return permissions;
	}

	
}
