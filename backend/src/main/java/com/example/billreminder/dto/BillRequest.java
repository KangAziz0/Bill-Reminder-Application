package com.example.billreminder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private Integer reminderDaysBefore = 3;

    private Boolean isRecurring = false;

    private String recurringType;
}
