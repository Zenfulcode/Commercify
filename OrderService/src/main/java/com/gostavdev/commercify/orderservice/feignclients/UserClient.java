package com.gostavdev.commercify.orderservice.feignclients;

import com.gostavdev.commercify.orderservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "users-service", url = "${user.service.url}")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    UserDTO getUserById(@PathVariable Long id, @RequestHeader("Authorization") String bearerToken);
}