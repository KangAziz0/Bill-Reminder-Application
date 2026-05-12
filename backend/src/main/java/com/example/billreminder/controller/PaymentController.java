package com.example.billreminder.controller;

import com.example.billreminder.dto.PaymentRequest;
import com.example.billreminder.dto.PaymentResponse;
import com.example.billreminder.entity.User;
import com.example.billreminder.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getAllPayments(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id,
                                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getPaymentById(id, user));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
                                                          @AuthenticationPrincipal User user) {
        PaymentResponse payment = paymentService.createPayment(request, user);
        return ResponseEntity.created(URI.create("/api/payments/" + payment.getId())).body(payment);
    }
}
