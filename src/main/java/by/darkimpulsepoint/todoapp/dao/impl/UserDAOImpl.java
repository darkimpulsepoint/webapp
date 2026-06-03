package by.darkimpulsepoint.todoapp.dao.impl;

import by.darkimpulsepoint.todoapp.dao.UserDAO;
import by.darkimpulsepoint.todoapp.model.User;
import by.darkimpulsepoint.todoapp.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String INSERT_USER =
            "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?) RETURNING id, created_at";
    private static final String SELECT_BY_ID =
            "SELECT id, username, email, password_hash, role, created_at FROM users WHERE id = ?";
    private static final String SELECT_BY_USERNAME =
            "SELECT id, username, email, password_hash, role, created_at FROM users WHERE username = ?";
    private static final String SELECT_BY_EMAIL =
            "SELECT id, username, email, password_hash, role, created_at FROM users WHERE email = ?";
    private static final String SELECT_ALL =
            "SELECT id, username, email, password_hash, role, created_at FROM users ORDER BY created_at DESC";
    private static final String UPDATE_USER =
            "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ? WHERE id = ?";
    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";

    @Override
    public User create(User user) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_USER)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getRole().name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        user.setId(rs.getLong("id"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                }
            }
            logger.debug("Created user: {}", user);
            return user;
        } catch (SQLException e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating user", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error finding user", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error finding user", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<User> findAll() {
        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapUser(rs));
            }
            return users;
        } catch (SQLException e) {
            logger.error("Error finding all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding users", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public User update(User user) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getRole().name());
                ps.setLong(5, user.getId());
                ps.executeUpdate();
            }
            logger.debug("Updated user: {}", user);
            return user;
        } catch (SQLException e) {
            logger.error("Error updating user {}: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Error updating user", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Long id) {
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(DELETE_USER)) {
                ps.setLong(1, id);
                int rows = ps.executeUpdate();
                logger.debug("Deleted user with id: {}", id);
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error deleting user", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
