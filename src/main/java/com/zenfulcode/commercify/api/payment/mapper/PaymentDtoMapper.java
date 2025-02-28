package com.zenfulcode.commercify.api.payment.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenfulcode.commercify.api.payment.dto.request.InitiatePaymentRequest;
import com.zenfulcode.commercify.api.payment.dto.request.PaymentDetailsRequest;
import com.zenfulcode.commercify.api.payment.dto.response.CapturedPaymentResponse;
import com.zenfulcode.commercify.api.payment.dto.response.PaymentResponse;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.application.command.CapturePaymentCommand;
import com.zenfulcode.commercify.payment.application.command.InitiatePaymentCommand;
import com.zenfulcode.commercify.payment.application.dto.CapturedPayment;
import com.zenfulcode.commercify.payment.application.dto.InitializedPayment;
import com.zenfulcode.commercify.payment.application.service.PaymentApplicationService;
import com.zenfulcode.commercify.payment.domain.exception.WebhookProcessingException;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.valueobject.MobilepayPaymentRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.WebhookRequest;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.MobilepayWebhookPayload;
import com.zenfulcode.commercify.payment.domain.valueobject.webhook.WebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentDtoMapper {
    private final PaymentApplicationService paymentService;
    private final OrderApplicationService orderService;
    private final ObjectMapper objectMapper;

    public InitiatePaymentCommand toCommand(InitiatePaymentRequest request) {
        Order order = orderService.getOrderById(request.getOrderId());

        return new InitiatePaymentCommand(
                order,
                PaymentMethod.valueOf(request.paymentDetails().paymentMethod()),
                paymentService.getPaymentProvider(request.provider()),
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

    //    TODO: Make more generic to support other providers
    public WebhookPayload toWebhookPayload(WebhookRequest request) {
        try {
            return objectMapper.readValue(request.body(), MobilepayWebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new WebhookProcessingException(e.getMessage());
        }
    }

    public CapturePaymentCommand toCaptureCommand(PaymentId paymentId) {
        return new CapturePaymentCommand(
                paymentId,
                null
        );
    }

    private PaymentProviderRequest toProviderRequest(PaymentDetailsRequest details) {
        return new MobilepayPaymentRequest(
                PaymentMethod.valueOf(details.paymentMethod()),
                details.additionalData().get("phoneNumber"),
                details.returnUrl()
        );
    }

    public CapturedPaymentResponse toCapturedResponse(CapturedPayment capturedPayment) {
        return new CapturedPaymentResponse(
                capturedPayment.transactionId(),
                capturedPayment.captureAmount(),
                capturedPayment.isFullyCaptured()
        );
    }
}
