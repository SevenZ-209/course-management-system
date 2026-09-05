package com.lmdk.course_management_system.repository.impl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTransactionRepositoryImplGroupingTest {

    @Test
    void groupAmountsByPeriod_sameDay_keepsLocalDateAndSumsAmounts() {
        List<Object[]> rows = List.of(
                new Object[]{LocalDateTime.of(2026, 9, 1, 0, 38), new BigDecimal("10000")},
                new Object[]{LocalDateTime.of(2026, 9, 1, 0, 53), new BigDecimal("10000")}
        );

        List<Object[]> result = PaymentTransactionRepositoryImpl.groupAmountsByPeriod(rows, "%Y-%m-%d");

        assertEquals(1, result.size());
        assertEquals("2026-09-01", result.get(0)[0]);
        assertEquals(new BigDecimal("20000"), result.get(0)[1]);
    }

    @Test
    void groupAmountsByPeriod_monthly_groupsDifferentDaysIntoSameMonth() {
        List<Object[]> rows = List.of(
                new Object[]{LocalDateTime.of(2026, 8, 17, 10, 0), new BigDecimal("1200000")},
                new Object[]{LocalDateTime.of(2026, 8, 30, 20, 0), new BigDecimal("3000000")},
                new Object[]{LocalDateTime.of(2026, 9, 1, 0, 38), new BigDecimal("20000")}
        );

        List<Object[]> result = PaymentTransactionRepositoryImpl.groupAmountsByPeriod(rows, "%Y-%m");

        assertEquals(2, result.size());
        assertEquals("2026-08", result.get(0)[0]);
        assertEquals(new BigDecimal("4200000"), result.get(0)[1]);
        assertEquals("2026-09", result.get(1)[0]);
        assertEquals(new BigDecimal("20000"), result.get(1)[1]);
    }
}
