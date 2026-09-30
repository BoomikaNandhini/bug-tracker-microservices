package com.bugtrackerpro.repository;

import com.bugtrackerpro.entity.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BugReportRepository extends JpaRepository<BugReport, Long> {
    List<BugReport> findByProjectId(Long projectId);

    List<BugReport> findByAssignedToId(Long userId);

    List<BugReport> findByReportedById(Long userId);

    List<BugReport> findByAssignedTesterId(Long testerId);
}
