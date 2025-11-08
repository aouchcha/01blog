package _blog.backend.helpers;

// Import all the necessary classes
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
// This registers it as a Spring bean.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // By "implements AuthenticationEntryPoint", this class promises
    // to have the "commence" method.

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // This method is called by Spring's filter chain *only when*
        // an *unauthenticated* user (no token, or bad token)
        // tries to access a *protected* resource (like /api/admin).
        
        // It sends a custom JSON error response.
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.getWriter().write("{ \"error\": \"Unauthorized\", \"message\": \"Authentication is required to access this resource.\" }");
    }
}
