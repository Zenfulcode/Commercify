package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CustomerDetailsDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
