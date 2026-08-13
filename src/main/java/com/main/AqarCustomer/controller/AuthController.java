package com.main.AqarCustomer.controller;

import com.main.AqarCustomer.dto.*;
import com.main.AqarCustomer.model.Customer;
import com.main.AqarCustomer.repository.CustomerRepository;
import com.main.AqarCustomer.service.AuthService;
import com.main.AqarCustomer.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CustomerRepository customerRepository;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<ReturnObject> register(@Valid @RequestBody RegisterCustomerRequestDTO request) {
        if (request == null) {
            log.error("the request is empty");
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Please fill required fields")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @Valid @RequestBody VerifyOtpRequestDto request) {
        if (request == null) {
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Please fill the otp field")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }
        Customer customer = customerRepository.findByEmail(email);
        return authService.verifyOtp(customer, request.getOtp());
    }

    @PostMapping("/login")
    public ResponseEntity<ReturnObject> login(@Valid @RequestBody LoginRequestDto request) {
        if (request == null) {
            log.error("the login request is empty");
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Please fill required fields")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ReturnObject> reSendOtp(@Valid @RequestBody ResendOtpRequestDto request) {
        if (request == null) {
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Please fill the otp field")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }
        Customer customer = customerRepository.findByEmail(request.getEmail());
        return authService.resendOtp(customer);
    }
}
