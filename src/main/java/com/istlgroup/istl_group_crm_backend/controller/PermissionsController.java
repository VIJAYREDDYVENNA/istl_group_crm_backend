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
import com.istlgroup.istl_group_crm_backend.entity.PermissionsEntity;

import com.istlgroup.istl_group_crm_backend.service.PermissionsService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.GetRolesWrapper;

@RestController
@RequestMapping("/permissions")
//@CrossOrigin(origins = "${cros.allowed-origins}")
public class PermissionsController {

	@Autowired
	private PermissionsService permissionsService;
	
	@GetMapping("/getAllPermissions")
	public List<GetRolesWrapper> GetAllPermissions() {
		return permissionsService.GetAllPermissions();
	}
	@PostMapping("/addNewPermission")
	public String AddNewPermission(@RequestBody PermissionsEntity newPermission) throws CustomException{
		return permissionsService.AddNewPermission(newPermission);
	}
}
