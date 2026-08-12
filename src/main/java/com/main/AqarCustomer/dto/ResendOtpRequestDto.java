package com.main.AqarCustomer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@NotBlank
@Builder
public class ResendOtpRequestDto {
    @Email(message = "Invalid email")
    @NotBlank(message = "Required field")
    private String email;
}
