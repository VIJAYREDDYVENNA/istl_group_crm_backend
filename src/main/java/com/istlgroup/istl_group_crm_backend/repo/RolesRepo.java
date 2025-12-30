package com.istlgroup.istl_group_crm_backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.istlgroup.istl_group_crm_backend.entity.RolesEntity;

@Repository
public interface RolesRepo extends JpaRepository<RolesEntity, Integer> {
    
    @Query(value = "SELECT id FROM roles WHERE name = :roleName", nativeQuery = true)
    Integer findRoleIdByName(@Param("roleName") String roleName);
}