package com.main.AqarCustomer.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    @Value("${otp.length:6}")
    private int otpLength;
    private static final SecureRandom RANDOM = new SecureRandom();
    public String generate() {
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            // nextInt() generate a number between 0 and bound param "10" it generates a number between 0 and 9 because 10 is excluded
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }
}
