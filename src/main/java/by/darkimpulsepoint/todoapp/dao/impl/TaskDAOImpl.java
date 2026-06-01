package by.darkimpulsepoint.todoapp.dao.impl;

import by.darkimpulsepoint.todoapp.dao.TaskDAO;
import by.darkimpulsepoint.todoapp.model.Task;
import by.darkimpulsepoint.todoapp.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAOImpl implements TaskDAO {
    private static final Logger logger = LogManager.getLogger(TaskDAOImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String BASE_SELECT =
            "SELECT t.id, t.title, t.description, t.status, t.priority, t.user_id, u.username, " +
            "t.created_at, t.updated_at, t.due_date FROM tasks t JOIN users u ON t.user_id = u.id";

    private static final String INSERT_TASK =
            "INSERT INTO tasks (title, description, status, priority, user_id, due_date) " +
            "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
    private static final String SELECT_BY_ID = BASE_SELECT + " WHERE t.id = ?";
    private static final String SELECT_BY_USER = BASE_SELECT + " WHERE t.user_id = ? ORDER BY t.created_at DESC";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY t.created_at DESC";
    private static final String SELECT_BY_STATUS = BASE_SELECT + " WHERE t.status = ? ORDER BY t.created_at DESC";
    private static final String SELECT_BY_USER_STATUS = BASE_SELECT + " WHERE t.user_id = ? AND t.status = ? ORDER BY t.created_at DESC";
    private static final String UPDATE_TASK =
            "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, due_date = ?, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String UPDATE_STATUS =
            "UPDATE tasks SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String DELETE_TASK = "DELETE FROM tasks WHERE id = ?";
    private static final String COUNT_BY_USER = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
    private static final String COUNT_BY_STATUS = "SELECT COUNT(*) FROM tasks WHERE status = ?";

    @Override
    public Task create(Task task) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_TASK)) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getStatus().name());
                ps.setString(4, task.getPriority().name());
                ps.setLong(5, task.getUserId());
                ps.setTimestamp(6, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        task.setId(rs.getLong("id"));
                        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    }
                }
            }
            logger.debug("Created task: {}", task);
            return task;
        } catch (SQLException e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating task", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapTask(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding task by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error finding task", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Task> findByUserId(Long userId) {
        Connection conn = null;
        List<Task> tasks = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tasks.add(mapTask(rs));
                }
            }
            return tasks;
        } catch (SQLException e) {
            logger.error("Error finding tasks by user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error finding tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Task> findAll() {
        Connection conn = null;
        List<Task> tasks = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            logger.error("Error finding all tasks: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Task> findByStatus(Task.Status status) {
        Connection conn = null;
        List<Task> tasks = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STATUS)) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tasks.add(mapTask(rs));
                }
            }
            return tasks;
        } catch (SQLException e) {
            logger.error("Error finding tasks by status: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Task> findByUserIdAndStatus(Long userId, Task.Status status) {
        Connection conn = null;
        List<Task> tasks = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_STATUS)) {
                ps.setLong(1, userId);
                ps.setString(2, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tasks.add(mapTask(rs));
                }
            }
            return tasks;
        } catch (SQLException e) {
            logger.error("Error finding tasks by user and status: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Task update(Task task) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_TASK)) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getStatus().name());
                ps.setString(4, task.getPriority().name());
                ps.setTimestamp(5, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
                ps.setLong(6, task.getId());
                ps.executeUpdate();
            }
            logger.debug("Updated task: {}", task);
            return task;
        } catch (SQLException e) {
            logger.error("Error updating task {}: {}", task.getId(), e.getMessage(), e);
            throw new RuntimeException("Error updating task", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean updateStatus(Long id, Task.Status status) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {
                ps.setString(1, status.name());
                ps.setLong(2, id);
                int rows = ps.executeUpdate();
                logger.debug("Updated task {} status to {}", id, status);
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating task status {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error updating task status", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(DELETE_TASK)) {
                ps.setLong(1, id);
                int rows = ps.executeUpdate();
                logger.debug("Deleted task with id: {}", id);
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Error deleting task {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error deleting task", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public long countByUserId(Long userId) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(COUNT_BY_USER)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting tasks by user: {}", e.getMessage(), e);
            throw new RuntimeException("Error counting tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public long countByStatus(Task.Status status) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(COUNT_BY_STATUS)) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting tasks by status: {}", e.getMessage(), e);
            throw new RuntimeException("Error counting tasks", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(Task.Status.valueOf(rs.getString("status")));
        task.setPriority(Task.Priority.valueOf(rs.getString("priority")));
        task.setUserId(rs.getLong("user_id"));
        task.setUsername(rs.getString("username"));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        Timestamp due = rs.getTimestamp("due_date");
        if (due != null) task.setDueDate(due.toLocalDateTime());
        return task;
    }
}
