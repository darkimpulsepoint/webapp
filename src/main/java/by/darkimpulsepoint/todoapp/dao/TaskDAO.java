package by.darkimpulsepoint.todoapp.dao;

import by.darkimpulsepoint.todoapp.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    Task create(Task task);
    Optional<Task> findById(Long id);
    List<Task> findByUserId(Long userId);
    List<Task> findAll();
    List<Task> findByStatus(Task.Status status);
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);
    Task update(Task task);
    boolean updateStatus(Long id, Task.Status status);
    boolean delete(Long id);
    long countByUserId(Long userId);
    long countByStatus(Task.Status status);
}
