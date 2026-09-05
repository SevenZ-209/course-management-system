package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.repository.PaymentTransactionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${payment-transactions.page-size:10}")
    private int pageSize;

    @Override
    public PaymentTransaction getTransactionById(Integer id) {
        return entityManager.find(PaymentTransaction.class, id);
    }

    @Override
    public PaymentTransaction getTransactionByIdForUpdate(Integer id) {
        return entityManager.find(PaymentTransaction.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public PaymentTransaction getTransactionByCode(String transactionCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PaymentTransaction> cq = cb.createQuery(PaymentTransaction.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        root.fetch("enrollment", JoinType.LEFT)
                .fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("transactionCode"), transactionCode));

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public PaymentTransaction addTransaction(PaymentTransaction transaction) {
        entityManager.persist(transaction);
        return transaction;
    }

    @Override
    public void updateTransaction(PaymentTransaction transaction) {
        entityManager.merge(transaction);
    }

    @Override
    public List<PaymentTransaction> getTransactions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PaymentTransaction> cq = cb.createQuery(PaymentTransaction.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        Fetch<PaymentTransaction, ?> enrollment = root.fetch("enrollment", JoinType.LEFT);
        enrollment.fetch("student", JoinType.LEFT);
        enrollment.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<PaymentTransaction> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<PaymentTransaction> getTransactionsByEnrollment(Integer enrollmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PaymentTransaction> cq = cb.createQuery(PaymentTransaction.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        cq.select(root)
                .where(cb.equal(
                        root.get("enrollment").get("id"),
                        enrollmentId
                ))
                .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countTransactions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public BigDecimal sumTransactionAmounts(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        List<Predicate> predicates = createPredicates(cb, root, params);
        Expression<BigDecimal> sum = cb.sum(root.<BigDecimal>get("amount"));

        cq.select(cb.coalesce(sum, BigDecimal.ZERO))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public List<Object[]> sumTransactionAmountsByPeriod(Map<String, String> params, String dateFormat) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        List<Predicate> predicates = createPredicates(cb, root, params);
        cq.multiselect(root.get("createdAt"), root.get("amount"))
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("createdAt")));

        return groupAmountsByPeriod(entityManager.createQuery(cq).getResultList(), dateFormat);
    }

    static List<Object[]> groupAmountsByPeriod(List<Object[]> rows, String dateFormat) {
        DateTimeFormatter formatter = "%Y-%m".equals(dateFormat)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        if(rows != null) {
            for(Object[] row : rows) {
                if(row == null || row.length < 2 || !(row[0] instanceof LocalDateTime))
                    continue;

                LocalDateTime createdAt = (LocalDateTime) row[0];
                BigDecimal amount = row[1] instanceof BigDecimal value
                        ? value : row[1] == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(row[1]));
                totals.merge(createdAt.format(formatter), amount, BigDecimal::add);
            }
        }

        return totals.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toList();
    }

    @Override
    public boolean existsByTransactionCode(String transactionCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        cq.select(cb.count(root))
                .where(cb.equal(
                        root.get("transactionCode"),
                        transactionCode
                ));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsSuccessfulTransaction(Integer enrollmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("enrollment").get("id"), enrollmentId),
                        cb.equal(root.get("status"), PaymentTransaction.TransactionStatus.SUCCESS)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsSuccessfulTransactionExceptId(Integer enrollmentId, Integer transactionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("enrollment").get("id"), enrollmentId),
                        cb.equal(root.get("status"), PaymentTransaction.TransactionStatus.SUCCESS),
                        cb.notEqual(root.get("id"), transactionId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<PaymentTransaction> getTransactionsByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PaymentTransaction> cq = cb.createQuery(PaymentTransaction.class);
        Root<PaymentTransaction> root = cq.from(PaymentTransaction.class);

        Fetch<PaymentTransaction, ?> enrollment = root.fetch("enrollment", JoinType.LEFT);
        enrollment.fetch("student", JoinType.LEFT);
        enrollment.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("enrollment").get("student").get("id"),
                        studentId
                ))
                .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<PaymentTransaction> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String enrollmentId = params.get("enrollmentId");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String status = params.get("status");
        String date = params.get("date");
        String fromDate = params.get("fromDate");
        String toDate = params.get("toDate");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("transactionCode")), value),
                    cb.like(cb.lower(
                            root.get("enrollment").get("student").get("username")
                    ), value),
                    cb.like(cb.lower(
                            root.get("enrollment").get("student").get("fullName")
                    ), value),
                    cb.like(cb.lower(
                            root.get("enrollment").get("student").get("email")
                    ), value),
                    cb.like(cb.lower(
                            root.get("enrollment").get("courseClass").get("course").get("name")
                    ), value),
                    cb.like(cb.lower(
                            root.get("enrollment").get("courseClass").get("name")
                    ), value)
            ));
        }

        if (enrollmentId != null && !enrollmentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("enrollment").get("id"),
                        Integer.parseInt(enrollmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("enrollment").get("student").get("id"),
                        Integer.parseInt(studentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("enrollment")
                                .get("courseClass")
                                .get("course")
                                .get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        PaymentTransaction.TransactionStatus.valueOf(status.trim().toUpperCase())
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (date != null && !date.isBlank()) {
            try {
                LocalDate selectedDate = LocalDate.parse(date);
                LocalDateTime start = selectedDate.atStartOfDay();
                LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

                predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), start),
                        cb.lessThan(root.get("createdAt"), end)
                ));
            } catch (Exception ignored) {
            }
        }

        addDateRangePredicates(cb, root, predicates, fromDate, toDate);
        return predicates;
    }

    private void addDateRangePredicates(CriteriaBuilder cb, Root<PaymentTransaction> root,
                                        List<Predicate> predicates, String fromDate, String toDate) {
        try {
            if(fromDate != null && !fromDate.isBlank()) {
                LocalDateTime start = LocalDate.parse(fromDate).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if(toDate != null && !toDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(toDate).plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.get("createdAt"), end));
            }
        } catch(Exception ignored) {
        }
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}