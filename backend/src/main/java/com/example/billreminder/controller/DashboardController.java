package com.example.billreminder.controller;

import com.example.billreminder.dto.BillResponse;
import com.example.billreminder.dto.DashboardChartResponse;
import com.example.billreminder.dto.DashboardSummaryResponse;
import com.example.billreminder.entity.User;
import com.example.billreminder.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getSummary(requireAuthenticatedUser(user)));
    }

    @GetMapping("/upcoming-bills")
    public ResponseEntity<List<BillResponse>> getUpcomingBills(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getUpcomingBills(requireAuthenticatedUser(user)));
    }

    @GetMapping("/charts")
    public ResponseEntity<DashboardChartResponse> getChartData(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getChartData(requireAuthenticatedUser(user)));
    }

    private User requireAuthenticatedUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "User authentication is required");
        }
        return user;
    }
}
