package com.istlgroup.istl_group_crm_backend.wrapperClasses;

import java.util.List;

import lombok.Data;

@Data
public class LoginResponseWrapper {

	private LoginCredentialsWrapper user;
    private List<String> menuPermissions;
}
