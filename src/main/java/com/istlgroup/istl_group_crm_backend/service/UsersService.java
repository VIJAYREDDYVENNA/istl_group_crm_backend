package com.istlgroup.istl_group_crm_backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

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
}