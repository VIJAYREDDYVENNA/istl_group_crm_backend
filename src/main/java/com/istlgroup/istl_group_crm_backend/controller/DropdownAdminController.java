package com.istlgroup.istl_group_crm_backend.controller;


import com.istlgroup.istl_group_crm_backend.entity.DropdownGroupEntity;
import com.istlgroup.istl_group_crm_backend.entity.DropdownProjectEntity;
import com.istlgroup.istl_group_crm_backend.entity.DropdownSubGroupEntity;
import com.istlgroup.istl_group_crm_backend.service.DropdownAdminService;
import com.istlgroup.istl_group_crm_backend.service.DropdownProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dropdowns")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cros.allowed-origins}")
public class DropdownAdminController {
    
    private final DropdownAdminService adminService;
    private final DropdownProjectService projectService;
    
    // ============ GROUP ENDPOINTS ============
    
    @GetMapping("/groups")
    public ResponseEntity<List<DropdownGroupEntity>> getAllGroups() {
        return ResponseEntity.ok(adminService.getAllGroupsAdmin());
    }
    
    @GetMapping("/groups/{id}")
    public ResponseEntity<DropdownGroupEntity> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getGroupById(id));
    }
    
    @PostMapping("/groups")
    public ResponseEntity<DropdownGroupEntity> createGroup(@RequestBody DropdownGroupEntity group) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createGroup(group));
    }
    
    @PutMapping("/groups/{id}")
    public ResponseEntity<DropdownGroupEntity> updateGroup(
            @PathVariable Long id, 
            @RequestBody DropdownGroupEntity group) {
        return ResponseEntity.ok(adminService.updateGroup(id, group));
    }
    
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        adminService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // ============ SUBGROUP ENDPOINTS ============
    
    @GetMapping("/subgroups")
    public ResponseEntity<List<DropdownSubGroupEntity>> getAllSubGroups() {
        return ResponseEntity.ok(adminService.getAllSubGroupsAdmin());
    }
    
    @GetMapping("/subgroups/{id}")
    public ResponseEntity<DropdownSubGroupEntity> getSubGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getSubGroupById(id));
    }
    
    @PostMapping("/subgroups")
    public ResponseEntity<DropdownSubGroupEntity> createSubGroup(
            @RequestBody DropdownSubGroupEntity subGroup,
            @RequestParam Long groupId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminService.createSubGroup(subGroup, groupId));
    }
    
    @PutMapping("/subgroups/{id}")
    public ResponseEntity<DropdownSubGroupEntity> updateSubGroup(
            @PathVariable Long id, 
            @RequestBody DropdownSubGroupEntity subGroup) {
        return ResponseEntity.ok(adminService.updateSubGroup(id, subGroup));
    }
    
    @DeleteMapping("/subgroups/{id}")
    public ResponseEntity<Void> deleteSubGroup(@PathVariable Long id) {
        adminService.deleteSubGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    // ============ PROJECT ENDPOINTS ============
    
    @GetMapping("/projects")
    public ResponseEntity<List<DropdownProjectEntity>> getAllProjects() {
        return ResponseEntity.ok(adminService.getAllProjectsAdmin());
    }
    
    @GetMapping("/projects/{id}")
    public ResponseEntity<DropdownProjectEntity> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getProjectById(id));
    }
    
    @PostMapping("/projects")
    public ResponseEntity<DropdownProjectEntity> createProject(
            @RequestBody DropdownProjectEntity project,
            @RequestParam Long subGroupId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectService.createProject(project, subGroupId));
    }
    
    @PutMapping("/projects/{projectUniqueId}")
    public ResponseEntity<DropdownProjectEntity> updateProject(
            @PathVariable String projectUniqueId,
            @RequestBody DropdownProjectEntity project) {
        return ResponseEntity.ok(projectService.updateProject(projectUniqueId, project));
    }
    
    @DeleteMapping("/projects/{projectUniqueId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectUniqueId) {
        projectService.deleteProject(projectUniqueId);
        return ResponseEntity.noContent().build();
    }
}