package com.S_Health.GenderHealthCare.dto.response.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialReportDTO {
    BigDecimal totalRevenue;
    double totalPendingAmount;
    double totalSuccessAmount;
    double totalFailedAmount;
    int totalTransactions;
    int successfulTransactions;
    int pendingTransactions;
    int failedTransactions;
    String fromDate;
    String toDate;
    String generatedAt;

}
