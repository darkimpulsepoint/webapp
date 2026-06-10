package by.darkimpulsepoint.todoapp.service;

import by.darkimpulsepoint.todoapp.model.Task;
import by.darkimpulsepoint.todoapp.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {
    Task createTask(String title, String description, Task.Priority priority,
                    LocalDateTime dueDate, Long userId);
    Optional<Task> findById(Long id);
    List<Task> findTasksForUser(User user);
    List<Task> findByUserId(Long userId);
    List<Task> findAll();
    Task updateTask(Long taskId, String title, String description,
                    Task.Status status, Task.Priority priority,
                    LocalDateTime dueDate, User currentUser);
    boolean updateStatus(Long taskId, Task.Status status, User currentUser);
    boolean deleteTask(Long taskId, User currentUser);
    Map<String, Long> getStatistics();
    Map<String, Long> getStatisticsForUser(Long userId);
}
