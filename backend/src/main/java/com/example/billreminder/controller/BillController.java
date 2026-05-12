package com.example.billreminder.controller;

import com.example.billreminder.dto.BillRequest;
import com.example.billreminder.dto.BillResponse;
import com.example.billreminder.entity.BillStatus;
import com.example.billreminder.entity.User;
import com.example.billreminder.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<List<BillResponse>> getAllBills(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {

        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(billService.getBillsByStatus(user, BillStatus.valueOf(status.toUpperCase())));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(billService.getBillsByCategory(user, category));
        }
        return ResponseEntity.ok(billService.getAllBills(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id,
                                                     @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.getBillById(id, user));
    }

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request,
                                                    @AuthenticationPrincipal User user) {
        BillResponse bill = billService.createBill(request, user);
        return ResponseEntity.created(URI.create("/api/bills/" + bill.getId())).body(bill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillResponse> updateBill(@PathVariable Long id,
                                                    @Valid @RequestBody BillRequest request,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.updateBill(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id,
                                            @AuthenticationPrincipal User user) {
        billService.deleteBill(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<BillResponse> markAsPaid(@PathVariable Long id,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.markAsPaid(id, user));
    }
}
