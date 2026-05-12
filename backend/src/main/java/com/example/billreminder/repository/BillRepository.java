package com.example.billreminder.repository;

import com.example.billreminder.entity.Bill;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUserOrderByDueDateAsc(User user);

    List<Bill> findByUserAndStatusOrderByDueDateAsc(User user, BillStatus status);

    List<Bill> findByUserAndCategoryOrderByDueDateAsc(User user, String category);

    Optional<Bill> findByIdAndUser(Long id, User user);

    List<Bill> findByStatusNot(BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.user = :user AND b.status != 'PAID' AND b.dueDate BETWEEN :startDate AND :endDate")
    List<Bill> findUpcomingBillsByUser(@Param("user") User user,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.user = :user AND MONTH(b.dueDate) = :month AND YEAR(b.dueDate) = :year")
    BigDecimal sumAmountByUserAndMonth(@Param("user") User user,
                                       @Param("month") int month,
                                       @Param("year") int year);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user = :user AND b.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.status != 'PAID' AND b.dueDate < :today")
    List<Bill> findOverdueBills(@Param("today") LocalDate today);

    @Query("SELECT b FROM Bill b WHERE b.status != 'PAID' AND b.dueDate BETWEEN :startDate AND :endDate")
    List<Bill> findBillsDueBetween(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
}
