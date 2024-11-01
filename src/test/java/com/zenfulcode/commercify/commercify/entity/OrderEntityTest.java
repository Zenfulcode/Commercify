package com.zenfulcode.commercify.commercify.entity;

import com.zenfulcode.commercify.commercify.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderEntity Tests")
class OrderEntityTest {

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Should create order using no-args constructor")
        void noArgsConstructor() {
            // Act
            OrderEntity order = new OrderEntity();

            // Assert
            assertNotNull(order);
            assertNull(order.getId());
            assertNull(order.getUserId());
            assertNull(order.getStatus());
        }

        @Test
        @DisplayName("Should create order using builder")
        void builder() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<OrderLineEntity> orderLines = new ArrayList<>();

            // Act
            OrderEntity order = OrderEntity.builder()
                    .id(1L)
                    .userId(100L)
                    .orderLines(orderLines)
                    .status(OrderStatus.PENDING)
                    .currency("USD")
                    .totalAmount(99.99)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Assert
            assertThat(order)
                    .hasFieldOrPropertyWithValue("id", 1L)
                    .hasFieldOrPropertyWithValue("userId", 100L)
                    .hasFieldOrPropertyWithValue("orderLines", orderLines)
                    .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                    .hasFieldOrPropertyWithValue("currency", "USD")
                    .hasFieldOrPropertyWithValue("totalAmount", 99.99)
                    .hasFieldOrPropertyWithValue("createdAt", now)
                    .hasFieldOrPropertyWithValue("updatedAt", now);
        }

        @Test
        @DisplayName("Should create order using all-args constructor")
        void allArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<OrderLineEntity> orderLines = new ArrayList<>();

            // Act
            OrderEntity order = new OrderEntity(
                    1L, 100L, orderLines, OrderStatus.PENDING,
                    "USD", 99.99, now, now
            );

            // Assert
            assertThat(order)
                    .hasFieldOrPropertyWithValue("id", 1L)
                    .hasFieldOrPropertyWithValue("userId", 100L)
                    .hasFieldOrPropertyWithValue("orderLines", orderLines)
                    .hasFieldOrPropertyWithValue("status", OrderStatus.PENDING)
                    .hasFieldOrPropertyWithValue("currency", "USD")
                    .hasFieldOrPropertyWithValue("totalAmount", 99.99)
                    .hasFieldOrPropertyWithValue("createdAt", now)
                    .hasFieldOrPropertyWithValue("updatedAt", now);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get all fields")
        void gettersAndSetters() {
            // Arrange
            OrderEntity order = new OrderEntity();
            LocalDateTime now = LocalDateTime.now();
            List<OrderLineEntity> orderLines = new ArrayList<>();

            // Act
            order.setId(1L);
            order.setUserId(100L);
            order.setOrderLines(orderLines);
            order.setStatus(OrderStatus.PENDING);
            order.setCurrency("USD");
            order.setTotalAmount(99.99);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            // Assert
            assertAll(
                    () -> assertEquals(1L, order.getId()),
                    () -> assertEquals(100L, order.getUserId()),
                    () -> assertEquals(orderLines, order.getOrderLines()),
                    () -> assertEquals(OrderStatus.PENDING, order.getStatus()),
                    () -> assertEquals("USD", order.getCurrency()),
                    () -> assertEquals(99.99, order.getTotalAmount()),
                    () -> assertEquals(now, order.getCreatedAt()),
                    () -> assertEquals(now, order.getUpdatedAt())
            );
        }
    }

    @Nested
    @DisplayName("Order Status Tests")
    class OrderStatusTests {

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        @DisplayName("Should handle all order statuses")
        void orderStatus(OrderStatus status) {
            // Arrange
            OrderEntity order = new OrderEntity();

            // Act
            order.setStatus(status);

            // Assert
            assertEquals(status, order.getStatus());
        }
    }

    @Nested
    @DisplayName("Order Lines Tests")
    class OrderLinesTests {

        @Test
        @DisplayName("Should manage order lines list")
        void orderLinesList() {
            // Arrange
            OrderEntity order = new OrderEntity();
            List<OrderLineEntity> orderLines = new ArrayList<>();
            OrderLineEntity orderLine1 = new OrderLineEntity();
            OrderLineEntity orderLine2 = new OrderLineEntity();

            // Act
            orderLines.add(orderLine1);
            orderLines.add(orderLine2);
            order.setOrderLines(orderLines);

            // Assert
            assertThat(order.getOrderLines())
                    .hasSize(2)
                    .contains(orderLine1, orderLine2);
        }

        @Test
        @DisplayName("Should allow empty order lines list")
        void emptyOrderLinesList() {
            // Arrange
            OrderEntity order = new OrderEntity();

            // Act
            order.setOrderLines(new ArrayList<>());

            // Assert
            assertThat(order.getOrderLines())
                    .isNotNull()
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("Currency and Amount Tests")
    class CurrencyAndAmountTests {

        @Test
        @DisplayName("Should handle currency and amount")
        void currencyAndAmount() {
            // Arrange
            OrderEntity order = new OrderEntity();

            // Act
            order.setCurrency("EUR");
            order.setTotalAmount(199.99);

            // Assert
            assertAll(
                    () -> assertEquals("EUR", order.getCurrency()),
                    () -> assertEquals(199.99, order.getTotalAmount())
            );
        }

        @Test
        @DisplayName("Should handle null amount")
        void nullAmount() {
            // Arrange
            OrderEntity order = new OrderEntity();

            // Act & Assert
            assertDoesNotThrow(() -> order.setTotalAmount(null));
            assertNull(order.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should handle timestamps")
        void timestamps() {
            // Arrange
            OrderEntity order = new OrderEntity();
            LocalDateTime now = LocalDateTime.now();

            // Act
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            // Assert
            assertAll(
                    () -> assertEquals(now, order.getCreatedAt()),
                    () -> assertEquals(now, order.getUpdatedAt())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should implement equals and hashCode correctly")
        void equalsAndHashCode() {
            // Arrange
            OrderEntity order1 = OrderEntity.builder()
                    .id(1L)
                    .userId(100L)
                    .status(OrderStatus.PENDING)
                    .build();

            OrderEntity order2 = OrderEntity.builder()
                    .id(1L)
                    .userId(100L)
                    .status(OrderStatus.PENDING)
                    .build();

            OrderEntity differentOrder = OrderEntity.builder()
                    .id(2L)
                    .userId(100L)
                    .status(OrderStatus.PENDING)
                    .build();

            // Assert
            assertAll(
                    () -> assertEquals(order1, order2),
                    () -> assertNotEquals(order1, differentOrder),
                    () -> assertEquals(order1.hashCode(), order2.hashCode()),
                    () -> assertNotEquals(order1.hashCode(), differentOrder.hashCode())
            );
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should implement toString correctly")
        void toStringImplementation() {
            // Arrange
            OrderEntity order = OrderEntity.builder()
                    .id(1L)
                    .userId(100L)
                    .status(OrderStatus.PENDING)
                    .currency("USD")
                    .totalAmount(99.99)
                    .build();

            // Act
            String toString = order.toString();

            // Assert
            assertThat(toString)
                    .contains("id=1")
                    .contains("userId=100")
                    .contains("status=PENDING")
                    .contains("currency=USD")
                    .contains("totalAmount=99.99");
        }
    }
}