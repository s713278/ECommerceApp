package com.app.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.config.GlobalConfig;
import com.app.entites.Customer;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.request.OtpVerificationRequest;
import com.app.repositories.CustomerRepo;
import com.app.repositories.RoleRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepo customerRepo;
    
    private final RoleRepo roleRepo;

    private final PasswordEncoder passwordEncoder;

   private final GlobalConfig globalConfig;
   
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    public String verifyOtp(OtpVerificationRequest request) {
        // Find the user by email
        Optional<Customer> userOptional = customerRepo.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        Customer user = userOptional.get();

        // Check OTP and expiration
        if (!user.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        if (user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        // Mark user as verified
       // user.setVerified(true);
        user.setOtp(null); // Clear the OTP
        user.setOtpExpiration(null);

        customerRepo.save(user);

        return "OTP verified successfully.";
    }

    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000); // Generates 6-digit OTP
    }
    

    // Activate user account
    public boolean activateAccount(final String token) {
        Customer customer = customerRepo.findByEmailActivationToken(token);
        if (customer != null && customer.getEmailTokenExpiration().isAfter(LocalDateTime.now())) {
            customer.setEmailVerified(true);
            customer.setEmailActivationToken(null); // Clear the token after activation
            customer.setEmailTokenExpiration(null);
            customerRepo.save(customer);
            return true;
        }
        return false;
    }
    

    // Generate reset password token
    public Customer generateResetToken(String email) {
        Customer customer = customerRepo.findByEmail(email).orElseThrow(()->new ResourceNotFoundException());
        if (customer!=null) {
            customer.setResetPasswordToken(UUID.randomUUID().toString());
            customer.setEmailTokenExpiration(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
            customerRepo.save(customer);
        }
        return customer;
    }
    
 // Reset password
    public boolean resetPassword(String token, String newPassword) {
        Customer customer = customerRepo.findByResetPasswordToken(token);
        if (customer != null && customer.getEmailTokenExpiration().isAfter(LocalDateTime.now())) {
            customer.setPassword(passwordEncoder.encode(newPassword)); // Set new encoded password
            customer.setResetPasswordToken(null); // Clear the token
            customer.setEmailTokenExpiration(null);
            customerRepo.save(customer);
            return true;
        }
        return false;
    }
}
