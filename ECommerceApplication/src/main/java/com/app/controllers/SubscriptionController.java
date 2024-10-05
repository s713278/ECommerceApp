package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entites.Subscription;
import com.app.payloads.request.SubscriptionRequest;
import com.app.payloads.response.SubscriptionResponse;
import com.app.services.SubscriptionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@Tag(name = "8. Subscription Service API")
@RestController
@RequestMapping("/api/subscriptions")
@Slf4j
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Subscription>> getSubscriptions(@PathVariable Long customerId) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByCustomerId(customerId);
        return ResponseEntity.ok(subscriptions);
    }
}
