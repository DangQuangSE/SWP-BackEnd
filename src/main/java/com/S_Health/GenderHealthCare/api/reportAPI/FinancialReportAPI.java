package com.S_Health.GenderHealthCare.api.reportAPI;

import com.S_Health.GenderHealthCare.service.report.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/financial-reports")
public class FinancialReportAPI {
    @Autowired
    FinancialReportService financialReportService;

    @GetMapping("/revenue-today")
    public BigDecimal getTodayRevenue() {
        return financialReportService.getTodayRevenue();
    }

    @GetMapping("/revenue-month")
    public BigDecimal getCurrentMonthRevenue() {
        return financialReportService.getCurrentMonthRevenue();
    }

    @GetMapping("/revenue-year")
    public BigDecimal getCurrentYearRevenue() {
        return financialReportService.getCurrentYearRevenue();
    }

    @GetMapping("/revenue-by-date-range")
    public BigDecimal getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return financialReportService.getRevenueByDateRange(startDate, endDate);
    }
}
