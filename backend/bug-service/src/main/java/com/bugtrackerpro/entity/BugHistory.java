package com.bugtrackerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "bug_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BugHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id", nullable = false)
    private BugReport bug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugStatus status;

    // MICROSERVICES RULE: Store the user ID, not the full User entity
    @Column(name = "changed_by_id", nullable = false)
    private Long changedById;

    // Store the name directly to avoid calling User Service for every history read
    @Column(name = "changed_by_name")
    private String changedByName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "bugHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BugAttachment> attachments = new ArrayList<>();
}
