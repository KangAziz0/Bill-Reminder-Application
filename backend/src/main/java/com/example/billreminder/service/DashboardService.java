package com.example.billreminder.service;

import com.example.billreminder.dto.BillResponse;
import com.example.billreminder.dto.DashboardChartResponse;
import com.example.billreminder.dto.DashboardSummaryResponse;
import com.example.billreminder.entity.Bill;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.User;
import com.example.billreminder.repository.BillRepository;
import com.example.billreminder.repository.NotificationRepository;
import com.example.billreminder.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final BillService billService;

    public DashboardSummaryResponse getSummary(User user) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        BigDecimal totalBills = billRepository.sumAmountByUserAndMonth(user, month, year);
        BigDecimal totalPaid = paymentRepository.sumPaidAmountByUserAndMonth(user, month, year);
        BigDecimal totalUnpaid = totalBills.subtract(totalPaid).max(BigDecimal.ZERO);

        long overdueCount = billRepository.countByUserAndStatus(user, BillStatus.OVERDUE);
        long dueSoonCount = billRepository.countByUserAndStatus(user, BillStatus.DUE_SOON);
        long upcomingCount = billRepository.countByUserAndStatus(user, BillStatus.UPCOMING);
        long paidCount = billRepository.countByUserAndStatus(user, BillStatus.PAID);
        long unreadNotifications = notificationRepository.countByUserAndIsReadFalse(user);

        return DashboardSummaryResponse.builder()
                .totalBillsThisMonth(totalBills)
                .totalPaid(totalPaid)
                .totalUnpaid(totalUnpaid)
                .overdueCount(overdueCount)
                .dueSoonCount(dueSoonCount)
                .upcomingCount(upcomingCount)
                .paidCount(paidCount)
                .unreadNotifications(unreadNotifications)
                .build();
    }

    public List<BillResponse> getUpcomingBills(User user) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return billRepository.findUpcomingBillsByUser(user, today, nextWeek)
                .stream()
                .map(billService::toResponse)
                .collect(Collectors.toList());
    }

    public DashboardChartResponse getChartData(User user) {
        List<DashboardChartResponse.MonthlyTrend> monthlyTrends = getMonthlyTrends(user);
        List<DashboardChartResponse.CategoryBreakdown> categoryBreakdown = getCategoryBreakdown(user);
        List<DashboardChartResponse.StatusDistribution> statusDistribution = getStatusDistribution(user);

        return DashboardChartResponse.builder()
                .monthlyTrends(monthlyTrends)
                .categoryBreakdown(categoryBreakdown)
                .statusDistribution(statusDistribution)
                .build();
    }

    private List<DashboardChartResponse.MonthlyTrend> getMonthlyTrends(User user) {
        List<DashboardChartResponse.MonthlyTrend> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int month = date.getMonthValue();
            int year = date.getYear();

            BigDecimal totalBills = billRepository.sumAmountByUserAndMonth(user, month, year);
            BigDecimal totalPaid = paymentRepository.sumPaidAmountByUserAndMonth(user, month, year);

            trends.add(DashboardChartResponse.MonthlyTrend.builder()
                    .month(date.format(formatter))
                    .totalBills(totalBills != null ? totalBills : BigDecimal.ZERO)
                    .totalPaid(totalPaid != null ? totalPaid : BigDecimal.ZERO)
                    .build());
        }

        return trends;
    }

    private List<DashboardChartResponse.CategoryBreakdown> getCategoryBreakdown(User user) {
        List<Bill> allBills = billRepository.findByUserOrderByDueDateAsc(user);

        Map<String, BigDecimal> amountByCategory = new LinkedHashMap<>();
        Map<String, Long> countByCategory = new LinkedHashMap<>();

        for (Bill bill : allBills) {
            String category = bill.getCategory() != null ? bill.getCategory() : "Lainnya";
            amountByCategory.merge(category, bill.getAmount(), BigDecimal::add);
            countByCategory.merge(category, 1L, Long::sum);
        }

        return amountByCategory.entrySet().stream()
                .map(entry -> DashboardChartResponse.CategoryBreakdown.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .count(countByCategory.get(entry.getKey()))
                        .build())
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    private List<DashboardChartResponse.StatusDistribution> getStatusDistribution(User user) {
        List<DashboardChartResponse.StatusDistribution> distribution = new ArrayList<>();

        for (BillStatus status : BillStatus.values()) {
            long count = billRepository.countByUserAndStatus(user, status);
            if (count > 0) {
                distribution.add(DashboardChartResponse.StatusDistribution.builder()
                        .status(status.name())
                        .count(count)
                        .build());
            }
        }

        return distribution;
    }
}
