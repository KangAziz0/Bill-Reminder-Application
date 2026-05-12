package com.example.billreminder.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalBillsThisMonth;
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
    private long overdueCount;
    private long dueSoonCount;
    private long upcomingCount;
    private long paidCount;
    private long unreadNotifications;
}
