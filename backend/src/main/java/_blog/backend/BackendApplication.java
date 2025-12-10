package _blog.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import _blog.backend.Entitys.User.Role;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.PasswordUtils;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	private UserRepository userRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void initData() {
		if (userRepository.findByEmail("ouchchatea@gmail.com") == null) {
			User admin = new User();
			admin.setUsername("aouchcha");
			admin.setPassword(PasswordUtils.hashPassword("Achraf1303@@"));
			admin.setRole(Role.Admin);
			admin.setEmail("ouchchatea@gmail.com");
			userRepository.save(admin);
		}

	}

}
