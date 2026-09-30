package com.bugtrackerpro.repository;

import com.bugtrackerpro.entity.BugAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BugAttachmentRepository extends JpaRepository<BugAttachment, Long> {
}
