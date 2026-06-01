<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="by.darkimpulsepoint.todoapp.model.Task, by.darkimpulsepoint.todoapp.model.User, java.util.List, java.util.Map, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Tasks – TodoApp</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    User currentUser = (User) session.getAttribute("user");
    List<Task> tasks = (List<Task>) request.getAttribute("tasks");
    Map<String, Long> stats = (Map<String, Long>) request.getAttribute("stats");
    Task.Status[] statuses = (Task.Status[]) request.getAttribute("statuses");
    String activeFilter = (String) request.getAttribute("activeFilter");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
%>
<nav class="navbar">
    <div class="nav-brand">
        <span class="logo-icon">✓</span> TodoApp
    </div>
    <div class="nav-links">
        <span class="nav-user">👤 <%= currentUser.getUsername() %></span>
        <% if (currentUser.getRole() == User.Role.ADMIN) { %>
        <a href="<%= request.getContextPath() %>/admin/users" class="nav-link">Admin</a>
        <% } %>
        <a href="<%= request.getContextPath() %>/logout" class="nav-link nav-logout">Sign Out</a>
    </div>
</nav>

<div class="container">
    <% if ("welcome".equals(request.getParameter("msg"))) { %><div class="alert alert-success">Welcome! Your account has been created.</div><% } %>
    <% if ("created".equals(request.getParameter("msg"))) { %><div class="alert alert-success">Task created successfully.</div><% } %>
    <% if ("updated".equals(request.getParameter("msg"))) { %><div class="alert alert-success">Task updated successfully.</div><% } %>
    <% if ("deleted".equals(request.getParameter("msg"))) { %><div class="alert alert-info">Task deleted.</div><% } %>

    <div class="stats-grid">
        <div class="stat-card stat-total"><div class="stat-number"><%= stats.getOrDefault("total", 0L) %></div><div class="stat-label">Total</div></div>
        <div class="stat-card stat-todo"><div class="stat-number"><%= stats.getOrDefault("todo", 0L) %></div><div class="stat-label">To Do</div></div>
        <div class="stat-card stat-progress"><div class="stat-number"><%= stats.getOrDefault("in_progress", 0L) %></div><div class="stat-label">In Progress</div></div>
        <div class="stat-card stat-done"><div class="stat-number"><%= stats.getOrDefault("done", 0L) %></div><div class="stat-label">Done</div></div>
        <div class="stat-card stat-cancelled"><div class="stat-number"><%= stats.getOrDefault("cancelled", 0L) %></div><div class="stat-label">Cancelled</div></div>
    </div>

    <div class="page-header">
        <h1 class="page-title"><%= currentUser.getRole() == User.Role.ADMIN ? "All Tasks" : "My Tasks" %></h1>
        <a href="<%= request.getContextPath() %>/tasks/new" class="btn btn-primary">+ New Task</a>
    </div>

    <div class="filter-bar">
        <a href="<%= request.getContextPath() %>/tasks" class="filter-btn <%= activeFilter == null ? "active" : "" %>">All</a>
        <% for (Task.Status s : statuses) { %>
        <a href="<%= request.getContextPath() %>/tasks?status=<%= s.name() %>"
           class="filter-btn <%= s.name().equals(activeFilter) ? "active" : "" %>">
            <%= s.name().replace("_", " ") %>
        </a>
        <% } %>
    </div>

    <% if (tasks == null || tasks.isEmpty()) { %>
    <div class="empty-state">
        <div class="empty-icon">📋</div>
        <h2>No tasks found</h2>
        <p>Get started by creating your first task.</p>
        <a href="<%= request.getContextPath() %>/tasks/new" class="btn btn-primary">Create Task</a>
    </div>
    <% } else { %>
    <div class="task-list">
        <% for (Task task : tasks) {
            String priorityCls = task.getPriority().name().toLowerCase();
            String statusCls = task.getStatus().name().toLowerCase().replace("_","-");
            boolean isDone = task.getStatus() == Task.Status.DONE;
        %>
        <div class="task-card priority-<%= priorityCls %>">
            <div class="task-card-left">
                <form method="post" action="<%= request.getContextPath() %>/tasks/status">
                    <input type="hidden" name="id" value="<%= task.getId() %>">
                    <input type="hidden" name="status" value="<%= isDone ? "TODO" : "DONE" %>">
                    <button type="submit" class="status-check <%= isDone ? "checked" : "" %>"><%= isDone ? "✓" : "" %></button>
                </form>
            </div>
            <div class="task-card-body">
                <div class="task-card-title">
                    <a href="<%= request.getContextPath() %>/tasks/view?id=<%= task.getId() %>"
                       class="<%= isDone ? "strikethrough" : "" %>"><%= task.getTitle() %></a>
                    <span class="badge badge-<%= priorityCls %>"><%= task.getPriority() %></span>
                    <span class="badge badge-status-<%= statusCls %>"><%= task.getStatus().name().replace("_"," ") %></span>
                </div>
                <div class="task-card-meta">
                    <% if (currentUser.getRole() == User.Role.ADMIN) { %><span>👤 <%= task.getUsername() %></span><% } %>
                    <% if (task.getDueDate() != null) { %><span>📅 <%= task.getDueDate().format(fmt) %></span><% } %>
                    <span>🕐 <%= task.getCreatedAt().format(fmt) %></span>
                </div>
            </div>
            <div class="task-card-actions">
                <a href="<%= request.getContextPath() %>/tasks/edit?id=<%= task.getId() %>" class="btn btn-sm btn-outline">Edit</a>
                <form method="post" action="<%= request.getContextPath() %>/tasks/delete" style="display:inline"
                      onsubmit="return confirm('Delete this task?')">
                    <input type="hidden" name="id" value="<%= task.getId() %>">
                    <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                </form>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>
</div>
<script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
