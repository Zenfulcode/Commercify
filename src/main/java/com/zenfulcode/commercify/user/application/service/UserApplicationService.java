package com.zenfulcode.commercify.user.application.service;

import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.application.command.CreateUserCommand;
import com.zenfulcode.commercify.user.application.command.UpdateUserCommand;
import com.zenfulcode.commercify.user.application.command.UpdateUserStatusCommand;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
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
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user
     */
    @Transactional
    public UserId createUser(CreateUserCommand command) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(command.password());

        UserSpecification userSpecification = new UserSpecification(command.firstName(), command.lastName(), command.email(), hashedPassword, command.phoneNumber(), UserStatus.PENDING, command.roles());

        // Create the user through domain service
        User user = userDomainService.createUser(userSpecification);

        // Publish domain events
        eventPublisher.publish(user.getDomainEvents());
        return user.getId();
    }

    @Transactional
    public void registerUser(String firstName, String lastName, String email, String password, String phone) {
        CreateUserCommand createUserCommand = new CreateUserCommand(email, firstName, lastName, password, Set.of(UserRole.USER), phone);

        createUser(createUserCommand);
    }

    /**
     * Updates an existing user
     */
    @Transactional
    public void updateUser(UpdateUserCommand command) {
        // Retrieve user
        User user = getUser(command.userId());

        // Update through domain service
        userDomainService.updateUserInfo(user, command.userSpec());

        // Publish events
        eventPublisher.publish(user.getDomainEvents());
    }

    /**
     * Updates user status (activate/deactivate)
     */
    @Transactional
    public void updateUserStatus(UpdateUserStatusCommand command) {
        User user = userDomainService.getUserById(command.userId());

        userDomainService.updateUserStatus(user, command.newStatus());
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
        return userDomainService.getUserById(userId);
    }

    /**
     * Gets a user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userDomainService.getUserByEmail(email);
    }

    /**
     * Lists all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userDomainService.getAllUsers(pageable);
    }

    /**
     * Lists active users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getActiveUsers(Pageable pageable) {
        return userDomainService.getUsersByStatus(UserStatus.ACTIVE, pageable);
    }

    /**
     * Checks if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userDomainService.emailExists(email);
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

        eventPublisher.publish(user.getDomainEvents());
    }
}
