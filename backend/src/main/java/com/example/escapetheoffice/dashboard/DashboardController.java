package com.example.escapetheoffice.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.dashboard.dto.DashboardResponse;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // この URL でできることは1つなので、これ以上パスを掘らない
    @GetMapping
    public DashboardResponse find() {
        return dashboardService.find();
    }
}
