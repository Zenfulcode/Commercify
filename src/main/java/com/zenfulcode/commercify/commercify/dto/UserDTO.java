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
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Date createdAt;
    private List<String> roles;
    private List<AddressDTO> addresses;
}
