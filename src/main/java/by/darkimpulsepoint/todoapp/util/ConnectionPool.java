package by.darkimpulsepoint.todoapp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;

    private ConnectionPool() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties not found in classpath");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        url = props.getProperty("db.url");
        username = props.getProperty("db.username");
        password = props.getProperty("db.password");
        poolSize = Integer.parseInt(props.getProperty("db.pool.size", "10"));

        try {
            Class.forName(props.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }

        pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            try {
                pool.add(createConnection());
            } catch (SQLException e) {
                logger.error("Failed to create connection: {}", e.getMessage());
                throw new RuntimeException("Cannot initialize connection pool", e);
            }
        }
        logger.info("Connection pool initialized with {} connections", poolSize);
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.take();
            if (conn.isClosed()) {
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection", e);
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    pool.offer(connection);
                } else {
                    pool.offer(createConnection());
                }
            } catch (SQLException e) {
                logger.error("Error releasing connection: {}", e.getMessage());
            }
        }
    }

    public void shutdown() {
        for (Connection conn : pool) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
        logger.info("Connection pool shut down");
    }
}
