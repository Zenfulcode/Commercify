package com.zenfulcode.commercify.user.application.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.application.command.CreateUserCommand;
import com.zenfulcode.commercify.user.application.command.UpdateUserCommand;
import com.zenfulcode.commercify.user.application.command.UpdateUserStatusCommand;
import com.zenfulcode.commercify.user.domain.exception.UserNotFoundException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import com.zenfulcode.commercify.user.domain.service.UserDomainService;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import com.zenfulcode.commercify.user.domain.valueobject.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user
     */
    @Transactional
    public UserId createUser(CreateUserCommand command) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(command.password());

        UserSpecification userSpecification = new UserSpecification(
                command.firstName(),
                command.lastName(),
                command.email(),
                hashedPassword,
                command.phoneNumber(),
                UserStatus.PENDING,
                command.roles()
        );

        // Create the user through domain service
        User user = userDomainService.createUser(userSpecification);

        // Save the user
        User savedUser = userRepository.save(user);

        // Publish domain events
        eventPublisher.publish(user.getDomainEvents());

        return savedUser.getId();
    }

    @Transactional
    public void registerUser(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone
    ) {
        // Create user specification
        UserSpecification spec = UserSpecification.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .status(UserStatus.ACTIVE)
                .roles(Set.of(UserRole.USER))
                .build();

        // Create user through domain service
        User user = userDomainService.createUser(spec);

        // Save user
        userRepository.save(user);

        // Publish domain event
        eventPublisher.publish(user.getDomainEvents());
    }

    /**
     * Updates an existing user
     */
    @Transactional
    public void updateUser(UpdateUserCommand command) {
        // Retrieve user
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Update through domain service
        userDomainService.updateUserInfo(user, command.userSpec());

        // Save changes
        userRepository.save(user);

        // Publish events
        eventPublisher.publish(user.getDomainEvents());
    }

    /**
     * Updates user status (activate/deactivate)
     */
    @Transactional
    public void updateUserStatus(UpdateUserStatusCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        userDomainService.updateUserStatus(user, command.newStatus());
        userRepository.save(user);
        eventPublisher.publish(user.getDomainEvents());
    }

    /**
     * Activates a user
     */
    @Transactional
    public void activateUser(UserId userId) {
        updateUserStatus(new UpdateUserStatusCommand(userId, UserStatus.ACTIVE));
    }

    /**
     * Deactivates a user
     */
    @Transactional
    public void deactivateUser(UserId userId) {
        updateUserStatus(new UpdateUserStatusCommand(userId, UserStatus.DEACTIVATED));
    }

    /**
     * Gets a user by ID
     */
    @Transactional(readOnly = true)
    public User getUser(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Gets a user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Lists all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Lists active users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.ACTIVE, pageable);
    }

    /**
     * Checks if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Changes user password
     */
    @Transactional
    public void changePassword(UserId userId, String currentPassword, String newPassword) {
        User user = getUser(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Hash new password and update
        String hashedPassword = passwordEncoder.encode(newPassword);
        userDomainService.changePassword(user, hashedPassword);

        userRepository.save(user);
        eventPublisher.publish(user.getDomainEvents());
    }

    /**
     * Resets user password (admin function)
     */
    @Transactional
    public void resetPassword(UserId userId, String newPassword) {
        User user = getUser(userId);

        String hashedPassword = passwordEncoder.encode(newPassword);
        userDomainService.changePassword(user, hashedPassword);

        userRepository.save(user);
        eventPublisher.publish(user.getDomainEvents());
    }
}
