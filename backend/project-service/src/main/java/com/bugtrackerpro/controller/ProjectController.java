package com.bugtrackerpro.controller;

import com.bugtrackerpro.dto.CreateProjectDTO;
import com.bugtrackerpro.dto.ProjectDTO;
import com.bugtrackerpro.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
            @RequestBody CreateProjectDTO projectDTO,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(projectService.createProject(projectDTO, currentUserId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectDTO>> getMyProjects(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(projectService.getProjectsForUser(currentUserId, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        projectService.deleteProject(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectDTO projectDTO,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDTO, currentUserId));
    }

    @GetMapping("/internal/count")
    public ResponseEntity<Long> countProjects() {
        return ResponseEntity.ok(projectService.countProjects());
    }

    @GetMapping("/internal/assigned-count")
    public ResponseEntity<Long> countAssignedProjects(
            @RequestParam Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(projectService.countAssignedProjects(userId, role));
    }
}
