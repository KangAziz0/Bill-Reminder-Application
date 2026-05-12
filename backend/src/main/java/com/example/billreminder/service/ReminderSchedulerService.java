package com.example.billreminder.service;

import com.example.billreminder.entity.Bill;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.Notification;
import com.example.billreminder.repository.BillRepository;
import com.example.billreminder.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final BillRepository billRepository;
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    /**
     * Runs every day at 08:00 AM
     * 1. Update overdue bills
     * 2. Send reminders for bills approaching due date
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processReminders() {
        log.info("Running daily bill reminder scheduler...");
        LocalDate today = LocalDate.now();

        // 1. Mark overdue bills
        List<Bill> overdueBills = billRepository.findOverdueBills(today);
        for (Bill bill : overdueBills) {
            if (bill.getStatus() != BillStatus.OVERDUE) {
                bill.setStatus(BillStatus.OVERDUE);
                billRepository.save(bill);
                log.info("Bill [{}] marked as OVERDUE", bill.getTitle());
            }
        }

        // 2. Send reminders
        List<Bill> allActiveBills = billRepository.findByStatusNot(BillStatus.PAID);
        for (Bill bill : allActiveBills) {
            if (bill.getDueDate() == null || bill.getReminderDaysBefore() == null) continue;

            long daysUntilDue = today.until(bill.getDueDate()).getDays();

            // Check if today matches one of the reminder intervals (H-1, H-3, H-7 etc.)
            if (daysUntilDue == bill.getReminderDaysBefore()
                    || daysUntilDue == 1
                    || daysUntilDue == 3) {
                if (daysUntilDue >= 0) {
                    createReminderNotification(bill, daysUntilDue);
                    sendReminderEmail(bill, daysUntilDue);

                    // Update to DUE_SOON if within 7 days
                    if (daysUntilDue <= 7 && bill.getStatus() != BillStatus.DUE_SOON) {
                        bill.setStatus(BillStatus.DUE_SOON);
                        billRepository.save(bill);
                    }
                }
            }
        }

        log.info("Scheduler finished. Processed {} overdue and sent reminders.", overdueBills.size());
    }

    private void createReminderNotification(Bill bill, long daysUntilDue) {
        String message;
        if (daysUntilDue == 0) {
            message = String.format("Tagihan '%s' jatuh tempo HARI INI! Segera lakukan pembayaran.", bill.getTitle());
        } else {
            message = String.format("Pengingat: Tagihan '%s' akan jatuh tempo dalam %d hari (Rp %s).",
                    bill.getTitle(), daysUntilDue, bill.getAmount().toPlainString());
        }

        Notification notification = Notification.builder()
                .user(bill.getUser())
                .bill(bill)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for bill [{}]", bill.getTitle());
    }

    private void sendReminderEmail(Bill bill, long daysUntilDue) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(bill.getUser().getEmail());
            message.setSubject("[Bill Reminder] " + bill.getTitle() + " - Jatuh Tempo " + daysUntilDue + " Hari Lagi");
            message.setText(
                    "Halo " + bill.getUser().getName() + ",\n\n" +
                    "Ini adalah pengingat bahwa tagihan Anda akan segera jatuh tempo:\n\n" +
                    "Tagihan   : " + bill.getTitle() + "\n" +
                    "Nominal   : Rp " + bill.getAmount().toPlainString() + "\n" +
                    "Jatuh Tempo: " + bill.getDueDate() + "\n" +
                    "Sisa Hari : " + daysUntilDue + " hari\n\n" +
                    "Segera lakukan pembayaran untuk menghindari keterlambatan.\n\n" +
                    "Salam,\nBill Reminder App"
            );
            mailSender.send(message);
            log.info("Reminder email sent to {}", bill.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send reminder email for bill [{}]: {}", bill.getTitle(), e.getMessage());
        }
    }
}
