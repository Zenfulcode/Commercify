package com.gostavdev.commercify.productsservice.feignclients;

import com.gostavdev.commercify.productsservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "users-service", url = "${user.service.url}")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/load/{userEmail}")
    UserDTO loadUserByEmail(@PathVariable String userEmail, @RequestHeader("Authorization") String authHeader);
}