package com.example.billreminder.service;

import com.example.billreminder.dto.BillResponse;
import com.example.billreminder.dto.DashboardSummaryResponse;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.User;
import com.example.billreminder.repository.BillRepository;
import com.example.billreminder.repository.NotificationRepository;
import com.example.billreminder.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
}
