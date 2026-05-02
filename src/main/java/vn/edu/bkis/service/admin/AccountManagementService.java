package vn.edu.bkis.service.admin;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.account.AccountFilterDto;
import vn.edu.bkis.dto.admin.account.AccountFormDto;
import vn.edu.bkis.dto.admin.account.AccountManagementPageDto;
import vn.edu.bkis.dto.admin.account.AccountRowDto;
import vn.edu.bkis.dto.admin.account.AccountSummaryDto;
import vn.edu.bkis.dto.admin.account.AccountUpdateFormDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.UserRepository;

/**
 * Service for the admin account management screen.
 */
@Service
public class AccountManagementService {
    private static final String DEFAULT_PROFILE_PICTURE = "/img/team-1.jpg";
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final DateTimeFormatter ACCOUNT_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create the service with repository and password support.
     *
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder
     */
    public AccountManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Build all data required by the admin account management page.
     *
     * @param filter the requested table filter
     * @return the composite page DTO
     */
    @Transactional(readOnly = true)
    public AccountManagementPageDto getAccountManagementPage(AccountFilterDto filter) {
        String roleFilter = normalize(filter.getRole());
        List<User> users = userRepository.searchAccounts(normalize(filter.getKeyword()));
        if (roleFilter != null) {
            users = users.stream()
                .filter(user -> user.getRole() != null && user.getRole().name().equalsIgnoreCase(roleFilter))
                .toList();
        }
        int pageSize = resolvePageSize(filter.getSize());
        int currentPage = resolveCurrentPage(filter.getPage());
        long totalItems = users.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / pageSize);
        currentPage = Math.min(currentPage, totalPages - 1);

        int fromIndex = Math.min(currentPage * pageSize, users.size());
        int toIndex = Math.min(fromIndex + pageSize, users.size());
        List<User> pageUsers = users.subList(fromIndex, toIndex);

        List<AccountRowDto> accountRows = new ArrayList<>();
        for (int index = 0; index < pageUsers.size(); index++) {
            long rowNumber = ((long) currentPage * pageSize) + index + 1;
            accountRows.add(toAccountRowDto(pageUsers.get(index), rowNumber));
        }

        AccountSummaryDto summary = new AccountSummaryDto(
            userRepository.count(),
            userRepository.countByRole(UserRole.ADMIN),
            userRepository.countByRole(UserRole.TEACHER) + userRepository.countByRole(UserRole.INSTRUCTOR),
            userRepository.countByRole(UserRole.STUDENT),
            userRepository.countByLockedFalse(),
            userRepository.countByLockedTrue()
        );

        return new AccountManagementPageDto(
            summary,
            accountRows,
            Arrays.asList(UserRole.values()),
            currentPage,
            pageSize,
            totalPages,
            totalItems
        );
    }

    /**
     * Create a new account from the admin create-account form.
     *
     * @param form the submitted account form
     */
    @Transactional
    public void createAccount(AccountFormDto form) {
        String username = required(form.getUsername(), "Username is required");
        String fullName = required(form.getFullName(), "Full name is required");
        String email = required(form.getEmail(), "Email is required");
        String password = required(form.getPassword(), "Password is required");
        UserRole role = parseRole(required(form.getRole(), "Role is required"));

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must have at least 6 characters");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username.trim());
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBio(blankToDefault(form.getBio(), "Created from admin account management screen."));
        user.setProfilePictureUrl(blankToDefault(form.getProfilePictureUrl(), DEFAULT_PROFILE_PICTURE));
        user.setFailedLoginAttempts(0);
        user.setLocked(false);

        userRepository.save(user);
    }

    /**
     * Update an existing account from the admin modal form.
     *
     * @param form the submitted update form
     */
    @Transactional
    public void updateAccount(AccountUpdateFormDto form) {
        String accountId = required(form.getId(), "Account id is required");
        String username = required(form.getUsername(), "Username is required");
        String fullName = required(form.getFullName(), "Full name is required");
        String email = required(form.getEmail(), "Email is required");
        UserRole role = parseRole(required(form.getRole(), "Role is required"));

        User user = userRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        userRepository.findByUsername(username)
            .filter(existing -> !existing.getId().equals(user.getId()))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Username already exists");
            });

        userRepository.findByEmail(email)
            .filter(existing -> !existing.getId().equals(user.getId()))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Email already exists");
            });

        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setRole(role);
        user.setBio(blankToDefault(form.getBio(), "Updated from admin account management screen."));
        user.setProfilePictureUrl(blankToDefault(form.getProfilePictureUrl(), DEFAULT_PROFILE_PICTURE));
        user.setLocked(Boolean.TRUE.equals(form.getLocked()));

        userRepository.save(user);
    }

    /**
     * Convert a persistent user into a row DTO for table rendering.
     *
     * @param user the persistent user
     * @return the table row DTO
     */
    private AccountRowDto toAccountRowDto(User user, long rowNumber) {
        String createdAt = user.getCreatedAt() == null ? "N/A" : user.getCreatedAt().format(ACCOUNT_DATE_FORMAT);
        return new AccountRowDto(
            rowNumber,
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole() == null ? "UNKNOWN" : user.getRole().name(),
            blankToDefault(user.getBio(), ""),
            blankToDefault(user.getProfilePictureUrl(), DEFAULT_PROFILE_PICTURE),
            Boolean.TRUE.equals(user.getLocked()),
            user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts(),
            createdAt
        );
    }

    /**
     * Resolve the requested page index into a safe zero-based value.
     *
     * @param requestedPage the requested page index
     * @return a safe zero-based page index
     */
    private int resolveCurrentPage(Integer requestedPage) {
        return requestedPage == null || requestedPage < 0 ? 0 : requestedPage;
    }

    /**
     * Resolve the requested page size into a safe supported value.
     *
     * @param requestedSize the requested page size
     * @return a safe page size
     */
    private int resolvePageSize(Integer requestedSize) {
        if (requestedSize == null || requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(requestedSize, 20);
    }

    /**
     * Parse the submitted role value into a valid enum.
     *
     * @param rawRole the raw role string
     * @return the parsed enum value
     */
    private UserRole parseRole(String rawRole) {
        try {
            return UserRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + rawRole);
        }
    }

    /**
     * Ensure the submitted field is present and non-blank.
     *
     * @param value the raw field value
     * @param message the exception message
     * @return the normalized value
     */
    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * Normalize a raw input string before passing it to a query.
     *
     * @param value the raw input value
     * @return the normalized value or null when empty
     */
    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * Replace a blank form field with a default value.
     *
     * @param value the submitted field value
     * @param defaultValue the fallback value
     * @return the submitted value or the fallback value
     */
    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
