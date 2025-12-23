package com.istlgroup.istl_group_crm_backend.service;


import com.istlgroup.istl_group_crm_backend.entity.DropdownGroupEntity;
import com.istlgroup.istl_group_crm_backend.entity.DropdownProjectEntity;
import com.istlgroup.istl_group_crm_backend.entity.DropdownSubGroupEntity;
import com.istlgroup.istl_group_crm_backend.repo.DropdownGroupRepository;
import com.istlgroup.istl_group_crm_backend.repo.DropdownProjectRepository;
import com.istlgroup.istl_group_crm_backend.repo.DropdownSubGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DropdownAdminService {
    
    private final DropdownGroupRepository groupRepository;
    private final DropdownSubGroupRepository subGroupRepository;
    private final DropdownProjectRepository projectRepository;
    
    // ============ GROUP OPERATIONS ============
    
    public List<DropdownGroupEntity> getAllGroupsAdmin() {
        return groupRepository.findAll();
    }
    
    public DropdownGroupEntity getGroupById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
    }
    
    @Transactional
    public DropdownGroupEntity createGroup(DropdownGroupEntity group) {
        return groupRepository.save(group);
    }
    
    @Transactional
    public DropdownGroupEntity updateGroup(Long id, DropdownGroupEntity updatedGroup) {
        DropdownGroupEntity existingGroup = getGroupById(id);
        
        existingGroup.setGroupName(updatedGroup.getGroupName());
        existingGroup.setGroupLabel(updatedGroup.getGroupLabel());
        existingGroup.setDescription(updatedGroup.getDescription());
        existingGroup.setIsActive(updatedGroup.getIsActive());
        
        return groupRepository.save(existingGroup);
    }
    
    @Transactional
    public void deleteGroup(Long id) {
        DropdownGroupEntity group = getGroupById(id);
        group.setIsActive(false);
        groupRepository.save(group);
    }
    
    // ============ SUBGROUP OPERATIONS ============
    
    public List<DropdownSubGroupEntity> getAllSubGroupsAdmin() {
        return subGroupRepository.findAll();
    }
    
    public DropdownSubGroupEntity getSubGroupById(Long id) {
        return subGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("SubGroup not found with id: " + id));
    }
    
    @Transactional
    public DropdownSubGroupEntity createSubGroup(DropdownSubGroupEntity subGroup, Long groupId) {
        DropdownGroupEntity group = getGroupById(groupId);
        subGroup.setGroup(group);
        return subGroupRepository.save(subGroup);
    }
    
    @Transactional
    public DropdownSubGroupEntity updateSubGroup(Long id, DropdownSubGroupEntity updatedSubGroup) {
        DropdownSubGroupEntity existingSubGroup = getSubGroupById(id);
        
        existingSubGroup.setSubGroupName(updatedSubGroup.getSubGroupName());
        existingSubGroup.setSubGroupLabel(updatedSubGroup.getSubGroupLabel());
        existingSubGroup.setDescription(updatedSubGroup.getDescription());
        existingSubGroup.setIsActive(updatedSubGroup.getIsActive());
        
        return subGroupRepository.save(existingSubGroup);
    }
    
    @Transactional
    public void deleteSubGroup(Long id) {
        DropdownSubGroupEntity subGroup = getSubGroupById(id);
        subGroup.setIsActive(false);
        subGroupRepository.save(subGroup);
    }
    
    // ============ PROJECT OPERATIONS ============
    
    public List<DropdownProjectEntity> getAllProjectsAdmin() {
        return projectRepository.findAll();
    }
    
    public DropdownProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }
}
