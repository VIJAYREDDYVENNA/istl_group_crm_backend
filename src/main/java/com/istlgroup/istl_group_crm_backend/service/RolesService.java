package com.istlgroup.istl_group_crm_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.RolesEntity;
import com.istlgroup.istl_group_crm_backend.repo.RolesRepo;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.GetRolesWrapper;

@Service
public class RolesService {

	@Autowired
	private RolesRepo rolesRepo;
	
	public List<GetRolesWrapper> GetAllRoles() {
		
		return rolesRepo.getAllRolesWithIds();
	}

	public String AddNewRole(RolesEntity newRole) throws CustomException{
		newRole.setCreated_at(LocalDateTime.now());
		Integer  isRoleExist=rolesRepo.findRole(newRole);
		
		if (isRoleExist > 0) {
	        throw new CustomException("Role already exist");
	    }

	    rolesRepo.save(newRole);
	    return "New Role Created Successfully";
		
	}

}
