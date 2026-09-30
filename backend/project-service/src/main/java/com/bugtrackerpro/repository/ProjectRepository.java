package com.bugtrackerpro.repository;

import com.bugtrackerpro.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // We changed the entity to store IDs instead of the full User object
    List<Project> findByAssignedDeveloperIdsContaining(Long developerId);
    List<Project> findByAssignedTesterIdsContaining(Long testerId);
}
