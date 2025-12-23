package com.istlgroup.istl_group_crm_backend.controller;


import com.istlgroup.istl_group_crm_backend.entity.DropdownProjectEntity;
import com.istlgroup.istl_group_crm_backend.service.DropdownFilterService;
import com.istlgroup.istl_group_crm_backend.service.DropdownProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DropdownProjectController {
    
    private final DropdownProjectService projectService;
    private final DropdownFilterService filterService;
    
    @GetMapping("/{projectUniqueId}")
    public ResponseEntity<DropdownProjectEntity> getProject(@PathVariable String projectUniqueId) {
        return ResponseEntity.ok(filterService.getProjectByUniqueId(projectUniqueId));
    }
    
    @PostMapping
    public ResponseEntity<DropdownProjectEntity> createProject(
            @RequestBody DropdownProjectEntity project,
            @RequestParam Long subGroupId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectService.createProject(project, subGroupId));
    }
    
    @PutMapping("/{projectUniqueId}")
    public ResponseEntity<DropdownProjectEntity> updateProject(
            @PathVariable String projectUniqueId,
            @RequestBody DropdownProjectEntity project) {
        return ResponseEntity.ok(projectService.updateProject(projectUniqueId, project));
    }
    
    @DeleteMapping("/{projectUniqueId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectUniqueId) {
        projectService.deleteProject(projectUniqueId);
        return ResponseEntity.noContent().build();
    }
}
