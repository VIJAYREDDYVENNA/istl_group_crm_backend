package com.istlgroup.istl_group_crm_backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.istlgroup.istl_group_crm_backend.customException.CustomException;
import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;
import com.istlgroup.istl_group_crm_backend.entity.UsersEntity;
import com.istlgroup.istl_group_crm_backend.repo.UsersRepo;

@Service
public class UsersService {

	@Autowired
	private UsersRepo usersRepo;
	
	public ResponseEntity<?> UpdateUser(LoginEntity newData, Long id) throws CustomException {
		
		UsersEntity isUserExist=usersRepo.findById(id).orElseThrow(()-> new CustomException("Invalid User"));

		isUserExist.setName(newData.getName());
		isUserExist.setEmail(newData.getEmail());
		isUserExist.setPhone(newData.getPhone());
		isUserExist.setRole(newData.getRole());
		isUserExist.setIs_active(newData.getIs_active());
		isUserExist.setUpdated_at(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		isUserExist.setUpdated_type("PROFILE_UPDATED");
		
		UsersEntity response=usersRepo.save(isUserExist);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Update Failed");
	    }
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Profile Details Updated Successfully");
	}

	public ResponseEntity<String> DeleteUser(Long id) throws CustomException {
		usersRepo.findById(id).orElseThrow(()-> new CustomException("Invalid User"));
		usersRepo.deleteById(id);
		 return ResponseEntity.ok("User deleted successfully");
	}
}
