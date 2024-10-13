package com.gostavdev.commercify.paymentservice;

import com.gostavdev.commercify.paymentservice.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderClient {
    @RequestMapping(method = RequestMethod.GET, value = "/orders/{id}")
    OrderDTO getOrderById(@PathVariable Long id);

    @RequestMapping(method = RequestMethod.PUT, value = "/orders/{id}/status")
    void updateOrderStatus(@PathVariable Long id, String status);
}