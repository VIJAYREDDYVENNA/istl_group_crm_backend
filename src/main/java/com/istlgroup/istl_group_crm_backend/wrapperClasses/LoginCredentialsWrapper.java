package com.istlgroup.istl_group_crm_backend.wrapperClasses;

import lombok.Data;

@Data
public class LoginCredentialsWrapper {

	private Long id;
	
	private String name;
	private String role;
	private String user_id;
    private String email;
    private String phone;
	private Long is_active;
}
