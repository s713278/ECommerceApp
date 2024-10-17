package com.app.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.app.notification.services.SMSService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MobileActivationListener {

    private final SMSService smsService;
        @EventListener
        public void sendActivationLink(MobileActivationEvent event) {
            // Simulate sending an email
           log.info("Sending OTP : {}  to: {}" , event.getOtp(),event.getMobileNumber());
            // Actual email sending logic goes here

           smsService.sendOTP(event.getMobileNumber(),event.getOtp());
        }
}
