package com.istlgroup.istl_group_crm_backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.istlgroup.istl_group_crm_backend.entity.LoginEntity;

public interface LoginRepo extends JpaRepository<LoginEntity,Long> {

	@Query("SELECT c FROM LoginEntity c WHERE (c.user_id = :username OR c.email = :username OR c.phone = :username) AND c.password = :password")
	public LoginEntity AuthenticateUser(@Param("username") String username, @Param("password") String password);
	

}
