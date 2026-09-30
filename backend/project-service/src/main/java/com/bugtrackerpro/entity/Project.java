package com.bugtrackerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // MICROSERVICES RULE: No @ManyToOne to User. Store the ID instead.
    @Column(name = "created_by_id", nullable = false)
    private Long createdById;

    @ElementCollection
    @CollectionTable(name = "project_developer_ids", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "developer_id")
    private Set<Long> assignedDeveloperIds;

    @ElementCollection
    @CollectionTable(name = "project_tester_ids", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tester_id")
    private Set<Long> assignedTesterIds;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
