package com.istlgroup.istl_group_crm_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.service.LoginService;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.LoginResponseWrapper;

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
}
