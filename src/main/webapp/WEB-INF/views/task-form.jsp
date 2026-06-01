<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="by.darkimpulsepoint.todoapp.model.Task, by.darkimpulsepoint.todoapp.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%  Task task = (Task) request.getAttribute("task");
        Task.Status[] statuses = (Task.Status[]) request.getAttribute("statuses");
        Task.Priority[] priorities = (Task.Priority[]) request.getAttribute("priorities");
        boolean isNew = (task == null);
    %>
    <title><%= isNew ? "New Task" : "Edit Task" %> – TodoApp</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<nav class="navbar">
    <div class="nav-brand">
        <a href="<%= request.getContextPath() %>/tasks" class="nav-back">← Back</a>
        <span class="logo-icon">✓</span> TodoApp
    </div>
    <div class="nav-links">
        <span class="nav-user">👤 <%= ((User)session.getAttribute("user")).getUsername() %></span>
        <a href="<%= request.getContextPath() %>/logout" class="nav-link nav-logout">Sign Out</a>
    </div>
</nav>
<div class="container container-narrow">
    <div class="form-card">
        <h1 class="form-title"><%= isNew ? "Create New Task" : "Edit Task" %></h1>
        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/tasks/<%= isNew ? "new" : "edit" %>">
            <% if (!isNew) { %><input type="hidden" name="id" value="<%= task.getId() %>"><% } %>
            <div class="form-group">
                <label for="title">Title <span class="required">*</span></label>
                <input type="text" id="title" name="title"
                       value="<%= !isNew ? task.getTitle() : "" %>"
                       placeholder="What needs to be done?" required maxlength="200">
            </div>
            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description" name="description" rows="4"
                          placeholder="Add more details..."><%= !isNew && task.getDescription() != null ? task.getDescription() : "" %></textarea>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label for="priority">Priority</label>
                    <select id="priority" name="priority">
                        <% for (Task.Priority p : priorities) { %>
                        <option value="<%= p %>"
                            <%= (!isNew && task.getPriority() == p) || (isNew && p == Task.Priority.MEDIUM) ? "selected" : "" %>><%= p %></option>
                        <% } %>
                    </select>
                </div>
                <% if (!isNew) { %>
                <div class="form-group">
                    <label for="status">Status</label>
                    <select id="status" name="status">
                        <% for (Task.Status s : statuses) { %>
                        <option value="<%= s %>" <%= task.getStatus() == s ? "selected" : "" %>><%= s.name().replace("_"," ") %></option>
                        <% } %>
                    </select>
                </div>
                <% } %>
            </div>
            <div class="form-group">
                <label for="dueDate">Due Date</label>
                <input type="datetime-local" id="dueDate" name="dueDate"
                       value="<%= !isNew && task.getDueDate() != null ? task.getDueDate().toString().substring(0,16) : "" %>">
            </div>
            <div class="form-actions">
                <a href="<%= request.getContextPath() %>/tasks" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary"><%= isNew ? "Create Task" : "Save Changes" %></button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
