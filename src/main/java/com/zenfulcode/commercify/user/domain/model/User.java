package com.zenfulcode.commercify.user.domain.model;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.user.domain.event.UserCreatedEvent;
import com.zenfulcode.commercify.user.domain.event.UserStatusChangedEvent;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uc_users_email", columnNames = {"email"})
})
public class User extends AggregateRoot {
    @EmbeddedId
    private UserId id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    public static User create(
            String firstName,
            String lastName,
            String email,
            String password,
            Set<UserRole> roles,
            String phoneNumber
    ) {
        User user = new User();
        user.id = UserId.generate();
        user.email = Objects.requireNonNull(email, "Email is required").toLowerCase();
        user.firstName = Objects.requireNonNull(firstName, "First name is required");
        user.lastName = Objects.requireNonNull(lastName, "Last name is required");
        user.password = Objects.requireNonNull(password, "Password is required");
        user.status = UserStatus.PENDING;
        user.roles = new HashSet<>(roles != null ? roles : Set.of(UserRole.USER));
        user.phoneNumber = phoneNumber;

        // Register domain event
        user.registerEvent(new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getStatus()
        ));

        return user;
    }

    // Domain methods
    public void updateProfile(String firstName, String lastName) {
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
    }

    public void updateEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail, "Email is required").toLowerCase();
    }

    public void updatePhone(String phone) {
        this.phoneNumber = phone;
    }

    public void updatePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "Password is required");
    }

    public void updateStatus(UserStatus newStatus) {
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

    public boolean hasOutstandingPayments() {
        return false;
    }

    public boolean hasActiveOrders() {
        return orders.stream()
                .anyMatch(order -> {
                    OrderStatus status = order.getStatus();
                    return status == OrderStatus.PENDING ||
                            status == OrderStatus.CONFIRMED ||
                            status == OrderStatus.SHIPPED;
                });
    }

    public String getUsername() {
        return firstName + lastName;
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

    public String getFullName() {
        return firstName + lastName;
    }
}
