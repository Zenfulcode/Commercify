package com.gostavdev.commercify.orderservice.feignclients;

import com.gostavdev.commercify.orderservice.dto.api.CancelPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "payments-service", url = "${payment.service.url}")
public interface PaymentsClient {
    @RequestMapping(method = RequestMethod.GET, value = "/payments/cancel/{orderId}")
    CancelPaymentResponse cancelPayment(@PathVariable Long orderId);
}