package com.zenfulcode.commercify.commercify.flow;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateFlowTest {

    @Nested
    @DisplayName("Order State Flow Tests")
    class OrderStateFlowTest {
        private final OrderStateFlow orderStateFlow = new OrderStateFlow();

        @Test
        @DisplayName("PENDING order can transition to CONFIRMED or CANCELLED")
        void testPendingTransitions() {
            Set<OrderStatus> validTransitions = orderStateFlow.getValidTransitions(OrderStatus.PENDING);
            assertTrue(orderStateFlow.canTransition(OrderStatus.PENDING, OrderStatus.PAID));
            assertTrue(orderStateFlow.canTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
            assertEquals(2, validTransitions.size());
            assertTrue(validTransitions.contains(OrderStatus.PAID));
            assertTrue(validTransitions.contains(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("PAID order can transition to SHIPPED, CANCELLED and COMPLETED")
        void testConfirmedTransitions() {
            Set<OrderStatus> validTransitions = orderStateFlow.getValidTransitions(OrderStatus.PAID);
            assertTrue(orderStateFlow.canTransition(OrderStatus.PAID, OrderStatus.SHIPPED));
            assertTrue(orderStateFlow.canTransition(OrderStatus.PAID, OrderStatus.CANCELLED));
            assertTrue(orderStateFlow.canTransition(OrderStatus.PAID, OrderStatus.COMPLETED));

            assertEquals(3, validTransitions.size());
        }

        @Test
        @DisplayName("SHIPPED order can transition to COMPLETED or RETURNED")
        void testShippedTransitions() {
            Set<OrderStatus> validTransitions = orderStateFlow.getValidTransitions(OrderStatus.SHIPPED);
            assertTrue(orderStateFlow.canTransition(OrderStatus.SHIPPED, OrderStatus.COMPLETED));
            assertTrue(orderStateFlow.canTransition(OrderStatus.SHIPPED, OrderStatus.RETURNED));
            assertEquals(2, validTransitions.size());
        }

        @Test
        @DisplayName("Terminal states cannot transition")
        void testTerminalStates() {
            OrderStatus[] terminalStates = {
                    OrderStatus.COMPLETED, OrderStatus.CANCELLED, OrderStatus.RETURNED, OrderStatus.REFUNDED
            };

            for (OrderStatus status : terminalStates) {
                assertTrue(orderStateFlow.isTerminalState(status));
                assertTrue(orderStateFlow.getValidTransitions(status).isEmpty());
            }
        }

        @Test
        @DisplayName("Invalid transitions are not allowed")
        void testInvalidTransitions() {
            assertFalse(orderStateFlow.canTransition(OrderStatus.PENDING, OrderStatus.COMPLETED));
            assertFalse(orderStateFlow.canTransition(OrderStatus.PAID, OrderStatus.RETURNED));
            assertFalse(orderStateFlow.canTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
            assertFalse(orderStateFlow.canTransition(OrderStatus.COMPLETED, OrderStatus.SHIPPED));
        }
    }

    @Nested
    @DisplayName("Payment State Flow Tests")
    class PaymentStateFlowTest {
        private final PaymentStateFlow paymentStateFlow = new PaymentStateFlow();

        @Test
        @DisplayName("PENDING payment can transition to PAID, FAILED, CANCELLED, or EXPIRED")
        void testPendingTransitions() {
            Set<PaymentStatus> validTransitions = paymentStateFlow.getValidTransitions(PaymentStatus.PENDING);
            assertTrue(paymentStateFlow.canTransition(PaymentStatus.PENDING, PaymentStatus.PAID));
            assertTrue(paymentStateFlow.canTransition(PaymentStatus.PENDING, PaymentStatus.FAILED));
            assertTrue(paymentStateFlow.canTransition(PaymentStatus.PENDING, PaymentStatus.CANCELLED));
            assertTrue(paymentStateFlow.canTransition(PaymentStatus.PENDING, PaymentStatus.EXPIRED));
            assertEquals(4, validTransitions.size());
        }

        @Test
        @DisplayName("PAID payment can only transition to REFUNDED")
        void testPaidTransitions() {
            Set<PaymentStatus> validTransitions = paymentStateFlow.getValidTransitions(PaymentStatus.PAID);
            assertTrue(paymentStateFlow.canTransition(PaymentStatus.PAID, PaymentStatus.REFUNDED));
            assertEquals(1, validTransitions.size());
            assertTrue(validTransitions.contains(PaymentStatus.REFUNDED));
        }

        @Test
        @DisplayName("Terminal payment states cannot transition")
        void testTerminalStates() {
            PaymentStatus[] terminalStates = {
                    PaymentStatus.FAILED, PaymentStatus.CANCELLED, PaymentStatus.REFUNDED,
                    PaymentStatus.EXPIRED, PaymentStatus.TERMINATED
            };

            for (PaymentStatus status : terminalStates) {
                assertTrue(paymentStateFlow.isTerminalState(status));
                assertTrue(paymentStateFlow.getValidTransitions(status).isEmpty());
            }
        }

        @Test
        @DisplayName("Invalid payment transitions are not allowed")
        void testInvalidTransitions() {
            assertFalse(paymentStateFlow.canTransition(PaymentStatus.PENDING, PaymentStatus.TERMINATED));
            assertFalse(paymentStateFlow.canTransition(PaymentStatus.PAID, PaymentStatus.CANCELLED));
            assertFalse(paymentStateFlow.canTransition(PaymentStatus.FAILED, PaymentStatus.PAID));
            assertFalse(paymentStateFlow.canTransition(PaymentStatus.REFUNDED, PaymentStatus.PENDING));
        }
    }
}