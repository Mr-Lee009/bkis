package vn.edu.bkis.config;

import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            ensureSystemAdmin(userRepository, passwordEncoder);
            createIfMissing(userRepository, passwordEncoder,
                "admin", "System Admin", "admin@example.com", "admin123", UserRole.ADMIN);
            createIfMissing(userRepository, passwordEncoder,
                "teacher1", "Minh Nguyen", "teacher1@example.com", "teacher123", UserRole.TEACHER);
            createIfMissing(userRepository, passwordEncoder,
                "student1", "Nguyen Van A", "student1@example.com", "student123", UserRole.STUDENT);
            createIfMissing(userRepository, passwordEncoder,
                "student2", "Tran Thi B", "student2@example.com", "student234", UserRole.STUDENT);
            createIfMissing(userRepository, passwordEncoder,
                "ducla", "ducla", "ducla@example.com", "ducla12345", UserRole.ADMIN);
        };
    }

    // Bao dam tai khoan SA luon ton tai de dang nhap quan tri moi truong local va Docker.
    private void ensureSystemAdmin(UserRepository repo, PasswordEncoder encoder) {
        User systemAdmin = repo.findByUsername("SA").orElseGet(User::new);
        if (systemAdmin.getId() == null) {
            systemAdmin.setId(UUID.randomUUID().toString());
            systemAdmin.setBio("System administrator account for local and Docker startup.");
            systemAdmin.setProfilePictureUrl("/img/team-1.jpg");
        }
        systemAdmin.setUsername("SA");
        systemAdmin.setFullName("System Administrator");
        systemAdmin.setEmail("sa@bkis.local");
        systemAdmin.setPasswordHash(encoder.encode("admin112233"));
        systemAdmin.setRole(UserRole.ADMIN);
        systemAdmin.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        systemAdmin.setLocked(false);
        repo.save(systemAdmin);
    }

    // Tao user mau neu he thong chua co username tuong ung.
    private void createIfMissing(UserRepository repo, PasswordEncoder encoder,
                     String username, String fullName, String email, String plainPassword, UserRole role) {
        if (repo.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setId(UUID.randomUUID().toString());
            u.setUsername(username);
            u.setFullName(fullName);
            u.setBio("Seeded user for local and Docker development.");
            u.setProfilePictureUrl("/img/team-1.jpg");
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(plainPassword));
            u.setRole(role);
            u.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
            u.setLocked(false);
            repo.save(u);
        }
    }
}
