package com.example.billreminder.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long billId;
    private String billTitle;
    private BigDecimal paidAmount;
    private LocalDate paidDate;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
}
