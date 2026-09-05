package com.lmdk.course_management_system.dto.manager.payment;

import java.util.List;

public record ManagerPaymentTransactionPageResponse(
        List<ManagerPaymentTransactionResponse> transactions,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
