package com.bugtrackerpro.controller;

import com.bugtrackerpro.dto.BugReportDTO;
import com.bugtrackerpro.dto.CreateBugDTO;
import com.bugtrackerpro.dto.BugHistoryDTO;
import com.bugtrackerpro.entity.BugStatus;
import com.bugtrackerpro.service.BugReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugReportService bugReportService;

    @GetMapping
    public ResponseEntity<List<BugReportDTO>> getAllBugs(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) BugStatus status) {
        if (projectId != null || status != null) {
            return ResponseEntity.ok(bugReportService.filterBugs(projectId, status));
        }
        return ResponseEntity.ok(bugReportService.getAllBugs());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BugReportDTO>> getMyBugs(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(bugReportService.getBugsForUser(currentUserId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BugReportDTO> getBugById(@PathVariable Long id) {
        return ResponseEntity.ok(bugReportService.getBugById(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<BugReportDTO>> getBugsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(bugReportService.getBugsByProject(projectId));
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BugReportDTO> createBug(
            @RequestPart("bug") CreateBugDTO bugDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long currentUserId) {
        if (files != null && !files.isEmpty()) {
            return ResponseEntity.ok(bugReportService.createBugWithAttachments(bugDTO, currentUserId, files));
        }
        return ResponseEntity.ok(bugReportService.createBug(bugDTO, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BugReportDTO> updateBug(@PathVariable Long id, @RequestBody BugReportDTO bugDTO) {
        return ResponseEntity.ok(bugReportService.updateBug(id, bugDTO));
    }

    @PutMapping("/{id}/start-progress")
    public ResponseEntity<BugReportDTO> startProgressAlias(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.startProgress(id, currentUserId));
    }

    // Alias used by Angular frontend
    @PutMapping("/{id}/start")
    public ResponseEntity<BugReportDTO> startProgress(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.startProgress(id, currentUserId));
    }

    @PutMapping("/{id}/mark-fixed")
    public ResponseEntity<BugReportDTO> markFixedAlias(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.markFixed(id, currentUserId));
    }

    // Alias used by Angular frontend
    @PutMapping("/{id}/fix")
    public ResponseEntity<BugReportDTO> markFixed(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.markFixed(id, currentUserId));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<BugReportDTO> resolveBug(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.resolveBug(id, currentUserId));
    }

    // Reassign: both PUT (frontend) and POST variants
    @PutMapping(value = "/{id}/reassign", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BugReportDTO> reassignBugPut(
            @PathVariable Long id,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.reassignBug(id, description, currentUserId, files));
    }

    @PostMapping(value = "/{id}/reassign", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BugReportDTO> reassignBugPost(
            @PathVariable Long id,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(bugReportService.reassignBug(id, description, currentUserId, files));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        bugReportService.deleteBug(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<BugHistoryDTO>> getBugHistory(@PathVariable Long id) {
        return ResponseEntity.ok(bugReportService.getBugHistory(id));
    }

    @GetMapping("/internal/count")
    public ResponseEntity<Long> countBugs() {
        return ResponseEntity.ok(bugReportService.countBugs());
    }

    @GetMapping("/internal/count/status")
    public ResponseEntity<Long> countBugsByStatus(@RequestParam String status) {
        return ResponseEntity.ok(bugReportService.countBugsByStatus(status));
    }

    @GetMapping("/internal/assigned-count")
    public ResponseEntity<Long> countAssignedBugs(
            @RequestParam Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(bugReportService.countAssignedBugs(userId, role));
    }

    @GetMapping("/internal/raised-count")
    public ResponseEntity<Long> countRaisedBugs(@RequestParam Long userId) {
        return ResponseEntity.ok(bugReportService.countRaisedBugs(userId));
    }

    @GetMapping("/internal/raised-closed-count")
    public ResponseEntity<Long> countClosedRaisedBugs(@RequestParam Long userId) {
        return ResponseEntity.ok(bugReportService.countClosedRaisedBugs(userId));
    }
}
