package com.lmdk.course_management_system.dto.admin.payment;

import java.util.List;

public record AdminPaymentTransactionPageResponse(
        List<AdminPaymentTransactionResponse> transactions,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}