package com.lmdk.course_management_system.dto.payment;

import java.util.List;

public record PaymentTransactionPageResponse(
        List<PaymentTransactionResponse> transactions,
        int currentPage,
        int totalPages,
        long totalRecords,
        long totalTransactions,
        long successCount,
        long pendingCount,
        long failedCount
) {
}
