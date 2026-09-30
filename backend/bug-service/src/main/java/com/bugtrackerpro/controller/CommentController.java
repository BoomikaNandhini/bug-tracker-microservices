package com.bugtrackerpro.controller;

import com.bugtrackerpro.dto.CommentDTO;
import com.bugtrackerpro.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/bug/{bugId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByBug(@PathVariable Long bugId) {
        return ResponseEntity.ok(commentService.getCommentsByBug(bugId));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @RequestBody CommentDTO commentDTO,
            @RequestHeader("X-User-Id") Long currentUserId) {
        return ResponseEntity.ok(commentService.addComment(commentDTO, currentUserId));
    }
}
