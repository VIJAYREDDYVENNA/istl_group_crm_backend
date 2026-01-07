package com.istlgroup.istl_group_crm_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.PermissionsEntity;
import com.istlgroup.istl_group_crm_backend.repo.PermissionsRepo;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.GetRolesWrapper;

@Service
public class PermissionsService {

	@Autowired
	private PermissionsRepo permissionsRepo;
	
	public List<GetRolesWrapper> GetAllPermissions() {
		
		return permissionsRepo.getAllPermissionsWithIds();
	}

	public String AddNewPermission(PermissionsEntity newPermissions) throws CustomException{
		newPermissions.setCreated_at(LocalDateTime.now());
		Integer  isPermissionExist=permissionsRepo.findPermission(newPermissions);
		
		if (isPermissionExist > 0) {
	        throw new CustomException("Permission already exist");
	    }
		permissionsRepo.save(newPermissions);
	    return "New Permission Created Successfully";
	}

}
