package com.example.billreminder.dto;

import com.example.billreminder.entity.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BillResponse {
    private Long id;
    private String title;
    private String category;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Integer reminderDaysBefore;
    private BillStatus status;
    private Boolean isRecurring;
    private String recurringType;
    private LocalDateTime createdAt;
    private Long userId;
}
