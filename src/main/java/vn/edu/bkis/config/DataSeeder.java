package vn.edu.bkis.config;

import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder,
                    "admin", "admin@example.com", "admin123", UserRole.ADMIN);
            createIfMissing(userRepository, passwordEncoder,
                    "teacher1", "teacher1@example.com", "teacher123", UserRole.TEACHER);
            createIfMissing(userRepository, passwordEncoder,
                    "student1", "student1@example.com", "student123", UserRole.STUDENT);
            createIfMissing(userRepository, passwordEncoder,
                    "student2", "student2@example.com", "student234", UserRole.STUDENT);
        };
    }

    private void createIfMissing(UserRepository repo, PasswordEncoder encoder,
                                 String username, String email, String plainPassword, UserRole role) {
        if (repo.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setId(UUID.randomUUID().toString());
            u.setUsername(username);
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(plainPassword));
            u.setRole(role);
            u.setFailedLoginAttempts(0);
            u.setLocked(false);
            repo.save(u);
        }
    }
}
