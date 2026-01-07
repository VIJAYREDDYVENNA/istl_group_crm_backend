package com.istlgroup.istl_group_crm_backend.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.istlgroup.istl_group_crm_backend.entity.PermissionsEntity;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.GetRolesWrapper;

@Repository
public interface PermissionsRepo extends JpaRepository<PermissionsEntity,Integer>{
	

    @Query(value = """
        SELECT p.*
        FROM permissions p
        JOIN role_permissions rp ON p.id = rp.permission_id
        JOIN roles r ON rp.role_id = r.id
        WHERE r.id = :roleId
        """, nativeQuery = true)
    List<PermissionsEntity> findPermissionsByRoleId(@Param("roleId") Long userId);
    @Query(value = "SELECT id AS id, name AS name FROM permissions ORDER BY id ASC", nativeQuery = true)
	List<GetRolesWrapper> getAllPermissionsWithIds();
    
    @Query(value = "SELECT count(name) FROM permissions", nativeQuery = true)
	Integer findPermission(PermissionsEntity newPermissions);

}
