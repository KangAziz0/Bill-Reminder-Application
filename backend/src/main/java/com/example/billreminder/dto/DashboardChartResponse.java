package com.example.billreminder.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardChartResponse {

    private List<MonthlyTrend> monthlyTrends;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<StatusDistribution> statusDistribution;

    @Data
    @Builder
    public static class MonthlyTrend {
        private String month;
        private BigDecimal totalBills;
        private BigDecimal totalPaid;
    }

    @Data
    @Builder
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private long count;
    }

    @Data
    @Builder
    public static class StatusDistribution {
        private String status;
        private long count;
    }
}
