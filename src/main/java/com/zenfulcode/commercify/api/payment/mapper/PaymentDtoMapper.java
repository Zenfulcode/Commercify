package com.zenfulcode.commercify.api.payment.mapper;

import com.zenfulcode.commercify.api.payment.request.InitiatePaymentRequest;
import com.zenfulcode.commercify.api.payment.request.PaymentDetailsRequest;
import com.zenfulcode.commercify.api.payment.response.PaymentResponse;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.application.command.CapturePaymentCommand;
import com.zenfulcode.commercify.payment.application.command.InitiatePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.InitializedPayment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentDtoMapper {
    private final OrderApplicationService orderService;

    public InitiatePaymentCommand toCommand(InitiatePaymentRequest request) {
        Order order = orderService.getOrderById(request.orderId());

        return new InitiatePaymentCommand(
                order,
                PaymentMethod.valueOf(request.paymentMethod()),
                PaymentProvider.valueOf(request.provider()),
                toProviderRequest(request.paymentDetails())
        );
    }

    public PaymentResponse toResponse(InitializedPayment response) {
        return new PaymentResponse(
                response.paymentId().toString(),
                response.redirectUrl(),
                response.additionalData()
        );
    }

    public WebhookPayload toWebhookPayload(String payload, String signature) {
        return new WebhookPayload() {
            @Override
            public String getEventType() {
                return "payment.callback";
            }

            @Override
            public String getPaymentReference() {
                return null;
            }

            @Override
            public Instant getTimestamp() {
                return Instant.now();
            }

            @Override
            public boolean isValid() {
                return true;
            }

            public String getPayload() {
                return payload;
            }

            public String getSignature() {
                return signature;
            }
        };
    }

    public CapturePaymentCommand toCaptureCommand(PaymentId paymentId, String transactionId) {
        return new CapturePaymentCommand(
                paymentId,
                transactionId,
                null
        );
    }

    private PaymentProviderRequest toProviderRequest(PaymentDetailsRequest details) {
        return new PaymentProviderRequest() {
            @Override
            public PaymentMethod getPaymentMethod() {
                return PaymentMethod.valueOf(details.paymentMethodId());
            }

            public String getPaymentMethodId() {
                return details.paymentMethodId();
            }

            public String getReturnUrl() {
                return details.returnUrl();
            }

            public String getCancelUrl() {
                return details.cancelUrl();
            }

            public Map<String, String> getAdditionalData() {
                return details.additionalData();
            }
        };
    }
}
