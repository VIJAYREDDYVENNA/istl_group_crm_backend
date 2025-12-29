package com.istlgroup.istl_group_crm_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;
import com.istlgroup.istl_group_crm_backend.service.LoginService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.LoginResponseWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.UsersResponseWrapper;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "${cros.allowed-origins}")
public class LoginController {

	@Autowired
	private LoginService logingService;
	
	@PostMapping("/userLogin")
	public ResponseEntity<LoginResponseWrapper> Login(@RequestBody Map<String, String> credentials) throws CustomException {
		return logingService.AuthenticateUser(credentials);
	}
	
	@PutMapping("/updateUser/{id}")
	public ResponseEntity<?> UpdateUser(@RequestBody LoginEntity newData,@PathVariable Long id) throws CustomException {
		return logingService.UpdateUser(newData,id);
	}
	
	@PutMapping("/updatePassword/{id}")
	public ResponseEntity<String> UpdatePassword(@RequestBody Map<String, String> credentials,@PathVariable Long id) throws CustomException{
		return  logingService.UpdatePassword(credentials,id);
	}
	
	@GetMapping("/users/{userId}")
	public UsersResponseWrapper Users(@PathVariable Long userId)throws CustomException {
		return logingService.Users(userId);
	}
	
}
