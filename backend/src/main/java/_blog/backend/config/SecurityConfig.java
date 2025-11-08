package _blog.backend.config;

import _blog.backend.helpers.JwtAuthenticationEntryPoint; 
import _blog.backend.helpers.JwtAuthenticationFilter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// This annotation tells Spring to find this file and treat it as a source of configuration.
@EnableWebSecurity
// This is the main switch. It turns on all of Spring Security's web-related features.
@EnableMethodSecurity
// This switch turns on the AOP that scans for @PreAuthorize annotations on your controllers.
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    // Asks Spring to inject the 401 Error Handler (which we will create).

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    // Asks Spring to inject the Token-Checking Filter (which we will create).

    @Autowired
    private CorsConfig corsConfig;

    @Bean
    // @Bean tells Spring: "Create an object of this type and keep it in your context
    // (a 'bean') so you can inject it anywhere else we need it."
    public PasswordEncoder passwordEncoder() {
        // This creates a single, application-wide instance of the BCrypt password
        // hasher. Your RegisterService and LoginService will now use this one.
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // This officially exposes Spring Security's core "AuthenticationManager" as
        // a bean. Your LoginService needs this to run the authentication process.
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // This is the main configuration block. It defines the "security filter chain"
        // which is like a list of bouncers (filters) at a club, each with one job.

        // 1. Disable CSRF
        http.csrf(csrf -> csrf.disable())
        // CSRF is a vulnerability for session-based (cookie) apps.
        // Since we are stateless and use a JWT in a header, we don't need it.

        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

        // 2. Set up the 401 Error Handler
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        // Tells Spring: "If an authentication error happens, don't use your default
        // HTML login page. Instead, call *this* class."

        // 3. Make the app STATELESS
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // This is the *most important* line for a JWT app. It tells Spring:
        // "Do NOT create or use any HttpSessions. Ever. Each request is on its own."

        // 4. Set up URL (HTTP Request) Authorization Rules
        .authorizeHttpRequests(auth -> auth
            // These lines are read from top to bottom, like firewall rules.
            .requestMatchers("/api/register").permitAll()
            .requestMatchers("/api/login").permitAll()
            // These URLs are public. No token is required. Let them pass.
            
            .requestMatchers("/uploads/**").permitAll()
            // Your static files are also public.
            
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // To access *any* URL starting with /api/admin/, the user *must*
            // have the 'ADMIN' role. (This check runs *after* our token filter).
            
            .anyRequest().authenticated()
            // For *any other request* not listed above, the user must
            // simply be authenticated (i.e., have a valid, non-expired token).
        );

        // 5. Add our custom JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // This tells Spring: "Find the built-in UsernamePasswordAuthenticationFilter
        // and insert *our* jwtAuthenticationFilter right before it in the chain."
        
        return http.build();
    }

   
}
