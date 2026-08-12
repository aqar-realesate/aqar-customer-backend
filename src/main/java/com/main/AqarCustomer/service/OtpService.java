package com.main.AqarCustomer.service;

import com.main.AqarCustomer.model.Customer;
import com.main.AqarCustomer.repository.CustomerRepository;
import com.main.AqarCustomer.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;

    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    // Generate and send a fresh otp for the given email
    @Transactional
    public void generateAndSendOtp(Customer customer) {
        String otp = otpGenerator.generate();
        customer.setOtp(otp);
        customer.setOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        customer.setOtpLastSentAt(LocalDateTime.now());
        customer.setOtpCount(customer.getOtpCount() + 1);
        customer.setFailedAttempts(0);
        customerRepository.save(customer);
//        emailService.sendOtpMail(customer.getEmail(), otp, customer.getName());
        log.info("Otp generated and sent to: {}", customer.getEmail());
    }

}
