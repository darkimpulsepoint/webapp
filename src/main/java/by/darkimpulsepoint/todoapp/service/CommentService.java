package by.darkimpulsepoint.todoapp.service;


import by.darkimpulsepoint.todoapp.dao.CommentDAO;
import by.darkimpulsepoint.todoapp.dao.impl.CommentDAOImpl;
import by.darkimpulsepoint.todoapp.model.Comment;
import by.darkimpulsepoint.todoapp.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CommentService {
    private static final Logger logger = LogManager.getLogger(CommentService.class);
    private final CommentDAO commentDAO = new CommentDAOImpl();

    public Comment addComment(Long taskId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        logger.info("Adding comment to task {}", taskId);
        return commentDAO.create(comment);
    }

    public List<Comment> getCommentsForTask(Long taskId) {
        return commentDAO.findByTaskId(taskId);
    }

    public boolean deleteComment(Long commentId, User currentUser) {
        Comment comment = commentDAO.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (currentUser.getRole() != User.Role.ADMIN && !comment.getUserId().equals(currentUser.getId())) {
            throw new SecurityException("No permission to delete this comment");
        }
        logger.info("Deleting comment id={}", commentId);
        return commentDAO.delete(commentId);
    }
}
