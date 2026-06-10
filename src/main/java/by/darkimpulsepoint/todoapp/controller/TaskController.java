package by.darkimpulsepoint.todoapp.controller;

import by.darkimpulsepoint.todoapp.model.Task;
import by.darkimpulsepoint.todoapp.model.User;
import by.darkimpulsepoint.todoapp.service.CommentService;
import by.darkimpulsepoint.todoapp.service.TaskService;
import by.darkimpulsepoint.todoapp.service.impl.CommentServiceImpl;
import by.darkimpulsepoint.todoapp.service.impl.TaskServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = {"/tasks", "/tasks/new", "/tasks/edit", "/tasks/delete",
        "/tasks/status", "/tasks/view", "/tasks/comment", "/tasks/comment/delete"})
public class TaskController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(TaskController.class);
    private final TaskService taskService = new TaskServiceImpl();
    private final CommentService commentService = new CommentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        String path = req.getServletPath();

        switch (path) {
            case "/tasks" -> listTasks(req, resp, user);
            case "/tasks/new" -> showNewForm(req, resp);
            case "/tasks/edit" -> showEditForm(req, resp, user);
            case "/tasks/view" -> showTask(req, resp, user);
            default -> resp.sendRedirect(req.getContextPath() + "/tasks");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        String path = req.getServletPath();

        switch (path) {
            case "/tasks/new" -> createTask(req, resp, user);
            case "/tasks/edit" -> updateTask(req, resp, user);
            case "/tasks/delete" -> deleteTask(req, resp, user);
            case "/tasks/status" -> updateStatus(req, resp, user);
            case "/tasks/comment" -> addComment(req, resp, user);
            case "/tasks/comment/delete" -> deleteComment(req, resp, user);
            default -> resp.sendRedirect(req.getContextPath() + "/tasks");
        }
    }

    private void showNewForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("statuses", Task.Status.values());
        req.setAttribute("priorities", Task.Priority.values());
        req.getRequestDispatcher("/WEB-INF/views/task-form.jsp").forward(req, resp);
    }

    private void listTasks(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        String statusFilter = req.getParameter("status");
        List<Task> tasks;

        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                Task.Status status = Task.Status.valueOf(statusFilter.toUpperCase());
                if (user.getRole() == User.Role.ADMIN) {
                    tasks = taskService.findAll().stream()
                            .filter(t -> t.getStatus() == status).toList();
                } else {
                    tasks = taskService.findByUserId(user.getId()).stream()
                            .filter(t -> t.getStatus() == status).toList();
                }
                req.setAttribute("activeFilter", statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                tasks = taskService.findTasksForUser(user);
            }
        } else {
            tasks = taskService.findTasksForUser(user);
        }

        Map<String, Long> stats = user.getRole() == User.Role.ADMIN
                ? taskService.getStatistics()
                : taskService.getStatisticsForUser(user.getId());

        req.setAttribute("tasks", tasks);
        req.setAttribute("stats", stats);
        req.setAttribute("statuses", Task.Status.values());
        req.setAttribute("priorities", Task.Priority.values());
        req.getRequestDispatcher("/WEB-INF/views/tasks.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) { resp.sendRedirect(req.getContextPath() + "/tasks"); return; }

        Optional<Task> taskOpt = taskService.findById(id);
        if (taskOpt.isEmpty() || (!taskOpt.get().getUserId().equals(user.getId()) && user.getRole() != User.Role.ADMIN)) {
            resp.sendRedirect(req.getContextPath() + "/tasks");
            return;
        }
        req.setAttribute("task", taskOpt.get());
        req.setAttribute("statuses", Task.Status.values());
        req.setAttribute("priorities", Task.Priority.values());
        req.getRequestDispatcher("/WEB-INF/views/task-form.jsp").forward(req, resp);
    }

    private void showTask(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) { resp.sendRedirect(req.getContextPath() + "/tasks"); return; }

        Optional<Task> taskOpt = taskService.findById(id);
        if (taskOpt.isEmpty()) { resp.sendRedirect(req.getContextPath() + "/tasks"); return; }

        Task task = taskOpt.get();
        if (user.getRole() != User.Role.ADMIN && !task.getUserId().equals(user.getId())) {
            resp.sendRedirect(req.getContextPath() + "/tasks");
            return;
        }
        req.setAttribute("task", task);
        req.setAttribute("comments", commentService.getCommentsForTask(id));
        req.setAttribute("statuses", Task.Status.values());
        req.getRequestDispatcher("/WEB-INF/views/task-view.jsp").forward(req, resp);
    }

    private void createTask(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        try {
            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String priorityStr = req.getParameter("priority");
            String dueDateStr = req.getParameter("dueDate");

            Task.Priority priority = priorityStr != null && !priorityStr.isBlank()
                    ? Task.Priority.valueOf(priorityStr) : Task.Priority.MEDIUM;
            LocalDateTime dueDate = parseDueDate(dueDateStr);

            taskService.createTask(title, description, priority, dueDate, user.getId());
            resp.sendRedirect(req.getContextPath() + "/tasks?msg=created");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("priorities", Task.Priority.values());
            req.getRequestDispatcher("/WEB-INF/views/task-form.jsp").forward(req, resp);
        }
    }

    private void updateTask(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        try {
            Long id = parseLong(req.getParameter("id"));
            if (id == null) throw new IllegalArgumentException("Invalid task ID");

            String title = req.getParameter("title");
            String description = req.getParameter("description");
            String statusStr = req.getParameter("status");
            String priorityStr = req.getParameter("priority");
            String dueDateStr = req.getParameter("dueDate");

            Task.Status status = statusStr != null ? Task.Status.valueOf(statusStr) : null;
            Task.Priority priority = priorityStr != null ? Task.Priority.valueOf(priorityStr) : null;
            LocalDateTime dueDate = parseDueDate(dueDateStr);

            taskService.updateTask(id, title, description, status, priority, dueDate, user);
            resp.sendRedirect(req.getContextPath() + "/tasks?msg=updated");
        } catch (IllegalArgumentException | SecurityException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("statuses", Task.Status.values());
            req.setAttribute("priorities", Task.Priority.values());
            req.getRequestDispatcher("/WEB-INF/views/task-form.jsp").forward(req, resp);
        }
    }

    private void deleteTask(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        try {
            Long id = parseLong(req.getParameter("id"));
            if (id != null) taskService.deleteTask(id, user);
            resp.sendRedirect(req.getContextPath() + "/tasks?msg=deleted");
        } catch (SecurityException e) {
            resp.sendRedirect(req.getContextPath() + "/tasks?error=noperm");
        }
    }

    private void updateStatus(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        try {
            Long id = parseLong(req.getParameter("id"));
            String statusStr = req.getParameter("status");
            if (id != null && statusStr != null) {
                taskService.updateStatus(id, Task.Status.valueOf(statusStr), user);
            }
        } catch (Exception e) {
            logger.warn("Status update failed: {}", e.getMessage());
        }
        String redirect = req.getParameter("redirect");
        if ("view".equals(redirect)) {
            resp.sendRedirect(req.getContextPath() + "/tasks/view?id=" + req.getParameter("id"));
        } else {
            resp.sendRedirect(req.getContextPath() + "/tasks");
        }
    }

    private void addComment(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        Long taskId = parseLong(req.getParameter("taskId"));
        String content = req.getParameter("content");
        if (taskId != null && content != null && !content.isBlank()) {
            commentService.addComment(taskId, user.getId(), content);
        }
        resp.sendRedirect(req.getContextPath() + "/tasks/view?id=" + taskId);
    }

    private void deleteComment(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        Long commentId = parseLong(req.getParameter("id"));
        Long taskId = parseLong(req.getParameter("taskId"));
        try {
            if (commentId != null) commentService.deleteComment(commentId, user);
        } catch (SecurityException e) {
            logger.warn("Comment delete denied for user {}", user.getUsername());
        }
        resp.sendRedirect(req.getContextPath() + "/tasks/view?id=" + taskId);
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private LocalDateTime parseDueDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s); } catch (DateTimeParseException e) { return null; }
    }
}
