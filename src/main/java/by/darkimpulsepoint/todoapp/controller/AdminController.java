package by.darkimpulsepoint.todoapp.controller;

import by.darkimpulsepoint.todoapp.model.User;
import by.darkimpulsepoint.todoapp.service.TaskService;
import by.darkimpulsepoint.todoapp.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/delete"})
public class AdminController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AdminController.class);
    private final UserService userService = new UserService();
    private final TaskService taskService = new TaskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/admin/users".equals(path)) {
            List<User> users = userService.findAll();
            req.setAttribute("users", users);
            req.setAttribute("stats", taskService.getStatistics());
            req.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getServletPath();
        if ("/admin/users/delete".equals(path)) {
            User currentUser = (User) req.getSession().getAttribute("user");
            String idStr = req.getParameter("id");
            try {
                Long id = Long.parseLong(idStr);
                if (id.equals(currentUser.getId())) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users?error=cannot_delete_self");
                    return;
                }
                userService.deleteUser(id);
                logger.info("Admin {} deleted user id={}", currentUser.getUsername(), id);
            } catch (NumberFormatException e) {
                logger.warn("Invalid user id for deletion: {}", idStr);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/users?msg=deleted");
        }
    }
}
