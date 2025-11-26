package _blog.backend.helpers;

// Import all the necessary classes
// import _blog.backend.helpers.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// import _blog.backend.Entitys.User.User;
// import _blog.backend.Repos.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Component
// This registers the filter as a Spring component (a bean).
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // We extend "OncePerRequestFilter" to ensure this filter
    // runs exactly one time for each incoming request.

    @Autowired
    private JwtUtil jwtUtil;

    // @Autowired
    // private UserRepository userRepository;
    // We need our JWT helper to read and validate tokens.

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // This is the main method that runs on *every request*.

        // 1. Try to get the token from the "Authorization" header.
        String token = resolveToken(request);
    
        // System.out.println("|||||||||||||||||||||||||||||||||||||||||||||" + token
        //         + "||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        // User u = null;
        // if (token != null) {
        //     System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        //     u = userRepository.findByUsername(jwtUtil.getUsername(token));
        // }

        // 2. Check if the token is valid.
        if (token != null && jwtUtil.validateToken(token) /*&& u != null*/) {
            // System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
            // 3. If valid, parse the user's details from the token.
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);
            // System.out.println("|||||||||||||||||||||||||||||||||||||||||||||" + username
            //         + "||||||||||||||||||||||||||||||||||||||||||||||||||||||");
            // System.out.println("|||||||||||||||||||||||||||||||||||||||||||||" + role
            //         + "||||||||||||||||||||||||||||||||||||||||||||||||||||||");

            // 4. Create the required List<GrantedAuthority> for Spring.
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }

            // 5. Create the official "Authentication" object.
            // This is Spring's "Access Badge" for the user.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, // The user's identity (principal)
                    null, // No password (credentials) needed here
                    authorities // The user's roles
            );

            // 6. Set this "Access Badge" in the SecurityContextHolder.
            // THIS IS THE KEY LINE: You are manually telling Spring Security:
            // "For this one single request, this user is now authenticated."
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. Pass the request to the next filter in the chain.
        // If we don't do this, the request stops here and never
        // reaches your controller.
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // A helper method to read the "Authorization: Bearer <token>" header.
        System.out.println(request.getHeader("Authorization"));
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Return just the token string
        }
        return null;
    }
}
