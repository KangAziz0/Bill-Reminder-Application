package com.example.billreminder.service;

import com.example.billreminder.dto.PaymentRequest;
import com.example.billreminder.dto.PaymentResponse;
import com.example.billreminder.entity.Bill;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.Payment;
import com.example.billreminder.entity.User;
import com.example.billreminder.repository.BillRepository;
import com.example.billreminder.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    public List<PaymentResponse> getAllPayments(User user) {
        return paymentRepository.findByUserOrderByPaidDateDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id, User user) {
        Payment payment = paymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, User user) {
        Bill bill = billRepository.findByIdAndUser(request.getBillId(), user)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        Payment payment = Payment.builder()
                .bill(bill)
                .user(user)
                .paidAmount(request.getPaidAmount())
                .paidDate(request.getPaidDate())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();

        // Mark bill as paid and handle recurring
        bill.setStatus(BillStatus.PAID);
        billRepository.save(bill);

        // If recurring, generate next bill
        if (Boolean.TRUE.equals(bill.getIsRecurring()) && bill.getRecurringType() != null) {
            generateNextRecurringBill(bill);
        }

        return toResponse(paymentRepository.save(payment));
    }

    private void generateNextRecurringBill(Bill originalBill) {
        LocalDate nextDueDate;
        switch (originalBill.getRecurringType().toUpperCase()) {
            case "WEEKLY":
                nextDueDate = originalBill.getDueDate().plusWeeks(1);
                break;
            case "YEARLY":
                nextDueDate = originalBill.getDueDate().plusYears(1);
                break;
            default: // MONTHLY
                nextDueDate = originalBill.getDueDate().plusMonths(1);
                break;
        }

        Bill nextBill = Bill.builder()
                .user(originalBill.getUser())
                .title(originalBill.getTitle())
                .category(originalBill.getCategory())
                .amount(originalBill.getAmount())
                .dueDate(nextDueDate)
                .reminderDaysBefore(originalBill.getReminderDaysBefore())
                .isRecurring(true)
                .recurringType(originalBill.getRecurringType())
                .build();

        LocalDate today = LocalDate.now();
        if (nextDueDate.isBefore(today)) {
            nextBill.setStatus(BillStatus.OVERDUE);
        } else if (nextDueDate.isBefore(today.plusDays(7))) {
            nextBill.setStatus(BillStatus.DUE_SOON);
        } else {
            nextBill.setStatus(BillStatus.UPCOMING);
        }

        billRepository.save(nextBill);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .billTitle(payment.getBill().getTitle())
                .paidAmount(payment.getPaidAmount())
                .paidDate(payment.getPaidDate())
                .paymentMethod(payment.getPaymentMethod())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
