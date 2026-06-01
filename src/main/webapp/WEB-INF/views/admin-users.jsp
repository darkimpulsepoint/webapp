<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="by.darkimpulsepoint.todoapp.model.User, java.util.List, java.util.Map, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <% List<User> users = (List<User>) request.getAttribute("users");
       Map<String,Long> stats = (Map<String,Long>) request.getAttribute("stats");
       User currentUser = (User) session.getAttribute("user");
       DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    %>
    <title>Admin – Users – TodoApp</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<nav class="navbar">
    <div class="nav-brand"><span class="logo-icon">✓</span> TodoApp <span class="admin-badge">ADMIN</span></div>
    <div class="nav-links">
        <a href="<%= request.getContextPath() %>/tasks" class="nav-link">Tasks</a>
        <span class="nav-user">👤 <%= currentUser.getUsername() %></span>
        <a href="<%= request.getContextPath() %>/logout" class="nav-link nav-logout">Sign Out</a>
    </div>
</nav>
<div class="container">
    <% if ("deleted".equals(request.getParameter("msg"))) { %><div class="alert alert-info">User deleted.</div><% } %>
    <% if ("cannot_delete_self".equals(request.getParameter("error"))) { %><div class="alert alert-error">You cannot delete your own account.</div><% } %>
    <div class="stats-grid">
        <div class="stat-card stat-total"><div class="stat-number"><%= users.size() %></div><div class="stat-label">Users</div></div>
        <div class="stat-card stat-total"><div class="stat-number"><%= stats.getOrDefault("total",0L) %></div><div class="stat-label">Total Tasks</div></div>
        <div class="stat-card stat-done"><div class="stat-number"><%= stats.getOrDefault("done",0L) %></div><div class="stat-label">Done</div></div>
        <div class="stat-card stat-progress"><div class="stat-number"><%= stats.getOrDefault("in_progress",0L) %></div><div class="stat-label">In Progress</div></div>
    </div>
    <div class="page-header"><h1 class="page-title">User Management</h1></div>
    <div class="table-card">
        <table class="data-table">
            <thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Registered</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (User u : users) { %>
            <tr>
                <td><%= u.getId() %></td>
                <td><strong><%= u.getUsername() %></strong></td>
                <td><%= u.getEmail() %></td>
                <td><span class="badge badge-<%= u.getRole() == User.Role.ADMIN ? "high" : "low" %>"><%= u.getRole() %></span></td>
                <td><%= u.getCreatedAt().format(fmt) %></td>
                <td>
                    <% if (!currentUser.getId().equals(u.getId())) { %>
                    <form method="post" action="<%= request.getContextPath() %>/admin/users/delete" style="display:inline"
                          onsubmit="return confirm('Delete user <%= u.getUsername() %>?')">
                        <input type="hidden" name="id" value="<%= u.getId() %>">
                        <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                    </form>
                    <% } else { %>
                    <span class="badge badge-medium">You</span>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
