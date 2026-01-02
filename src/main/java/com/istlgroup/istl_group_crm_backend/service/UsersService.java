package com.istlgroup.istl_group_crm_backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;
import com.istlgroup.istl_group_crm_backend.entity.MenuPermissionsEntity;
import com.istlgroup.istl_group_crm_backend.entity.UsersEntity;
import com.istlgroup.istl_group_crm_backend.repo.MenuPermissionsRepo;
import com.istlgroup.istl_group_crm_backend.repo.RolePermissionsRepo;
import com.istlgroup.istl_group_crm_backend.repo.RolesRepo;
import com.istlgroup.istl_group_crm_backend.repo.UsersRepo;

@Service
public class UsersService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private MenuPermissionsRepo menuPermissionsRepo;

    @Autowired
    private RolePermissionsRepo rolePermissionsRepo;

    @Autowired
    private RolesRepo rolesRepo;  // ← ADD THIS

    public ResponseEntity<?> UpdateUser(LoginEntity newData, Long id) throws CustomException {
        UsersEntity isUserExist = usersRepo.findById(id).orElseThrow(() -> new CustomException("Invalid User"));

        isUserExist.setName(newData.getName());
        isUserExist.setEmail(newData.getEmail());
        isUserExist.setPhone(newData.getPhone());
        isUserExist.setRole(newData.getRole());
        isUserExist.setIs_active(newData.getIs_active());
        isUserExist.setUpdated_at(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        isUserExist.setUpdated_type("PROFILE_UPDATED");

        UsersEntity response = usersRepo.save(isUserExist);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Update Failed");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Profile Details Updated Successfully");
    }

    @Transactional
    public ResponseEntity<String> DeleteUser(Long id) throws CustomException {
        usersRepo.findById(id).orElseThrow(() -> new CustomException("Invalid User"));

        // Delete related menu permissions first
        MenuPermissionsEntity menuPerms = menuPermissionsRepo.findByUsersId(id);
        if (menuPerms != null) {
            menuPermissionsRepo.delete(menuPerms);
        }

        // Note: We DON'T delete role_permissions because they belong to the role, not the user
        // Multiple users can have the same role

        // Delete user
        usersRepo.deleteById(id);
        
        return ResponseEntity.ok("User deleted successfully");
    }

    public ResponseEntity<?> UpdateMenuPermissions(Long id, Map<String, Integer> permissions) throws CustomException {
        usersRepo.findById(id).orElseThrow(() -> new CustomException("Invalid User"));

        MenuPermissionsEntity menuPerms = menuPermissionsRepo.findByUsersId(id);
        if (menuPerms == null) {
            menuPerms = new MenuPermissionsEntity();
            menuPerms.setUsersId(id);
        }

        System.out.println("Updating menu permissions: " + permissions);
        
        menuPerms.setDashboard(permissions.getOrDefault("dashboard", 0));
        menuPerms.setAnalytics(permissions.getOrDefault("analytics", 0));
        menuPerms.setDocuments(permissions.getOrDefault("documents", 0));
        menuPerms.setSettings(permissions.getOrDefault("settings", 0));
        menuPerms.setFollow_ups(permissions.getOrDefault("follow_ups", 0));
        menuPerms.setReports(permissions.getOrDefault("reports", 0));
        menuPerms.setInvoices(permissions.getOrDefault("invoices", 0));
        menuPerms.setSales_clients(permissions.getOrDefault("sales_clients", 0));
        menuPerms.setSales_leads(permissions.getOrDefault("sales_leads", 0));
        menuPerms.setSales_estimation(permissions.getOrDefault("sales_estimation", 0));
        menuPerms.setProcurement_venders(permissions.getOrDefault("procurement_venders", 0));
        menuPerms.setProcurement_quotations_recived(permissions.getOrDefault("procurement_quotations_recived", 0));
        menuPerms.setProcurement_purchase_orders(permissions.getOrDefault("procurement_purchase_orders", 0));
        menuPerms.setProcurement_bills_received(permissions.getOrDefault("procurement_bills_received", 0));

        menuPermissionsRepo.save(menuPerms);

        return ResponseEntity.ok("Menu permissions updated successfully");
    }

    @Transactional
    public ResponseEntity<?> UpdatePagePermissions(Long id, Map<String, Object> requestData) throws CustomException {
        // Get the user
        UsersEntity user = usersRepo.findById(id)
                .orElseThrow(() -> new CustomException("Invalid User"));

        @SuppressWarnings("unchecked")
        List<Integer> permissionIds = (List<Integer>) requestData.get("permissionIds");
     
        if (permissionIds == null) {
            return ResponseEntity.badRequest().body("Permission IDs are required");
        }

        // Get the user's role name
        String roleName = user.getRole();
        if (roleName == null || roleName.isEmpty()) {
            return ResponseEntity.badRequest().body("User does not have a role assigned");
        }

        // Find the role ID from the roles table
        Integer roleId = rolesRepo.findRoleIdByName(roleName);
        if (roleId == null) {
            return ResponseEntity.badRequest()
                    .body("Role '" + roleName + "' not found in roles table");
        }

      
        // Delete existing role permissions
        rolePermissionsRepo.deleteByRoleId(roleId);

        // Add new permissions
        for (Integer permissionId : permissionIds) {
            rolePermissionsRepo.insertRolePermission(roleId, permissionId);
        }

        return ResponseEntity.ok("Page permissions updated successfully for role: " + roleName);
    }

	public boolean IsUserIdExist(String userid) {
		UsersEntity re=usersRepo.isUserIdExist(userid);
		System.err.println(re);
		if(re==null) {
			return false;
		}
		return true;
	}

	public ResponseEntity<String> AddNewUser(UsersEntity user) throws CustomException{
		
	
		
		List<String> errors = new ArrayList<>();

	    if (user.getCreated_by() == null) {
	        errors.add("Created by is required");
	    }

	    if (user.getUser_id() == null || user.getUser_id().trim().isEmpty()) {
	        errors.add("User ID is required");
	    }

	    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
	        errors.add("Email is required");
	    }

	    if (user.getName() == null || user.getName().trim().isEmpty()) {
	        errors.add("Name is required");
	    }

	    if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
	        errors.add("Password is required");
	    }

	    if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
	        errors.add("Phone number is required");
	    }

	    if (user.getRole() == null || user.getRole().trim().isEmpty()) {
	        errors.add("Role is required");
	    }
	    
	    if (user.getIs_active() == null) {
	        errors.add("User May Be Active");
	    }

	    if (!errors.isEmpty()) {
	        throw new CustomException(String.join(", ", errors));
	    }

	  
	    String encryptedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
	    user.setPassword(encryptedPassword);
	    
//	    MenuPermissionsEntity mp =menuPermissionsRepo.findByUsersId(user.getCreated_by());
//	    System.err.println(mp);
//	    System.err.println(extractPermissions(mp));
	    
    
//	    UsersEntity newUser = usersRepo.save(user);
//	    if (newUser == null) {
//	        throw new CustomException("Insertion Failed");
//	    }
//
//	    // 2. Fetch creator permissions
//	    MenuPermissionsEntity creatorPermissions = menuPermissionsRepo.findByUsersId(user.getCreated_by());
//
//	    if (creatorPermissions == null) {
//	        throw new CustomException("Creator menu permissions not found");
//	    }
//
//	    // 3. Clone permissions
//	    MenuPermissionsEntity newUserPermissions = new MenuPermissionsEntity();
//	    newUserPermissions.setUsersId(newUser.getId());
//
//	    newUserPermissions.setDashboard(creatorPermissions.getDashboard());
//	    newUserPermissions.setAnalytics(creatorPermissions.getAnalytics());
//	    newUserPermissions.setDocuments(creatorPermissions.getDocuments());
//	    newUserPermissions.setSettings(creatorPermissions.getSettings());
//	    newUserPermissions.setFollow_ups(creatorPermissions.getFollow_ups());
//	    newUserPermissions.setReports(creatorPermissions.getReports());
//	    newUserPermissions.setInvoices(creatorPermissions.getInvoices());
//	    newUserPermissions.setSales_clients(creatorPermissions.getSales_clients());
//	    newUserPermissions.setSales_leads(creatorPermissions.getSales_leads());
//	    newUserPermissions.setSales_estimation(creatorPermissions.getSales_estimation());
//	    newUserPermissions.setProcurement_venders(
//	            creatorPermissions.getProcurement_venders());
//	    newUserPermissions.setProcurement_quotations_recived(
//	            creatorPermissions.getProcurement_quotations_recived());
//	    newUserPermissions.setProcurement_purchase_orders(
//	            creatorPermissions.getProcurement_purchase_orders());
//	    newUserPermissions.setProcurement_bills_received(
//	            creatorPermissions.getProcurement_bills_received());
//
//	    // 4. Save permissions
//	    menuPermissionsRepo.save(newUserPermissions);
	       
	    return ResponseEntity.ok("New User Added Successfully");
		
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
	    if (p.getProcurement_quotations_recived() == 1) permissions.add("PROCUREMENT_QUOTATIONS");
	    if (p.getProcurement_purchase_orders() == 1) permissions.add("PROCUREMENT_PURCHASE_ORDERS");
	    if (p.getProcurement_bills_received() == 1) permissions.add("PROCUREMENT_BILLS");

	    return permissions;
	}
}