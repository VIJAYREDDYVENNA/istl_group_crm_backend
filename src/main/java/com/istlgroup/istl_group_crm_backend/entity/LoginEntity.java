package com.istlgroup.istl_group_crm_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class LoginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 255)
    private String user_id;

    @Column(name = "created_by")
    private Long created_by;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    // If you are actually using this column
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "phone", length = 255)
    private String phone;

//    @Column(name = "avatar_url", length = 1024)
//    private String avatarUrl;


    @Column(name = "is_active")
    private Long isActive;

//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 255)
    private String lastLoginIp;

    @Column(name = "role", nullable = false, length = 50)
    private String role;
}
