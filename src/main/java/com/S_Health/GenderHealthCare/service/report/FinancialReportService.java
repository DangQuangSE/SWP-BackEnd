package com.S_Health.GenderHealthCare.service.report;

import com.S_Health.GenderHealthCare.dto.response.report.FinancialReportDTO;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import com.S_Health.GenderHealthCare.repository.PaymentRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class FinancialReportService {
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    AuthUtil authUtil;

    public BigDecimal getTodayRevenue() {
        return paymentRepository.getTodayRevenue(PaymentStatus.FAILED);
    }

    public BigDecimal getCurrentMonthRevenue() {
        return paymentRepository.getCurrentMonthRevenue(PaymentStatus.FAILED);
    }

    public BigDecimal getCurrentYearRevenue() {
        return paymentRepository.getCurrentYearRevenue(PaymentStatus.FAILED);
    }

    public BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.getRevenueByDateRange(
                PaymentStatus.FAILED,
                startDate.atStartOfDay(),
                endDate.atTime(23,59,59)
        );
    }


}
