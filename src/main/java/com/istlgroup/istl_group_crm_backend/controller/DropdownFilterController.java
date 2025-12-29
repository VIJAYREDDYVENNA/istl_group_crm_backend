package com.istlgroup.istl_group_crm_backend.controller;


import com.istlgroup.istl_group_crm_backend.wrapperClasses.DropdownGroupWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.DropdownProjectWrapper;
import com.istlgroup.istl_group_crm_backend.wrapperClasses.DropdownSubGroupWrapper;
import com.istlgroup.istl_group_crm_backend.service.DropdownFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cros.allowed-origins}")
public class DropdownFilterController {
    
    private final DropdownFilterService filterService;
    
    @GetMapping("/groups")
    public ResponseEntity<List<DropdownGroupWrapper>> getAllGroups() {
        return ResponseEntity.ok(filterService.getAllGroups());
    }
    
    @GetMapping("/subgroups")
    public ResponseEntity<List<DropdownSubGroupWrapper>> getSubGroups(
            @RequestParam String groupName) {
        return ResponseEntity.ok(filterService.getSubGroupsByGroup(groupName));
    }
    
    @GetMapping("/projects")
    public ResponseEntity<List<DropdownProjectWrapper>> getProjects(
            @RequestParam String groupName,
            @RequestParam String subGroupName) {
        return ResponseEntity.ok(
            filterService.getProjectsByGroupAndSubGroup(groupName, subGroupName)
        );
    }
}
