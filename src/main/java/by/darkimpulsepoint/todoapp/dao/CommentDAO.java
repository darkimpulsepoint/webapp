package by.darkimpulsepoint.todoapp.dao;

import by.darkimpulsepoint.todoapp.model.Comment;
import by.darkimpulsepoint.todoapp.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentDAO {
    private static final Logger logger = LogManager.getLogger(CommentDAO.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String INSERT =
            "INSERT INTO comments (task_id, user_id, content) VALUES (?, ?, ?) RETURNING id, created_at";
    private static final String SELECT_BY_TASK =
            "SELECT c.id, c.task_id, c.user_id, u.username, c.content, c.created_at " +
            "FROM comments c JOIN users u ON c.user_id = u.id WHERE c.task_id = ? ORDER BY c.created_at ASC";
    private static final String SELECT_BY_ID =
            "SELECT c.id, c.task_id, c.user_id, u.username, c.content, c.created_at " +
            "FROM comments c JOIN users u ON c.user_id = u.id WHERE c.id = ?";
    private static final String DELETE = "DELETE FROM comments WHERE id = ?";

    public Comment create(Comment comment) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
                ps.setLong(1, comment.getTaskId());
                ps.setLong(2, comment.getUserId());
                ps.setString(3, comment.getContent());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        comment.setId(rs.getLong("id"));
                        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                }
            }
            logger.debug("Created comment id={}", comment.getId());
            return comment;
        } catch (SQLException e) {
            logger.error("Error creating comment: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating comment", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    public List<Comment> findByTaskId(Long taskId) {
        Connection conn = null;
        List<Comment> list = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_TASK)) {
                ps.setLong(1, taskId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapComment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding comments by task {}: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Error finding comments", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    public Optional<Comment> findById(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapComment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding comment by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error finding comment", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    public boolean delete(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(DELETE)) {
                ps.setLong(1, id);
                int rows = ps.executeUpdate();
                logger.debug("Deleted comment id={}", id);
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Error deleting comment {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error deleting comment", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Comment mapComment(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getLong("id"));
        c.setTaskId(rs.getLong("task_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setUsername(rs.getString("username"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return c;
    }
}
