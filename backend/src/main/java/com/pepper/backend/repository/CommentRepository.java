package com.pepper.backend.repository;

import com.pepper.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    void deleteAllByPostId(Long postId);

    void deleteAllByUserId(Long userId);
}
