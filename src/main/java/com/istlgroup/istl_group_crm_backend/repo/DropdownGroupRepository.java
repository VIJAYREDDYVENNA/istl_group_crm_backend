package com.istlgroup.istl_group_crm_backend.repo;

import com.istlgroup.istl_group_crm_backend.entity.DropdownGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DropdownGroupRepository extends JpaRepository<DropdownGroupEntity, Long> {
    List<DropdownGroupEntity> findByIsActiveTrue();
    List<DropdownGroupEntity> findAll(); 
    Optional<DropdownGroupEntity> findByGroupName(String groupName);
}