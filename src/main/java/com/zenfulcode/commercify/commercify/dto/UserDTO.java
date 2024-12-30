package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private Date createdAt;
    private AddressDTO defaultAddress;
    private List<String> roles;
}
