package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.GradingResult;
import com.lmdk.course_management_system.repository.GradingResultRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class GradingResultRepositoryImpl implements GradingResultRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${grading-results.page-size:10}")
    private int pageSize;

    @Override
    public GradingResult getGradingResultById(Integer id) {
        return entityManager.find(GradingResult.class, id);
    }

    @Override
    public GradingResult getGradingResultByAttempt(Integer attemptId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GradingResult> cq = cb.createQuery(GradingResult.class);
        Root<GradingResult> root = cq.from(GradingResult.class);

        Fetch<GradingResult, ?> attempt = root.fetch("assignmentAttempt", JoinType.LEFT);
        Fetch<?, ?> assigned = attempt.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("teacher", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        attemptId
                ));

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<GradingResult> getGradingResultsByAttemptIds(List<Integer> attemptIds) {
        if (attemptIds == null || attemptIds.isEmpty())
            return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GradingResult> cq = cb.createQuery(GradingResult.class);
        Root<GradingResult> root = cq.from(GradingResult.class);

        Fetch<GradingResult, ?> attempt = root.fetch("assignmentAttempt", JoinType.LEFT);
        Fetch<?, ?> assigned = attempt.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);
        root.fetch("teacher", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(root.get("assignmentAttempt").get("id").in(attemptIds));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public GradingResult addGradingResult(GradingResult gradingResult) {
        entityManager.persist(gradingResult);
        return gradingResult;
    }

    @Override
    public void updateGradingResult(GradingResult gradingResult) {
        entityManager.merge(gradingResult);
    }

    @Override
    public List<GradingResult> getGradingResults(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GradingResult> cq = cb.createQuery(GradingResult.class);
        Root<GradingResult> root = cq.from(GradingResult.class);

        Fetch<GradingResult, ?> attempt = root.fetch("assignmentAttempt", JoinType.LEFT);
        Fetch<?, ?> assigned = attempt.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("teacher", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("gradedAt")));

        TypedQuery<GradingResult> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public long countGradingResults(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<GradingResult> root = cq.from(GradingResult.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsByAttempt(Integer attemptId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<GradingResult> root = cq.from(GradingResult.class);

        cq.select(cb.count(root))
                .where(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        attemptId
                ));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<GradingResult> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String assignmentId = params.get("assignmentId");
        String teacherId = params.get("teacherId");
        String passed = params.get("passed");
        String date = params.get("date");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("student")
                                    .get("username")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("student")
                                    .get("fullName")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("student")
                                    .get("email")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("assignment")
                                    .get("name")
                    ), value)
            ));
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
                                .get("student")
                                .get("id"),
                        Integer.parseInt(studentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
                                .get("assignment")
                                .get("course")
                                .get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (assignmentId != null && !assignmentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
                                .get("assignment")
                                .get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (teacherId != null && !teacherId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("teacher").get("id"),
                        Integer.parseInt(teacherId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (passed != null && !passed.isBlank()) {
            if ("true".equalsIgnoreCase(passed))
                predicates.add(cb.isTrue(
                        root.get("assignmentAttempt").get("passed")
                ));

            if ("false".equalsIgnoreCase(passed))
                predicates.add(cb.isFalse(
                        root.get("assignmentAttempt").get("passed")
                ));
        }

        if (date != null && !date.isBlank()) {
            try {
                LocalDate selectedDate = LocalDate.parse(date);
                LocalDateTime start = selectedDate.atStartOfDay();
                LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

                predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("gradedAt"), start),
                        cb.lessThan(root.get("gradedAt"), end)
                ));
            } catch (Exception ignored) {
            }
        }

        return predicates;
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}