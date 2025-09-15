package _blog.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.models.User;
import _blog.backend.service.UserService;


@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private UserService userservice;
    //api/register
    @PostMapping("/register")
    public User create(@RequestBody User user) {
        return userservice.register(user);
        
    }

    // @GetMapping
    // public List<User> getAll() {
    //     return userRepository.findAll();
    // }
}
