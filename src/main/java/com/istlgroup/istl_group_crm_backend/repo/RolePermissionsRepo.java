package com.istlgroup.istl_group_crm_backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.istlgroup.istl_group_crm_backend.entity.RolePermissionsEntity;

@Repository
public interface RolePermissionsRepo extends JpaRepository<RolePermissionsEntity, Long> {
    
    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
    void deleteByRoleId(@Param("roleId") Integer roleId);
    
    @Modifying
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)", nativeQuery = true)
    void insertRolePermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);
}