// package _blog.backend.service;

// // Import all the necessary classes
// import _blog.backend.Entitys.User.User;
// import _blog.backend.Repos.UserRepository;
// import org.springframework.transaction.annotation.Transactional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
// import java.util.ArrayList;
// import java.util.List;

// @Service
// // This class is a standard Spring Service.
// public class UserDetailsServiceImpl implements UserDetailsService {
//     // By "implements UserDetailsService", this class promises to have the
//     // one method Spring Security needs: loadUserByUsername.

//     @Autowired
//     private UserRepository userRepository;
//     // We need this to talk to the database.

//     @Override
//     @Transactional(readOnly = true)
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         // This is the *only* method in this class. The AuthenticationManager
//         // will call this when your LoginService tries to authenticate.
        
//         // 1. Find the user in your database
//         User user = userRepository.findByUsername(username);
        
//         // 2. Check if the user exists
//         if (user == null) {
//             // If not, throw this *specific* exception. Spring Security
//             // will catch it and know the authentication failed.
//             throw new UsernameNotFoundException("User not found with username: " + username);
//         }

//         // 3. Create the "authorities" (roles) list
//         List<GrantedAuthority> authorities = new ArrayList<>();
//         if (user.getRole() != null) {
//             // Spring Security requires roles to be in a List and to
//             // have the "ROLE_" prefix by default.
//             authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
//         }

//         // 4. Return Spring Security's "User" object
//         // This is not *your* User entity. It's Spring's own UserDetails object
//         // that holds the essential info.
//         return new org.springframework.security.core.userdetails.User(
//                 user.getUsername(),
//                 user.getPassword(), // <-- The *hashed* password from your database
//                 authorities
//         );
//         // The AuthenticationManager will now take this object and compare
//         // its password with the one the user typed in.
//     }
// }