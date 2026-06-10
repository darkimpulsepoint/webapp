package by.darkimpulsepoint.todoapp.service;

import by.darkimpulsepoint.todoapp.model.Comment;
import by.darkimpulsepoint.todoapp.model.User;

import java.util.List;

public interface CommentService {
    Comment addComment(Long taskId, Long userId, String content);
    List<Comment> getCommentsForTask(Long taskId);
    boolean deleteComment(Long commentId, User currentUser);
}
