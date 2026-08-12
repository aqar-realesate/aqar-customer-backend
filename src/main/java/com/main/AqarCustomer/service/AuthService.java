package com.main.AqarCustomer.service;

import com.main.AqarCustomer.dto.RegisterCustomerRequestDTO;
import com.main.AqarCustomer.model.Customer;
import com.main.AqarCustomer.repository.CustomerRepository;
import com.main.AqarCustomer.dto.ReturnObject;
import com.main.AqarCustomer.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;

    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<ReturnObject> register(RegisterCustomerRequestDTO request) {

        // Check if customer already exist by email
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.error("Customer with this email: {} already exist", request.getEmail());
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("There's a customer registered with this mail: "+ request.getEmail())
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        Customer customer = new Customer();
        customer.setName(request.getName().toLowerCase().trim());
        customer.setEmail(request.getEmail().toLowerCase().trim());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setCity(request.getCity().toLowerCase().trim());
        customer.setState(request.getState().toLowerCase().trim());
        customer.setPhone(request.getPhone().trim());   // +201044526945
        customer.setNationalityId(request.getNationalityId());   // 14 digits for Egyptian national id
        customer.setAddress(request.getAddress().trim());
        customer.setIsBlocked(false);
        customer.setBlockedAt(null);
        customerRepository.save(customer);
        otpService.generateAndSendOtp(customer);
        return new ResponseEntity<>(ReturnObject.builder()
                .message("Customer registered successfully and otp was sent to his email: " + request.getEmail())
                .status(true)
                .data(null)
                .build(),
                HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> verifyOtp(Customer customer, String inputOtp) {

        // Check if customer is blocked
        if (customer.getIsBlocked()) {
            if (LocalDateTime.now().isAfter(customer.getBlockedAt().plusMinutes(5))) {
                customer.setIsBlocked(false);
                customer.setIsBlocked(null);
                customerRepository.save(customer);
                log.info("Customer reached the block minutes now unblock the customer");
                return null;
            }

            return new ResponseEntity<>(new ReturnObject("Customer is blocked, please try again later", false, null),
                    HttpStatus.BAD_REQUEST);
        }

        // Check otp expiry
        if (LocalDateTime.now().getMinute() > customer.getOtpExpiresAt().getMinute()) {
            log.info("The otp has been expired");
            customer.setOtp(null);
            customer.setOtpExpiresAt(null);
            customerRepository.save(customer);
            return new ResponseEntity<>(new ReturnObject("Otp is expired, send a new otp", false, null),
                    HttpStatus.BAD_REQUEST);
        }

        // Check otp failed attempts
        if(customer.getFailedAttempts() > MAX_ATTEMPTS) {
            log.info("The customer reached the max otp failed attempts");
            customer.setIsBlocked(true);
            customer.setBlockedAt(LocalDateTime.now());
            customerRepository.save(customer);
            return new ResponseEntity<>(new ReturnObject("You've achieved the maximum otp failed attempts", false, null),
                    HttpStatus.BAD_REQUEST);
        }

        // Check equity of otp send and saved one
        if (!customer.getOtp().equals(inputOtp)) {
            log.info("The otp input doesn't match the saved one");
            customer.setFailedAttempts(customer.getFailedAttempts() + 1);
            customerRepository.save(customer);
            return new ResponseEntity<>(new ReturnObject("Wrong otp input", false, null),
                    HttpStatus.BAD_REQUEST);
        }

        customer.setIsVerified(true);
        customer.setIsBlocked(false);
        customer.setBlockedAt(null);
        customer.setFailedAttempts(0);
        customer.setOtpCount(0);
        customer.setOtp(null);
        customer.setOtpExpiresAt(null);
        customer.setOtpLastSentAt(null);
        customerRepository.save(customer);
        String token = jwtUtil.generateToken(customer.getEmail());
        log.info("Otp verified successfully for: {}", customer.getEmail());
        return new ResponseEntity<>(new ReturnObject("Otp verified successfully", true, token),
                HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ReturnObject> login(String email, String password) {
        // Check email
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            log.error("There's no customer with this email: {}", email);
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Invalid email or password")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        // Check blocking customer
        if (customer.getIsBlocked()) {
            if (LocalDateTime.now().isAfter(customer.getBlockedAt().plusMinutes(5))) {
                log.info("Customer reached the 5 minutes of block");
                customer.setIsBlocked(false);
                customer.setBlockedAt(null);
                customerRepository.save(customer);
                return null;
            }
            log.info("Customer still in 5 minutes block duration");
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("You are blocked try again later")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        // Check failed attempts
        if (customer.getFailedAttempts() > 5) {
            log.error("The customer exceeds the maximum failed attempts");
            customer.setIsBlocked(true);
            customer.setBlockedAt(LocalDateTime.now());
            customerRepository.save(customer);
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("You reached the maximum login failed attempts, you're blocked for 5 minutes")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        // Check the incorrect credentials
        if (!authenticateCredentials(customer.getEmail(), password, customer)) {
            log.error("The password is incorrect");
            customer.setFailedAttempts(customer.getFailedAttempts() + 1);
            customerRepository.save(customer);
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Invalid email or password")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        log.info("The email and password are correct");
        customer.setFailedAttempts(0);
        customer.setIsBlocked(false);
        customer.setBlockedAt(null);
        customer.setOtpExpiresAt(null);
        customer.setOtp(null);
        customer.setOtpLastSentAt(null);
        customer.setOtpCount(0);
        customer.setOtpVerifiedAt(null);
        customerRepository.save(customer);
        String token = jwtUtil.generateToken(email);
        return new ResponseEntity<>(ReturnObject.builder()
                .message("Login Successfully")
                .status(true)
                .data(token)
                .build(),
                HttpStatus.OK);
    }

    public ResponseEntity<ReturnObject> resendOtp(Customer customer) {

        // Check blocking customer
        if (customer.getIsBlocked()) {
            if (LocalDateTime.now().isAfter(customer.getBlockedAt().plusMinutes(5))) {
                log.info("Customer reached the 5 minutes of block");
                customer.setIsBlocked(false);
                customer.setBlockedAt(null);
                customerRepository.save(customer);
                return null;
            }
            log.info("Customer still in 5 minutes block duration");
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("You are blocked try again later")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        if (!LocalDateTime.now().isAfter(customer.getOtpLastSentAt().plusMinutes(3))) {
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("You've requested otp recently, please try again later")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            otpService.generateAndSendOtp(customer);
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("Otp sent successfully")
                    .status(true)
                    .data(null)
                    .build(),
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("There's an error happen when trying to send new otp: {}", e.getMessage());
            return new ResponseEntity<>(ReturnObject.builder()
                    .message("There's an error happen when trying to send new otp, please try again")
                    .status(false)
                    .data(null)
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

    }

    private Boolean authenticateCredentials(String email, String password, Customer customer) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(customer.getEmail(), password)
            );
            customerRepository.save(customer);
            log.info("Successful authentication for user ID: {}", customer.getId());
            return true;
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for national ID: {}", email);
            return false;
        } catch (AuthenticationException e) {
            log.error("Authentication error for user: {} - {}", email, e.getMessage());
            return false;
        }
    }

}
