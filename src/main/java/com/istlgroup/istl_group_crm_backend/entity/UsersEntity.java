package com.istlgroup.istl_group_crm_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class UsersEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user_id;
    private Long created_by;
    private String email;
    private String password;
    private String name;
    private String phone;
    private Long is_active;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String updated_type;
    private LocalDateTime last_login_at;
    private String role;
    private String avatar_url;
}
