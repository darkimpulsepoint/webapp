<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="by.darkimpulsepoint.todoapp.model.Task, by.darkimpulsepoint.todoapp.model.User, by.darkimpulsepoint.todoapp.model.Comment, java.util.List, java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <% Task task = (Task) request.getAttribute("task");
       List<Comment> comments = (List<Comment>) request.getAttribute("comments");
       Task.Status[] statuses = (Task.Status[]) request.getAttribute("statuses");
       User currentUser = (User) session.getAttribute("user");
       DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
       DateTimeFormatter fmtFull = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    %>
    <title><%= task.getTitle() %> – TodoApp</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<nav class="navbar">
    <div class="nav-brand">
        <a href="<%= request.getContextPath() %>/tasks" class="nav-back">← Back</a>
        <span class="logo-icon">✓</span> TodoApp
    </div>
    <div class="nav-links">
        <span class="nav-user">👤 <%= currentUser.getUsername() %></span>
        <a href="<%= request.getContextPath() %>/logout" class="nav-link nav-logout">Sign Out</a>
    </div>
</nav>
<div class="container container-narrow">
    <div class="task-detail-card">
        <div class="task-detail-header">
            <div>
                <h1 class="task-detail-title <%= task.getStatus() == Task.Status.DONE ? "strikethrough" : "" %>"><%= task.getTitle() %></h1>
                <div class="task-detail-meta">
                    <span class="badge badge-<%= task.getPriority().name().toLowerCase() %>"><%= task.getPriority() %></span>
                    <span class="badge badge-status-<%= task.getStatus().name().toLowerCase().replace("_","-") %>"><%= task.getStatus().name().replace("_"," ") %></span>
                    <% if (currentUser.getRole() == User.Role.ADMIN) { %><span class="meta-item">👤 <%= task.getUsername() %></span><% } %>
                    <% if (task.getDueDate() != null) { %><span class="meta-item">📅 Due: <%= task.getDueDate().format(fmtFull) %></span><% } %>
                    <span class="meta-item">Created: <%= task.getCreatedAt().format(fmt) %></span>
                </div>
            </div>
            <div class="task-detail-actions">
                <a href="<%= request.getContextPath() %>/tasks/edit?id=<%= task.getId() %>" class="btn btn-outline">Edit</a>
                <form method="post" action="<%= request.getContextPath() %>/tasks/delete" style="display:inline"
                      onsubmit="return confirm('Delete this task?')">
                    <input type="hidden" name="id" value="<%= task.getId() %>">
                    <button type="submit" class="btn btn-danger">Delete</button>
                </form>
            </div>
        </div>
        <% if (task.getDescription() != null && !task.getDescription().isBlank()) { %>
        <div class="task-description">
            <h3>Description</h3>
            <p><%= task.getDescription() %></p>
        </div>
        <% } %>
        <div class="status-section">
            <h3>Update Status</h3>
            <div class="status-buttons">
                <% for (Task.Status s : statuses) { %>
                <form method="post" action="<%= request.getContextPath() %>/tasks/status" style="display:inline">
                    <input type="hidden" name="id" value="<%= task.getId() %>">
                    <input type="hidden" name="status" value="<%= s.name() %>">
                    <input type="hidden" name="redirect" value="view">
                    <button type="submit" class="btn btn-sm <%= task.getStatus() == s ? "btn-primary" : "btn-outline" %>">
                        <%= s.name().replace("_"," ") %>
                    </button>
                </form>
                <% } %>
            </div>
        </div>
        <div class="comments-section">
            <h3>Comments (<%= comments != null ? comments.size() : 0 %>)</h3>
            <% if (comments == null || comments.isEmpty()) { %>
            <p class="no-comments">No comments yet. Be the first to comment!</p>
            <% } else { %>
            <div class="comment-list">
                <% for (Comment c : comments) { %>
                <div class="comment-item">
                    <div class="comment-header">
                        <span class="comment-author">👤 <%= c.getUsername() %></span>
                        <span class="comment-date"><%= c.getCreatedAt().format(fmtFull) %></span>
                        <% if (currentUser.getId().equals(c.getUserId()) || currentUser.getRole() == User.Role.ADMIN) { %>
                        <form method="post" action="<%= request.getContextPath() %>/tasks/comment/delete" style="display:inline"
                              onsubmit="return confirm('Delete comment?')">
                            <input type="hidden" name="id" value="<%= c.getId() %>">
                            <input type="hidden" name="taskId" value="<%= task.getId() %>">
                            <button type="submit" class="btn-link-danger">Delete</button>
                        </form>
                        <% } %>
                    </div>
                    <div class="comment-body"><%= c.getContent() %></div>
                </div>
                <% } %>
            </div>
            <% } %>
            <form method="post" action="<%= request.getContextPath() %>/tasks/comment" class="comment-form">
                <input type="hidden" name="taskId" value="<%= task.getId() %>">
                <div class="form-group">
                    <textarea name="content" rows="3" placeholder="Add a comment..." required></textarea>
                </div>
                <button type="submit" class="btn btn-primary btn-sm">Post Comment</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
