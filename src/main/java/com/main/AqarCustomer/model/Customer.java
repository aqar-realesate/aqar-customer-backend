package com.main.AqarCustomer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.security.auth.Subject;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer implements UserDetails, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Long nationalityId;

    private Boolean isSetPassword;

    private String otp;

    private Boolean isVerified;

    private LocalDateTime otpVerifiedAt;

    private int otpCount;

    private int failedAttempts;

    private LocalDateTime otpLastSentAt;

    private LocalDateTime otpExpiresAt;

    private Boolean isBlocked;

    private LocalDateTime blockedAt;

    @CreationTimestamp
    private LocalDateTime createAt;
    @UpdateTimestamp
    private LocalDateTime updateAt;


    @Override
    public boolean isAccountNonLocked() {
        if (Boolean.TRUE.equals(this.isBlocked)) {
            if (this.blockedAt == null) {
                // Inconsistent state: blocked but no timestamp — treat as unblocked
                this.isBlocked = false;
                this.failedAttempts = 0;
                return true;
            }

            long minutesBlocked = Duration.between(this.blockedAt, LocalDateTime.now()).toMinutes();
            if (minutesBlocked >= 15) {
                this.isBlocked = false;
                this.failedAttempts = 0;
                this.blockedAt = null;
                return true;
            }

            return false;
        }
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}

