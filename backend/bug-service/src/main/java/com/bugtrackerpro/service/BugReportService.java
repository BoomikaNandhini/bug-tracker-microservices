package com.bugtrackerpro.service;

import com.bugtrackerpro.dto.BugReportDTO;
import com.bugtrackerpro.dto.CreateBugDTO;
import com.bugtrackerpro.dto.BugHistoryDTO;
import com.bugtrackerpro.dto.BugAttachmentDTO;
import com.bugtrackerpro.entity.*;
import com.bugtrackerpro.feign.ProjectResponse;
import com.bugtrackerpro.feign.NotificationRequest;
import com.bugtrackerpro.feign.NotificationServiceClient;
import com.bugtrackerpro.feign.ProjectServiceClient;
import com.bugtrackerpro.feign.UserResponse;
import com.bugtrackerpro.feign.UserServiceClient;
import com.bugtrackerpro.repository.BugAttachmentRepository;
import com.bugtrackerpro.repository.BugHistoryRepository;
import com.bugtrackerpro.repository.BugReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j 
public class BugReportService {

    private final BugReportRepository bugReportRepository;
    private final BugHistoryRepository bugHistoryRepository;
    private final BugAttachmentRepository bugAttachmentRepository;

    // OpenFeign clients for inter-service calls
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public List<BugReportDTO> getAllBugs() {
        return bugReportRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BugReportDTO getBugById(Long id) {
        BugReport bug = bugReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found: " + id));
        return mapToDTO(bug);
    }

    public List<BugReportDTO> getBugsByProject(Long projectId) {
        return bugReportRepository.findByProjectId(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BugReportDTO> getBugsForUser(Long userId, String role) {
        List<BugReport> bugs = new ArrayList<>();
        if ("DEVELOPER".equalsIgnoreCase(role)) {
            bugs = bugReportRepository.findByAssignedToId(userId);
        } else if ("TESTER".equalsIgnoreCase(role)) {
            List<BugReport> assignedBugs = bugReportRepository.findByAssignedTesterId(userId);
            List<BugReport> reportedBugs = bugReportRepository.findByReportedById(userId);
            bugs = new ArrayList<>(assignedBugs);
            for (BugReport reported : reportedBugs) {
                if (bugs.stream().noneMatch(b -> b.getId().equals(reported.getId()))) {
                    bugs.add(reported);
                }
            }
        } else {
            bugs = bugReportRepository.findAll();
        }
        return bugs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<BugReportDTO> filterBugs(Long projectId, BugStatus status) {
        List<BugReport> all = bugReportRepository.findAll();
        return all.stream()
                .filter(b -> projectId == null || b.getProjectId().equals(projectId))
                .filter(b -> status == null || b.getStatus() == status)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a bug. Uses OpenFeign to call Project Service for project info.
     * currentUserId is passed in from the controller (it will come from the JWT header later).
     */
    public BugReportDTO createBug(CreateBugDTO bugDTO, Long currentUserId) {
        // --- OpenFeign Call #1: Validate project and get assigned members ---
        // ProjectResponse project = projectServiceClient.getProjectById(bugDTO.getProjectId());
    log.info("Inside Create Bug method");
         ProjectResponse project = new ProjectResponse();
        try{
         project = projectServiceClient.getProjectById(bugDTO.getProjectId());

       }catch(Exception ex){
         log.warn(
                    "Project Service is unavailable. " +
                    "Could not create the bug due to unavailable project service for the ",
                    bugDTO.getProjectId(),
                    currentUserId,
                    ex
            );
       }

        if (project.getAssignedDeveloperIds() == null || project.getAssignedDeveloperIds().isEmpty()) {
            throw new RuntimeException("No developers assigned to project: " + bugDTO.getProjectId());
        }

        Set<Long> assignedDeveloperIds = new LinkedHashSet<>(project.getAssignedDeveloperIds());
        Long primaryDeveloperId = assignedDeveloperIds.iterator().next();
        Long assignedTesterId = (project.getAssignedTesterIds() != null && !project.getAssignedTesterIds().isEmpty())
                ? project.getAssignedTesterIds().iterator().next()
                : null;

        BugReport bug = BugReport.builder()
                .title(bugDTO.getTitle())
                .description(bugDTO.getDescription())
                .status(BugStatus.NEW)
                .priority(bugDTO.getPriority())
                .projectId(project.getId())
                .reportedById(currentUserId)
                .assignedToId(primaryDeveloperId)
                .assignedDeveloperIds(assignedDeveloperIds)
                .assignedTesterId(assignedTesterId)
                .build();

        BugReport savedBug = bugReportRepository.save(bug);

        // --- OpenFeign Call #2: Get user name for history record ---
        // UserResponse currentUser = userServiceClient.getUserById(currentUserId);
        // recordHistory(savedBug, bugDTO.getDescription(), currentUser.getName(), null);
        try {
            UserResponse currentUser = userServiceClient.getUserById(currentUserId);

                recordHistory(
                        savedBug,
                        bugDTO.getDescription(),
                        currentUser.getName(),
                        null
                );

        } catch (Exception ex) {

            log.warn(
                    "User Service is unavailable. " +
                    "Bug was created successfully, but history could not be recorded " +
                    "with the user's name. bugId={}, userId={}",
                    savedBug.getId(),
                    currentUserId,
                    ex);
        }
        sendBugCreatedNotifications(savedBug, project);

        return mapToDTO(savedBug);
    }

    public BugReportDTO createBugWithAttachments(CreateBugDTO bugDTO, Long currentUserId, List<MultipartFile> files) {
      ProjectResponse project = new ProjectResponse();
        try{
         project = projectServiceClient.getProjectById(bugDTO.getProjectId());

       }catch(Exception ex){
         log.warn(
                    "Project Service is unavailable. " +
                    "Could not create the bug due to unavailable project service for the ",
                    bugDTO.getProjectId(),
                    currentUserId,
                    ex
            );
       }
       
        if (project.getAssignedDeveloperIds() == null || project.getAssignedDeveloperIds().isEmpty()) {
            throw new RuntimeException("No developers assigned to project: " + project.getName());
        }

        Set<Long> assignedDeveloperIds = new LinkedHashSet<>(project.getAssignedDeveloperIds());
        Long primaryDeveloperId = assignedDeveloperIds.iterator().next();
        Long assignedTesterId = (project.getAssignedTesterIds() != null && !project.getAssignedTesterIds().isEmpty())
                ? project.getAssignedTesterIds().iterator().next()
                : null;

        BugReport bug = BugReport.builder()
                .title(bugDTO.getTitle())
                .description(bugDTO.getDescription())
                .status(BugStatus.NEW)
                .priority(bugDTO.getPriority())
                .projectId(project.getId())
                .reportedById(currentUserId)
                .assignedToId(primaryDeveloperId)
                .assignedDeveloperIds(assignedDeveloperIds)
                .assignedTesterId(assignedTesterId)
                .build();

        BugReport savedBug = bugReportRepository.save(bug);
        
        // UserResponse currentUser = userServiceClient.getUserById(currentUserId);
        // recordHistory(savedBug, bugDTO.getDescription(), currentUser.getName(), files);
        try {
            UserResponse currentUser = userServiceClient.getUserById(currentUserId);
            recordHistory(
                    savedBug,
                    bugDTO.getDescription(),
                    currentUser.getName(),
                    files  // ← pass files here so attachments get stored
            );
        } catch (Exception ex) {
            log.warn(
                    "User Service is unavailable. Bug was created successfully, but history could not be recorded with the user's name. bugId={}, userId={}",
                    savedBug.getId(), currentUserId, ex);
        }
        sendBugCreatedNotifications(savedBug, project);

        return mapToDTO(savedBug);
    }

    public BugReportDTO startProgress(Long bugId, Long currentUserId) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        if (bug.getStatus() != BugStatus.NEW && bug.getStatus() != BugStatus.REASSIGNED) {
            throw new RuntimeException("Only NEW or REASSIGNED bugs can be started");
        }
        bug.setStatus(BugStatus.IN_PROGRESS);
        BugReport saved = bugReportRepository.save(bug);
        UserResponse user = userServiceClient.getUserById(currentUserId);
        recordHistory(saved, "Started progress", user.getName(), null);
        return mapToDTO(saved);
    }

    public BugReportDTO markFixed(Long bugId, Long currentUserId) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        if (bug.getStatus() != BugStatus.IN_PROGRESS) {
            throw new RuntimeException("Only IN_PROGRESS bugs can be marked as fixed");
        }
        boolean isAssignedDeveloper = bug.getAssignedDeveloperIds() != null
                && !bug.getAssignedDeveloperIds().isEmpty()
                && bug.getAssignedDeveloperIds().contains(currentUserId);

        // Backward compatibility for bugs created before multi-developer assignment.
        if (!isAssignedDeveloper && (bug.getAssignedToId() == null || !bug.getAssignedToId().equals(currentUserId))) {
            throw new RuntimeException("Only an assigned developer can mark this bug as fixed");
        }
        bug.setStatus(BugStatus.FIXED);
        BugReport saved = bugReportRepository.save(bug);
        UserResponse user = userServiceClient.getUserById(currentUserId);
        recordHistory(saved, "Marked as fixed", user.getName(), null);
        
        // Notify tester that bug is fixed
        if (bug.getAssignedTesterId() != null) {
            String eventId = java.util.UUID.randomUUID().toString();
            String message = String.format("Bug '%s' has been marked as FIXED by %s.", bug.getTitle(), user.getName());
            sendNotificationToUser(eventId, bug.getAssignedTesterId(), message);
        }
        
        return mapToDTO(saved);
    }

    public BugReportDTO resolveBug(Long bugId, Long currentUserId) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        if (bug.getStatus() != BugStatus.FIXED) {
            throw new RuntimeException("Only FIXED bugs can be resolved");
        }
        bug.setStatus(BugStatus.RESOLVED);
        BugReport saved = bugReportRepository.save(bug);
        UserResponse user = userServiceClient.getUserById(currentUserId);
        recordHistory(saved, "Resolved", user.getName(), null);
        return mapToDTO(saved);
    }

    public BugReportDTO reassignBug(Long bugId, String description, Long currentUserId, List<MultipartFile> files) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        bug.setStatus(BugStatus.REASSIGNED);
        BugReport saved = bugReportRepository.save(bug);
        UserResponse user = userServiceClient.getUserById(currentUserId);
        recordHistory(saved, description, user.getName(), files);

        // Notify developers and manager that bug is reassigned
        try {
            ProjectResponse project = projectServiceClient.getProjectById(bug.getProjectId());
            String eventId = java.util.UUID.randomUUID().toString();
            String message = String.format("Bug '%s' has been REASSIGNED by %s.", bug.getTitle(), user.getName());
            
            // Notify Developers
            if (bug.getAssignedDeveloperIds() != null) {
                for (Long devId : bug.getAssignedDeveloperIds()) {
                    sendNotificationToUser(eventId, devId, message);
                }
            }
            // Notify Manager
            if (project != null && project.getCreatedById() != null) {
                sendNotificationToUser(eventId, project.getCreatedById(), message);
            }
        } catch (Exception ex) {
            log.warn("Could not send reassign notifications for bugId={}", bugId, ex);
        }

        return mapToDTO(saved);
    }

    public BugReportDTO updateBug(Long id, BugReportDTO bugDTO) {
        BugReport bug = bugReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        bug.setTitle(bugDTO.getTitle());
        bug.setDescription(bugDTO.getDescription());
        bug.setStatus(bugDTO.getStatus());
        bug.setPriority(bugDTO.getPriority());
        if (bugDTO.getAssignedToId() != null) {
            bug.setAssignedToId(bugDTO.getAssignedToId());
            Set<Long> developerIds = new LinkedHashSet<>();
            developerIds.add(bugDTO.getAssignedToId());
            bug.setAssignedDeveloperIds(developerIds);
        }
        return mapToDTO(bugReportRepository.save(bug));
    }

    public void deleteBug(Long id) {
        bugReportRepository.deleteById(id);
    }

    public long countBugs() {
        return bugReportRepository.count();
    }

    public long countBugsByStatus(String statusStr) {
        if ("NOT_RESOLVED".equalsIgnoreCase(statusStr)) {
            return bugReportRepository.findAll().stream().filter(b -> b.getStatus() != BugStatus.RESOLVED).count();
        } else if ("RESOLVED".equalsIgnoreCase(statusStr)) {
            return bugReportRepository.findAll().stream().filter(b -> b.getStatus() == BugStatus.RESOLVED).count();
        }
        return 0;
    }

    public long countAssignedBugs(Long userId, String role) {
        if ("DEVELOPER".equalsIgnoreCase(role)) {
            return bugReportRepository.findByAssignedToId(userId).size();
        } else if ("TESTER".equalsIgnoreCase(role)) {
            return bugReportRepository.findByAssignedTesterId(userId).size();
        }
        return 0;
    }

    public long countRaisedBugs(Long userId) {
        return bugReportRepository.findByReportedById(userId).size();
    }

    public long countClosedRaisedBugs(Long userId) {
        return bugReportRepository.findByReportedById(userId).stream()
                .filter(b -> b.getStatus() == BugStatus.RESOLVED)
                .count();
    }

    public List<BugHistoryDTO> getBugHistory(Long bugId) {
        return bugHistoryRepository.findByBugIdOrderByTimestampDesc(bugId).stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Phase 8 (initial implementation): create notifications synchronously
     * through Notification Service using OpenFeign.
     *
     * Kafka is intentionally disabled for now. The same recipients and event
     * information can be moved to Kafka later without changing the
     * Notification Service business logic.
     */
    private void sendBugCreatedNotifications(BugReport bug, ProjectResponse project) {
        log.info("Inside sendBug Created notification method");

        String eventId = java.util.UUID.randomUUID().toString();
        String message = String.format("A new bug '%s' has been created in project '%s'.",
                bug.getTitle(), project.getName());

        // Notify every developer assigned to the project/bug.
        Set<Long> developerIds = bug.getAssignedDeveloperIds();
        if (developerIds != null) {
            for (Long developerId : developerIds) {
                sendNotificationToUser(eventId, developerId, message);
            }
        }

        // Preserve the existing notification to the project creator.
        sendNotificationToUser(eventId, project.getCreatedById(), message);
    }

    private void sendNotificationToUser(String eventId, Long userId, String message) {
        if (userId == null) {
            return;
        }

        try {
            UserResponse user = userServiceClient.getUserById(userId);

            NotificationRequest request = NotificationRequest.builder()
                    .eventId(eventId + "-" + userId)
                    .recipientId(user.getId())
                    .recipientName(user.getName())
                    .recipientEmail(user.getEmail())
                    .message(message)
                    .build();

            notificationServiceClient.createNotification(request);
        } catch (Exception ex) {
            // Notification failure must not undo a bug that has already been saved.
            org.slf4j.LoggerFactory.getLogger(BugReportService.class)
                    .warn("Could not create notification for userId={}. Bug creation will continue.", userId, ex);
        }
    }

    private void recordHistory(BugReport bug, String description, String changedByName, List<MultipartFile> files) {
        BugHistory history = BugHistory.builder()
                .bug(bug)
                .status(bug.getStatus())
                .changedById(bug.getReportedById())
                .changedByName(changedByName)
                .description(description)
                .build();

        BugHistory savedHistory = bugHistoryRepository.save(history);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    BugAttachment attachment = BugAttachment.builder()
                            .fileName(file.getOriginalFilename())
                            .fileType(file.getContentType())
                            .data(file.getBytes())
                            .bugHistory(savedHistory)
                            .build();
                    bugAttachmentRepository.save(attachment);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file", e);
                }
            }
        }
    }

    private BugHistoryDTO mapHistoryToDTO(BugHistory history) {
        String changedByRole = null;
        try {
            if (history.getChangedById() != null) {
                UserResponse user = userServiceClient.getUserById(history.getChangedById());
                changedByRole = user != null ? user.getRole() : null;
            }
        } catch (Exception ignored) { }

        return BugHistoryDTO.builder()
                .id(history.getId())
                .status(history.getStatus())
                .changedByName(history.getChangedByName())
                .changedByRole(changedByRole)
                .timestamp(history.getTimestamp())
                .description(history.getDescription())
                .attachments(history.getAttachments() != null
                        ? history.getAttachments().stream().map(this::mapAttachmentToDTO).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    private BugAttachmentDTO mapAttachmentToDTO(BugAttachment attachment) {
        return BugAttachmentDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .base64Data(Base64.getEncoder().encodeToString(attachment.getData()))
                .build();
    }

    private BugReportDTO mapToDTO(BugReport bug) {
        List<BugHistory> histories = bugHistoryRepository.findByBugIdOrderByTimestampDesc(bug.getId());
        List<BugAttachmentDTO> allAttachments = histories.stream()
                .flatMap(h -> h.getAttachments() != null ? h.getAttachments().stream() : java.util.stream.Stream.empty())
                .map(this::mapAttachmentToDTO)
                .collect(Collectors.toList());

        String projectName = null;
        UserResponse reportedByUser = null;
        UserResponse assignedToUser = null;

        try {
            ProjectResponse project = projectServiceClient.getProjectById(bug.getProjectId());
            projectName = project != null ? project.getName() : null;
        } catch (Exception ignored) { }

        try {
            if (bug.getReportedById() != null) {
                reportedByUser = userServiceClient.getUserById(bug.getReportedById());
            }
        } catch (Exception ignored) { }

        try {
            if (bug.getAssignedToId() != null) {
                assignedToUser = userServiceClient.getUserById(bug.getAssignedToId());
            }
        } catch (Exception ignored) { }

        return BugReportDTO.builder()
                .id(bug.getId())
                .title(bug.getTitle())
                .description(bug.getDescription())
                .status(bug.getStatus())
                .priority(bug.getPriority())
                .projectId(bug.getProjectId())
                .projectName(projectName)
                .reportedById(bug.getReportedById())
                .reportedBy(reportedByUser)
                .assignedToId(bug.getAssignedToId())
                .assignedTo(assignedToUser)
                .assignedDeveloperIds(bug.getAssignedDeveloperIds() != null
                        ? new java.util.ArrayList<>(bug.getAssignedDeveloperIds())
                        : new java.util.ArrayList<>())
                .createdAt(bug.getCreatedAt())
                .updatedAt(bug.getUpdatedAt())
                .attachments(allAttachments)
                .build();
    }
}
