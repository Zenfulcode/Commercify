package com.zenfulcode.commercify.user.domain.service;

import com.zenfulcode.commercify.order.domain.repository.OrderRepository;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserValidationPolicy implements UserValidationPolicy {
    private final OrderRepository orderRepository;

    @Override
    public boolean canTransitionToStatus(User user, UserStatus newStatus) {
        if (newStatus == UserStatus.INACTIVE || newStatus == UserStatus.SUSPENDED) {
            // Check for active orders before allowing deactivation
            // Only allow transition if user has no active orders
            boolean hasActiveOrders = orderRepository.existsByUserId(user.getId());
            return !hasActiveOrders;
        }
        // Allow all other transitions
        return true;
    }
}