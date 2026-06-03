package by.darkimpulsepoint.todoapp.service;

import by.darkimpulsepoint.todoapp.dao.UserDAO;
import by.darkimpulsepoint.todoapp.dao.impl.UserDAOImpl;
import by.darkimpulsepoint.todoapp.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO = new UserDAOImpl();

    public User register(String username, String email, String password) {
        logger.info("Registering user: {}", username);

        if (username == null || username.isBlank() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userDAO.findByUsername(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(User.Role.USER);
        User created = userDAO.create(user);
        logger.info("User registered successfully: {}", username);
        return created;
    }

    public Optional<User> authenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                logger.info("Authentication successful for: {}", username);
                return Optional.of(user);
            }
        }
        logger.warn("Authentication failed for: {}", username);
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userDAO.findById(id);
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    public boolean deleteUser(Long id) {
        logger.info("Deleting user id={}", id);
        return userDAO.delete(id);
    }
}
