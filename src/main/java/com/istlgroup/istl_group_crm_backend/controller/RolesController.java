package com.istlgroup.istl_group_crm_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.RolesEntity;
import com.istlgroup.istl_group_crm_backend.service.RolesService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.GetRolesWrapper;

@RestController
@RequestMapping("/roles")
//@CrossOrigin(origins = "${cros.allowed-origins}")
public class RolesController {

	@Autowired
	private RolesService rolesService;
	
	@GetMapping("/getAllRoles")
	public List<GetRolesWrapper> GetAllRoles() {
		return rolesService.GetAllRoles();
	}
	
	@PostMapping("/addNewRole")
	public String AddNewRole(@RequestBody RolesEntity newRole) throws CustomException{
		return rolesService.AddNewRole(newRole);
	}
}
