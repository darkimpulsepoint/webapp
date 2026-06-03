package by.darkimpulsepoint.todoapp.dao;

import by.darkimpulsepoint.todoapp.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    Task create(Task task);
    Optional<Task> findById(Long id);
    List<Task> findByUserId(Long userId);
    List<Task> findAll();
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);
    Task update(Task task);
    boolean delete(Long id);
}
