<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><title>Error – TodoApp</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body>
<div class="container" style="text-align:center;padding-top:5rem">
    <div class="empty-icon">⚠️</div>
    <h1>Oops! Something went wrong.</h1>
    <p style="color:var(--text-muted);margin:1rem 0">An unexpected error occurred. Please try again.</p>
    <a href="${pageContext.request.contextPath}/tasks" class="btn btn-primary">Go Home</a>
</div>
</body>
</html>
