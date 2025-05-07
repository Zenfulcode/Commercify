package com.zenfulcode.commercify.user.infrastructure.config;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import com.zenfulcode.commercify.user.application.command.CreateUserCommand;
import com.zenfulcode.commercify.user.application.command.UpdateUserStatusCommand;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import com.zenfulcode.commercify.user.domain.exception.UserAlreadyExistsException;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminUserLoader {
    private final UserApplicationService userApplicationService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void createAdminUser() {
        try {
            CreateUserCommand command = new CreateUserCommand(
                    adminEmail,
                    "User",
                    "Admin",
                    adminPassword,
                    Set.of(UserRole.ROLE_ADMIN),
                    null
            );

            UserId adminId = userApplicationService.createUser(command);
            userApplicationService.updateUserStatus(new UpdateUserStatusCommand(adminId, UserStatus.ACTIVE));

            log.info("Admin user created");
        } catch (UserAlreadyExistsException e) {
            log.warn("Admin user already exists");
        } catch (DomainException e) {
            log.warn("Failed to create admin user: {}", e.getMessage());
        }
    }
}
