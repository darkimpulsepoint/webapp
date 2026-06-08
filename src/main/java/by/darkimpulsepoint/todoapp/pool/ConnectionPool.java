package by.darkimpulsepoint.todoapp.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger(ConnectionPool.class);
    private static final String PROPERTIES_FILE = "db.properties";
    private static volatile ConnectionPool instance;
    private static final Lock lock = new ReentrantLock();

    private final BlockingQueue<Connection> freeConnections;
    private final BlockingQueue<Connection> givenAwayConnections;

    private ConnectionPool() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Database properties file not found: " + PROPERTIES_FILE);
            }
            properties.load(inputStream);

            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.username");
            String pass = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");
            int poolSize = Integer.parseInt(properties.getProperty("db.pool.size", "10"));

            Class.forName(driver);

            freeConnections = new ArrayBlockingQueue<>(poolSize);
            givenAwayConnections = new ArrayBlockingQueue<>(poolSize);

            for (int i = 0; i < poolSize; i++) {
                Connection connection = DriverManager.getConnection(url, user, pass);
                freeConnections.add(connection);
            }

            logger.info("Connection pool initialized successfully with {} connections", poolSize);

        } catch (IOException | SQLException | ClassNotFoundException e) {
            logger.fatal("Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = freeConnections.take();
            givenAwayConnections.put(connection);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while waiting for connection", e);
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (givenAwayConnections.remove(connection)) {
                    freeConnections.put(connection);
                } else {
                    logger.warn("Trying to release a connection that is not in the given away queue!");
                }
            } catch (InterruptedException e) {
                logger.error("Thread was interrupted while releasing connection", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void destroyPool() {
        int currentSize = freeConnections.size();
        for (int i = 0; i < currentSize; i++) {
            try {
                Connection connection = freeConnections.take();
                connection.close();
            } catch (InterruptedException | SQLException e) {
                logger.error("Error while closing connection during pool destruction", e);
            }
        }

        for (Connection connection : givenAwayConnections) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing given away connection", e);
            }
        }

        deregisterDrivers();
        logger.info("Connection pool destroyed");
    }

    private void deregisterDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                logger.error("Error deregistering driver", e);
            }
        }
    }
}