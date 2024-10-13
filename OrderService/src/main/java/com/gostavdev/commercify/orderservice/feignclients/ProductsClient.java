package com.gostavdev.commercify.orderservice.feignclients;

import com.gostavdev.commercify.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "products-service", url = "${product.service.url}")
public interface ProductsClient {
    @RequestMapping(method = RequestMethod.GET, value = "/products/{id}")
    ProductDto getProductById(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader);
}