package com.bugtrackerpro.service;

import com.bugtrackerpro.dto.CreateProjectDTO;
import com.bugtrackerpro.dto.ProjectDTO;
import com.bugtrackerpro.entity.Project;
import com.bugtrackerpro.feign.NotificationRequest;
import com.bugtrackerpro.feign.NotificationServiceClient;
import com.bugtrackerpro.feign.UserResponse;
import com.bugtrackerpro.feign.UserServiceClient;
import com.bugtrackerpro.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j 
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        log.info("Inside getProjectById method");
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToDTO(project);
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectDTO dto, Long currentUserId) {

        log.info("Insdie create project method");
        if (currentUserId == null) {
            throw new IllegalArgumentException("Authenticated user id is required");
        }

        Set<Long> developerIds = dto.getAssignedDeveloperIds() != null
                ? new LinkedHashSet<>(dto.getAssignedDeveloperIds())
                : new LinkedHashSet<>();

        Set<Long> testerIds = dto.getAssignedTesterIds() != null
                ? new LinkedHashSet<>(dto.getAssignedTesterIds())
                : new LinkedHashSet<>();

        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .createdById(currentUserId)
                .assignedDeveloperIds(developerIds)
                .assignedTesterIds(testerIds)
                .build();

        Project savedProject = projectRepository.save(project);

        // Phase 8: synchronous notification through OpenFeign.
        // Kafka is intentionally disabled and will be introduced later.
        sendProjectAssignmentNotifications(savedProject);

        return mapToDTO(savedProject);
    }

    /**
     * Sends one notification to every developer and tester assigned to the project.
     * If a user is present in both lists, only one notification is sent.
     * Notification failure must not roll back a successfully created project.
     */
    private void sendProjectAssignmentNotifications(Project project) {
        log.info("Inside send Project Assignment notification method");
        Set<Long> recipientIds = new LinkedHashSet<>();
        if (project.getAssignedDeveloperIds() != null) {
            recipientIds.addAll(project.getAssignedDeveloperIds());
        }
        if (project.getAssignedTesterIds() != null) {
            recipientIds.addAll(project.getAssignedTesterIds());
        }

        for (Long userId : recipientIds) {
            sendProjectNotification(project, userId);
        }
    }

    private void sendProjectNotification(Project project, Long userId) {
        if (userId == null) {
            return;
        }

        try {
            UserResponse user = userServiceClient.getUserById(userId);

            String role = "Team Member";
            if (project.getAssignedDeveloperIds() != null
                    && project.getAssignedDeveloperIds().contains(userId)) {
                role = "Developer";
            } else if (project.getAssignedTesterIds() != null
                    && project.getAssignedTesterIds().contains(userId)) {
                role = "Tester";
            }

            String message = String.format(
                    "You have been assigned to project '%s' as a %s.",
                    project.getName(), role);

            NotificationRequest request = NotificationRequest.builder()
                    .eventId("PROJECT_CREATED-" + project.getId() + "-" + userId)
                    .recipientId(user.getId())
                    .recipientName(user.getName())
                    .recipientEmail(user.getEmail())
                    .message(message)
                    .build();

            notificationServiceClient.createNotification(request);
        } catch (Exception ex) {
            log.warn(
                    "Could not create project assignment notification for userId={} and projectId={}. "
                            + "Project creation will continue.",
                    userId, project.getId(), ex);
        }
    }

    public List<ProjectDTO> getProjectsForUser(Long userId, String role) {
        if ("DEVELOPER".equalsIgnoreCase(role)) {
            return projectRepository.findByAssignedDeveloperIdsContaining(userId).stream()
                    .map(this::mapToDTO).collect(Collectors.toList());
        } else if ("TESTER".equalsIgnoreCase(role)) {
            return projectRepository.findByAssignedTesterIdsContaining(userId).stream()
                    .map(this::mapToDTO).collect(Collectors.toList());
        } else {
            return getAllProjects(); // Manager/Admin see all
        }
    }

    @Transactional
    public ProjectDTO updateProject(Long id, CreateProjectDTO dto, Long currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        
        Set<Long> developerIds = dto.getAssignedDeveloperIds() != null
                ? new LinkedHashSet<>(dto.getAssignedDeveloperIds())
                : new LinkedHashSet<>();
        Set<Long> testerIds = dto.getAssignedTesterIds() != null
                ? new LinkedHashSet<>(dto.getAssignedTesterIds())
                : new LinkedHashSet<>();
                
        project.setAssignedDeveloperIds(developerIds);
        project.setAssignedTesterIds(testerIds);
        
        return mapToDTO(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("Authenticated user id is required");
        }
        projectRepository.deleteById(id);
    }

    public long countProjects() {
        return projectRepository.count();
    }

    public long countAssignedProjects(Long userId, String role) {
        if ("DEVELOPER".equalsIgnoreCase(role)) {
            return projectRepository.findByAssignedDeveloperIdsContaining(userId).size();
        } else if ("TESTER".equalsIgnoreCase(role)) {
            return projectRepository.findByAssignedTesterIdsContaining(userId).size();
        }
        return 0;
    }

    private ProjectDTO mapToDTO(Project project) {
        ProjectDTO.ProjectDTOBuilder builder = ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdById(project.getCreatedById())
                .assignedDeveloperIds(project.getAssignedDeveloperIds())
                .assignedTesterIds(project.getAssignedTesterIds())
                .createdAt(project.getCreatedAt());

        try {
            if (project.getCreatedById() != null) {
                builder.createdBy(userServiceClient.getUserById(project.getCreatedById()));
            }
            if (project.getAssignedDeveloperIds() != null && !project.getAssignedDeveloperIds().isEmpty()) {
                Set<UserResponse> devs = project.getAssignedDeveloperIds().stream()
                    .map(id -> {
                        try { return userServiceClient.getUserById(id); }
                        catch (Exception e) { return UserResponse.builder().id(id).name("Unknown Developer").build(); }
                    }).collect(Collectors.toSet());
                builder.assignedDevelopers(devs);
            }
            if (project.getAssignedTesterIds() != null && !project.getAssignedTesterIds().isEmpty()) {
                Set<UserResponse> testers = project.getAssignedTesterIds().stream()
                    .map(id -> {
                        try { return userServiceClient.getUserById(id); }
                        catch (Exception e) { return UserResponse.builder().id(id).name("Unknown Tester").build(); }
                    }).collect(Collectors.toSet());
                builder.assignedTesters(testers);
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch user details for project {} mapping", project.getId(), ex);
        }

        return builder.build();
    }
}
