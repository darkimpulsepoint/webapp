package by.darkimpulsepoint.todoapp.util;

import by.darkimpulsepoint.todoapp.dao.impl.UserDAOImpl;
import by.darkimpulsepoint.todoapp.model.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application starting up...");
        try {
            DatabaseInitializer.initialize();
            createAdminIfNotExists();
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Application startup failed: {}", e.getMessage(), e);
            throw new RuntimeException("Startup failed", e);
        }
    }

    private void createAdminIfNotExists() {
        UserDAOImpl userDAO = new UserDAOImpl();
        if (!userDAO.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@todoapp.com");
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setRole(User.Role.ADMIN);
            userDAO.create(admin);
            logger.info("Default admin user created (admin / admin123)");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application shutting down...");
        ConnectionPool.getInstance().destroyPool();
    }
}
