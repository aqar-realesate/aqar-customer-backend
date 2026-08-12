package com.main.AqarCustomer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterCustomerRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;
    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Mobile is required")
    @Size(max = 13, message = "Invalid phone number")
    private String phone;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "State is required")
    private String state;
//    @NotBlank(message = "Nationality ID is required")
//    @Size(max = 14, message = "Invalid nationality id")
    private Long nationalityId;
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 255, message = "Invalid address")
    private String address;
}
