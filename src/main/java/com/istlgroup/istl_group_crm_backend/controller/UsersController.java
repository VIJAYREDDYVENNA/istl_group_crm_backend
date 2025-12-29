package com.istlgroup.istl_group_crm_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;
import com.istlgroup.istl_group_crm_backend.service.UsersService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "${cros.allowed-origins}")
public class UsersController {

	@Autowired
	private UsersService usersService;
	
	@PostMapping("/updateUser/{id}")
	public ResponseEntity<?> UpdateUser(@RequestBody LoginEntity newData,@PathVariable Long id) throws CustomException {
		return usersService.UpdateUser(newData,id);
	}
	@DeleteMapping("/deleteUser/{id}")
	public ResponseEntity<String> DeleteUser(@PathVariable Long id) throws CustomException {
		return usersService.DeleteUser(id);
	}
	
}
