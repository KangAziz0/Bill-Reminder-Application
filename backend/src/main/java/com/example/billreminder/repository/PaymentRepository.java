package com.example.billreminder.repository;

import com.example.billreminder.entity.Payment;
import com.example.billreminder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserOrderByPaidDateDesc(User user);

    List<Payment> findByBill_IdAndUser(Long billId, User user);

    Optional<Payment> findByIdAndUser(Long id, User user);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p WHERE p.user = :user AND MONTH(p.paidDate) = :month AND YEAR(p.paidDate) = :year")
    BigDecimal sumPaidAmountByUserAndMonth(@Param("user") User user,
                                           @Param("month") int month,
                                           @Param("year") int year);
}
