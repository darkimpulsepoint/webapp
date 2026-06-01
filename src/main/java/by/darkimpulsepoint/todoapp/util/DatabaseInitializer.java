package by.darkimpulsepoint.todoapp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    private static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL DEFAULT 'USER',
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_TASKS_TABLE = """
            CREATE TABLE IF NOT EXISTS tasks (
                id BIGSERIAL PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                description TEXT,
                status VARCHAR(20) NOT NULL DEFAULT 'TODO',
                priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                due_date TIMESTAMP
            )
            """;

    private static final String CREATE_COMMENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS comments (
                id BIGSERIAL PRIMARY KEY,
                task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                content TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String CREATE_TASK_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id)
            """;

    private static final String CREATE_COMMENT_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_comments_task_id ON comments(task_id)
            """;

    public static void initialize() {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection conn = null;
        try {
            conn = pool.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_USERS_TABLE);
                stmt.execute(CREATE_TASKS_TABLE);
                stmt.execute(CREATE_COMMENTS_TABLE);
                stmt.execute(CREATE_TASK_INDEX);
                stmt.execute(CREATE_COMMENT_INDEX);
            }
            logger.info("Database schema initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }
}
