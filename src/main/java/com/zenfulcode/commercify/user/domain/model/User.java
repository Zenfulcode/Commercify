package com.zenfulcode.commercify.user.domain.model;

import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.user.domain.event.UserCreatedEvent;
import com.zenfulcode.commercify.user.domain.event.UserStatusChangedEvent;
import com.zenfulcode.commercify.user.domain.exception.InvalidUserStateException;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot {
    @EmbeddedId
    private UserId id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant lastLoginAt;

    // Factory method
    public static User create(
            String email,
            String firstName,
            String lastName,
            String password,
            Set<UserRole> roles
    ) {
        User user = new User();
        user.id = UserId.generate();
        user.email = Objects.requireNonNull(email, "Email is required").toLowerCase();
        user.firstName = Objects.requireNonNull(firstName, "First name is required");
        user.lastName = Objects.requireNonNull(lastName, "Last name is required");
        user.password = Objects.requireNonNull(password, "Password is required");
        user.status = UserStatus.PENDING;
        user.roles = new HashSet<>(roles != null ? roles : Set.of(UserRole.USER));

        // Register domain event
        user.registerEvent(new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getStatus()
        ));

        return user;
    }

    // Domain methods
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail, "Email is required").toLowerCase();
    }

    public void updatePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "Password is required");
    }

    public void updateStatus(UserStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new InvalidUserStateException(
                    id,
                    status,
                    newStatus,
                    "Invalid status transition"
            );
        }

        UserStatus oldStatus = this.status;
        this.status = newStatus;

        registerEvent(new UserStatusChangedEvent(
                this.id,
                oldStatus,
                newStatus
        ));
    }

    public void updateRoles(Set<UserRole> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        this.roles = new HashSet<>(newRoles);
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private boolean canTransitionTo(UserStatus newStatus) {
        return switch (status) {
            case PENDING -> Set.of(UserStatus.ACTIVE, UserStatus.INACTIVE).contains(newStatus);
            case ACTIVE -> Set.of(UserStatus.INACTIVE, UserStatus.SUSPENDED).contains(newStatus);
            case INACTIVE -> Set.of(UserStatus.ACTIVE).contains(newStatus);
            case SUSPENDED -> Set.of(UserStatus.ACTIVE, UserStatus.INACTIVE).contains(newStatus);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
