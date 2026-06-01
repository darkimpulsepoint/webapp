package by.darkimpulsepoint.todoapp.service;

import by.darkimpulsepoint.todoapp.dao.TaskDAO;
import by.darkimpulsepoint.todoapp.dao.impl.TaskDAOImpl;
import by.darkimpulsepoint.todoapp.model.Task;
import by.darkimpulsepoint.todoapp.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskService {
    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private final TaskDAO taskDAO = new TaskDAOImpl();

    public Task createTask(String title, String description, Task.Priority priority,
                           LocalDateTime dueDate, Long userId) {
        logger.info("Creating task '{}' for user {}", title, userId);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        Task task = new Task();
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : "");
        task.setStatus(Task.Status.TODO);
        task.setPriority(priority != null ? priority : Task.Priority.MEDIUM);
        task.setUserId(userId);
        task.setDueDate(dueDate);
        Task created = taskDAO.create(task);
        logger.info("Task created: id={}", created.getId());
        return created;
    }

    public Optional<Task> findById(Long id) {
        return taskDAO.findById(id);
    }

    public List<Task> findTasksForUser(User user) {
        if (user.getRole() == User.Role.ADMIN) {
            return taskDAO.findAll();
        }
        return taskDAO.findByUserId(user.getId());
    }

    public List<Task> findByUserId(Long userId) {
        return taskDAO.findByUserId(userId);
    }

    public List<Task> findAll() {
        return taskDAO.findAll();
    }

    public Task updateTask(Long taskId, String title, String description,
                           Task.Status status, Task.Priority priority,
                           LocalDateTime dueDate, User currentUser) {
        Task task = taskDAO.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!canModify(task, currentUser)) {
            throw new SecurityException("You do not have permission to modify this task");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : "");
        task.setStatus(status != null ? status : task.getStatus());
        task.setPriority(priority != null ? priority : task.getPriority());
        task.setDueDate(dueDate);
        logger.info("Updating task id={}", taskId);
        return taskDAO.update(task);
    }

    public boolean updateStatus(Long taskId, Task.Status status, User currentUser) {
        Task task = taskDAO.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!canModify(task, currentUser)) {
            throw new SecurityException("No permission");
        }
        logger.info("Updating status of task {} to {}", taskId, status);
        return taskDAO.updateStatus(taskId, status);
    }

    public boolean deleteTask(Long taskId, User currentUser) {
        Task task = taskDAO.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!canModify(task, currentUser)) {
            throw new SecurityException("You do not have permission to delete this task");
        }
        logger.info("Deleting task id={}", taskId);
        return taskDAO.delete(taskId);
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) taskDAO.findAll().size());
        for (Task.Status s : Task.Status.values()) {
            stats.put(s.name().toLowerCase(), taskDAO.countByStatus(s));
        }
        return stats;
    }

    public Map<String, Long> getStatisticsForUser(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", taskDAO.countByUserId(userId));
        for (Task.Status s : Task.Status.values()) {
            stats.put(s.name().toLowerCase(), (long) taskDAO.findByUserIdAndStatus(userId, s).size());
        }
        return stats;
    }

    private boolean canModify(Task task, User user) {
        return user.getRole() == User.Role.ADMIN || task.getUserId().equals(user.getId());
    }
}
