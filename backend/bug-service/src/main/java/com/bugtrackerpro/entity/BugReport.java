package com.bugtrackerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "bug_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugPriority priority;

    // MICROSERVICES RULE: Store foreign IDs, not full User/Project entities.
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "reported_by_id", nullable = false)
    private Long reportedById;

    /**
     * Backward-compatible primary assignee. For newly created bugs this is the
     * first developer in assignedDeveloperIds. Existing frontend code can keep
     * using this field until the UI is migrated to the collection.
     */
    @Column(name = "assigned_to_id")
    private Long assignedToId;

    /**
     * All developers assigned to this bug. This mirrors the developers assigned
     * to the project at the time the bug is created.
     */
    @ElementCollection
    @CollectionTable(name = "bug_assigned_developer_ids", joinColumns = @JoinColumn(name = "bug_id"))
    @Column(name = "developer_id")
    @Builder.Default
    private Set<Long> assignedDeveloperIds = new LinkedHashSet<>();

    // Kept for compatibility with the existing tester workflow.
    @Column(name = "assigned_tester_id")
    private Long assignedTesterId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
