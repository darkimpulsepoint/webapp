package by.darkimpulsepoint.todoapp.filter;

import by.darkimpulsepoint.todoapp.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String uri = req.getRequestURI();

        logger.debug("AuthFilter: path={}, uri={}", path, uri);

        if (path.equals("/login") || path.equals("/register") ||
                path.startsWith("/css/") || path.startsWith("/js/") ||
                uri.contains("/css/") || uri.contains("/js/") ||
                path.equals("/") || path.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            logger.debug("Unauthenticated access to {}, redirecting to login", path);
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (path.startsWith("/admin") && user.getRole() != User.Role.ADMIN) {
            logger.warn("Unauthorized admin access attempt by user {}", user.getUsername());
            res.sendRedirect(req.getContextPath() + "/tasks");
            return;
        }

        chain.doFilter(request, response);
    }
}
