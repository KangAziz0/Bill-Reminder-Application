package com.example.billreminder.service;

import com.example.billreminder.dto.BillRequest;
import com.example.billreminder.dto.BillResponse;
import com.example.billreminder.entity.Bill;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.User;
import com.example.billreminder.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;

    public List<BillResponse> getAllBills(User user) {
        return billRepository.findByUserOrderByDueDateAsc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByStatus(User user, BillStatus status) {
        return billRepository.findByUserAndStatusOrderByDueDateAsc(user, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByCategory(User user, String category) {
        return billRepository.findByUserAndCategoryOrderByDueDateAsc(user, category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BillResponse getBillById(Long id, User user) {
        Bill bill = billRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        return toResponse(bill);
    }

    @Transactional
    public BillResponse createBill(BillRequest request, User user) {
        Bill bill = Bill.builder()
                .user(user)
                .title(request.getTitle())
                .category(request.getCategory())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .reminderDaysBefore(request.getReminderDaysBefore())
                .isRecurring(request.getIsRecurring())
                .recurringType(request.getRecurringType())
                .build();

        bill.setStatus(computeStatus(bill.getDueDate()));
        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse updateBill(Long id, BillRequest request, User user) {
        Bill bill = billRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        bill.setTitle(request.getTitle());
        bill.setCategory(request.getCategory());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
        bill.setReminderDaysBefore(request.getReminderDaysBefore());
        bill.setIsRecurring(request.getIsRecurring());
        bill.setRecurringType(request.getRecurringType());

        if (bill.getStatus() != BillStatus.PAID) {
            bill.setStatus(computeStatus(bill.getDueDate()));
        }

        return toResponse(billRepository.save(bill));
    }

    @Transactional
    public void deleteBill(Long id, User user) {
        Bill bill = billRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        billRepository.delete(bill);
    }

    @Transactional
    public BillResponse markAsPaid(Long id, User user) {
        Bill bill = billRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        bill.setStatus(BillStatus.PAID);
        return toResponse(billRepository.save(bill));
    }

    public BillStatus computeStatus(LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) return BillStatus.OVERDUE;
        if (dueDate.isBefore(today.plusDays(7))) return BillStatus.DUE_SOON;
        return BillStatus.UPCOMING;
    }

    public BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .title(bill.getTitle())
                .category(bill.getCategory())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .reminderDaysBefore(bill.getReminderDaysBefore())
                .status(bill.getStatus())
                .isRecurring(bill.getIsRecurring())
                .recurringType(bill.getRecurringType())
                .createdAt(bill.getCreatedAt())
                .userId(bill.getUser().getId())
                .build();
    }
}
