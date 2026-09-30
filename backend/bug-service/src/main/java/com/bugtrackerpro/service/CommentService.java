package com.bugtrackerpro.service;

import com.bugtrackerpro.dto.CommentDTO;
import com.bugtrackerpro.entity.BugReport;
import com.bugtrackerpro.entity.Comment;
import com.bugtrackerpro.feign.UserResponse;
import com.bugtrackerpro.feign.UserServiceClient;
import com.bugtrackerpro.repository.BugReportRepository;
import com.bugtrackerpro.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BugReportRepository bugReportRepository;
    private final UserServiceClient userServiceClient; // OpenFeign to get user info

    public List<CommentDTO> getCommentsByBug(Long bugId) {
        return commentRepository.findByBugId(bugId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO addComment(CommentDTO commentDTO, Long currentUserId) {
        BugReport bug = bugReportRepository.findById(commentDTO.getBugId())
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // OpenFeign call to user-service to get the user's name
        UserResponse user = userServiceClient.getUserById(currentUserId);

        Comment comment = Comment.builder()
                .bug(bug)
                .userId(currentUserId)
                .userName(user.getName())
                .comment(commentDTO.getComment())
                .build();

        return mapToDTO(commentRepository.save(comment));
    }

    private CommentDTO mapToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .bugId(comment.getBug().getId())
                .userId(comment.getUserId())
                .userName(comment.getUserName())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
