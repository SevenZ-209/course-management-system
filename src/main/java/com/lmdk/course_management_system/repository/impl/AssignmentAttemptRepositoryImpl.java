package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.repository.AssignmentAttemptRepository;

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
public class AssignmentAttemptRepositoryImpl implements AssignmentAttemptRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${assignment-attempts.page-size:10}")
    private int pageSize;

    @Override
    public AssignmentAttempt getAttemptById(Integer id) {
        return entityManager.find(AssignmentAttempt.class, id);
    }

    @Override
    public AssignmentAttempt getAttempt(Integer assignedAssignmentId, Integer attemptNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        root.fetch("assignedAssignment", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(
                                root.get("assignedAssignment").get("id"),
                                assignedAssignmentId
                        ),
                        cb.equal(root.get("attemptNumber"), attemptNumber)
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public AssignmentAttempt getLatestAttempt(Integer assignedAssignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        cq.select(root)
                .where(cb.equal(
                        root.get("assignedAssignment").get("id"),
                        assignedAssignmentId
                ))
                .orderBy(cb.desc(root.get("attemptNumber")));

        List<AssignmentAttempt> results = entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public AssignmentAttempt addAttempt(AssignmentAttempt attempt) {
        entityManager.persist(attempt);
        return attempt;
    }

    @Override
    public void updateAttempt(AssignmentAttempt attempt) {
        entityManager.merge(attempt);
    }

    @Override
    public List<AssignmentAttempt> getAttempts(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        Fetch<AssignmentAttempt, ?> assigned = root.fetch("assignedAssignment", JoinType.LEFT);
        assigned.fetch("student", JoinType.LEFT);

        Fetch<?, ?> assignment = assigned.fetch("assignment", JoinType.LEFT);
        assignment.fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("startedAt")));

        TypedQuery<AssignmentAttempt> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<AssignmentAttempt> getAttemptsByAssignedAssignment(Integer assignedAssignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        cq.select(root)
                .where(cb.equal(
                        root.get("assignedAssignment").get("id"),
                        assignedAssignmentId
                ))
                .orderBy(cb.asc(root.get("attemptNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countAttempts(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countAttemptsByAssignedAssignment(Integer assignedAssignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        cq.select(cb.count(root))
                .where(cb.equal(
                        root.get("assignedAssignment").get("id"),
                        assignedAssignmentId
                ));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsAttemptNumber(Integer assignedAssignmentId, Integer attemptNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(
                                root.get("assignedAssignment").get("id"),
                                assignedAssignmentId
                        ),
                        cb.equal(root.get("attemptNumber"), attemptNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsInProgressAttempt(Integer assignedAssignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(
                                root.get("assignedAssignment").get("id"),
                                assignedAssignmentId
                        ),
                        cb.equal(
                                root.get("status"),
                                AssignmentAttempt.AttemptStatus.IN_PROGRESS
                        )
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public AssignmentAttempt getInProgressAttempt(Integer assignedAssignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        root.fetch("assignedAssignment", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(
                                root.get("assignedAssignment").get("id"),
                                assignedAssignmentId
                        ),
                        cb.equal(
                                root.get("status"),
                                AssignmentAttempt.AttemptStatus.IN_PROGRESS
                        )
                );

        List<AssignmentAttempt> results = entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<AssignmentAttempt> getPendingGradingAttempts() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        Fetch<AssignmentAttempt, ?> assigned =
                root.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("status"),
                        AssignmentAttempt.AttemptStatus.PENDING_GRADING
                ))
                .orderBy(cb.asc(root.get("submittedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<AssignmentAttempt> getPendingGradingAttemptsByTeacher(
            Integer teacherId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignmentAttempt> cq = cb.createQuery(AssignmentAttempt.class);
        Root<AssignmentAttempt> root = cq.from(AssignmentAttempt.class);

        Fetch<AssignmentAttempt, ?> assigned =
                root.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        Subquery<Integer> subquery = cq.subquery(Integer.class);
        Root<Enrollment> enrollment = subquery.from(Enrollment.class);

        subquery.select(cb.literal(1))
                .where(
                        cb.equal(
                                enrollment.get("student").get("id"),
                                root.get("assignedAssignment")
                                        .get("student")
                                        .get("id")
                        ),
                        cb.equal(
                                enrollment.get("courseClass")
                                        .get("course")
                                        .get("id"),
                                root.get("assignedAssignment")
                                        .get("assignment")
                                        .get("course")
                                        .get("id")
                        ),
                        cb.equal(
                                enrollment.get("courseClass")
                                        .get("teacher")
                                        .get("id"),
                                teacherId
                        ),
                        cb.equal(
                                enrollment.get("status"),
                                Enrollment.EnrollmentStatus.ACTIVE
                        )
                );

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(
                                root.get("status"),
                                AssignmentAttempt.AttemptStatus.PENDING_GRADING
                        ),
                        cb.exists(subquery)
                )
                .orderBy(cb.asc(root.get("submittedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<AssignmentAttempt> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String assignmentId = params.get("assignmentId");
        String assignedAssignmentId = params.get("assignedAssignmentId");
        String status = params.get("status");
        String passed = params.get("passed");
        String date = params.get("date");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(
                            root.get("assignedAssignment")
                                    .get("student")
                                    .get("username")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignedAssignment")
                                    .get("student")
                                    .get("fullName")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignedAssignment")
                                    .get("student")
                                    .get("email")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignedAssignment")
                                    .get("assignment")
                                    .get("name")
                    ), value)
            ));
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignedAssignment")
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
                        root.get("assignedAssignment")
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
                        root.get("assignedAssignment")
                                .get("assignment")
                                .get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (assignedAssignmentId != null && !assignedAssignmentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignedAssignment").get("id"),
                        Integer.parseInt(assignedAssignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        AssignmentAttempt.AttemptStatus.valueOf(status)
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (passed != null && !passed.isBlank()) {
            if ("true".equalsIgnoreCase(passed))
                predicates.add(cb.isTrue(root.get("passed")));

            if ("false".equalsIgnoreCase(passed))
                predicates.add(cb.isFalse(root.get("passed")));
        }

        if (date != null && !date.isBlank()) {
            try {
                LocalDate selectedDate = LocalDate.parse(date);
                LocalDateTime start = selectedDate.atStartOfDay();
                LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

                predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("startedAt"), start),
                        cb.lessThan(root.get("startedAt"), end)
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