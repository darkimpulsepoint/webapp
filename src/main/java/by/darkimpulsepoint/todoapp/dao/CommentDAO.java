package by.darkimpulsepoint.todoapp.dao;

import by.darkimpulsepoint.todoapp.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentDAO {
    Comment create(Comment comment);
    List<Comment> findByTaskId(Long taskId);
    Optional<Comment> findById(Long id);
    boolean delete(Long id);
}
