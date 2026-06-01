package by.darkimpulsepoint.todoapp.controller;

import by.darkimpulsepoint.todoapp.model.User;
import by.darkimpulsepoint.todoapp.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/login", "/logout", "/register"})
public class AuthController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/login" -> {
                HttpSession session = req.getSession(false);
                if (session != null && session.getAttribute("user") != null) {
                    resp.sendRedirect(req.getContextPath() + "/tasks");
                    return;
                }
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            }
            case "/register" -> req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            case "/logout" -> {
                HttpSession s = req.getSession(false);
                if (s != null) {
                    String username = ((User) s.getAttribute("user")).getUsername();
                    s.invalidate();
                    logger.info("User {} logged out", username);
                }
                resp.sendRedirect(req.getContextPath() + "/login?msg=logged_out");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/login" -> handleLogin(req, resp);
            case "/register" -> handleRegister(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Please enter username and password");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        Optional<User> userOpt = userService.authenticate(username.trim(), password);
        if (userOpt.isPresent()) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", userOpt.get());
            session.setMaxInactiveInterval(30 * 60);
            logger.info("User {} logged in", username);
            resp.sendRedirect(req.getContextPath() + "/tasks");
        } else {
            req.setAttribute("error", "Invalid username or password");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            if (password == null || !password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            User user = userService.register(username, email, password);
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            logger.info("New user registered: {}", username);
            resp.sendRedirect(req.getContextPath() + "/tasks?msg=welcome");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("username", username);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }
}
