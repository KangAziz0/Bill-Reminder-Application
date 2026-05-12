package com.example.billreminder.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long billId;
    private String billTitle;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
