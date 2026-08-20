package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.repository.AssignedAssignmentRepository;

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
public class AssignedAssignmentRepositoryImpl implements AssignedAssignmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${assigned-assignments.page-size:10}")
    private int pageSize;

    @Override
    public AssignedAssignment getAssignedAssignmentById(Integer id) {
        return entityManager.find(AssignedAssignment.class, id);
    }

    @Override
    public AssignedAssignment getByStudentAndLearningPathDetail(
            Integer studentId,
            Integer learningPathDetailId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignedAssignment> cq = cb.createQuery(AssignedAssignment.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("assignment", JoinType.LEFT);
        root.fetch("learningPathDetail", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(
                                root.get("learningPathDetail").get("id"),
                                learningPathDetailId
                        )
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public AssignedAssignment addAssignedAssignment(AssignedAssignment assignedAssignment) {
        entityManager.persist(assignedAssignment);
        return assignedAssignment;
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByClass(
            Integer classId
    ) {
        CriteriaBuilder cb =
                entityManager.getCriteriaBuilder();

        CriteriaQuery<AssignedAssignment> cq =
                cb.createQuery(AssignedAssignment.class);

        Root<AssignedAssignment> root =
                cq.from(AssignedAssignment.class);

        root.fetch("student", JoinType.LEFT);

        root.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("learningPathDetail", JoinType.LEFT);

        root.fetch("assignedBy", JoinType.LEFT);

        Subquery<Integer> subquery =
                cq.subquery(Integer.class);

        Root<Enrollment> enrollment =
                subquery.from(Enrollment.class);

        subquery.select(cb.literal(1))
                .where(
                        cb.equal(
                                enrollment.get("student").get("id"),
                                root.get("student").get("id")
                        ),
                        cb.equal(
                                enrollment.get("courseClass").get("id"),
                                classId
                        ),
                        cb.equal(
                                enrollment.get("status"),
                                Enrollment.EnrollmentStatus.ACTIVE
                        ),
                        cb.equal(
                                enrollment.get("courseClass")
                                        .get("course")
                                        .get("id"),
                                root.get("assignment")
                                        .get("course")
                                        .get("id")
                        )
                );

        cq.select(root)
                .distinct(true)
                .where(cb.exists(subquery))
                .orderBy(
                        cb.desc(root.get("assignedAt"))
                );

        return entityManager
                .createQuery(cq)
                .getResultList();
    }

    @Override
    public void updateAssignedAssignment(AssignedAssignment assignedAssignment) {
        entityManager.merge(assignedAssignment);
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignedAssignment> cq = cb.createQuery(AssignedAssignment.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        root.fetch("student", JoinType.LEFT);

        Fetch<AssignedAssignment, ?> assignment = root.fetch("assignment", JoinType.LEFT);
        assignment.fetch("course", JoinType.LEFT);

        Fetch<AssignedAssignment, ?> detail = root.fetch("learningPathDetail", JoinType.LEFT);
        detail.fetch("learningPath", JoinType.LEFT);

        root.fetch("assignedBy", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("assignedAt")));

        TypedQuery<AssignedAssignment> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignedAssignment> cq = cb.createQuery(AssignedAssignment.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        root.fetch("student", JoinType.LEFT);

        root.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("learningPathDetail", JoinType.LEFT);
        root.fetch("assignedBy", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("student").get("id"),
                        studentId
                ))
                .orderBy(cb.desc(root.get("assignedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByStudentAndStatus(
            Integer studentId,
            AssignedAssignment.AssignedStatus status) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignedAssignment> cq = cb.createQuery(AssignedAssignment.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        root.fetch("assignment", JoinType.LEFT);
        root.fetch("learningPathDetail", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), status)
                )
                .orderBy(cb.asc(root.get("availableAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countAssignedAssignments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsByStudentAndLearningPathDetail(
            Integer studentId,
            Integer learningPathDetailId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignedAssignment> root = cq.from(AssignedAssignment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(
                                root.get("learningPathDetail").get("id"),
                                learningPathDetailId
                        )
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<AssignedAssignment> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String learningPathId = params.get("learningPathId");
        String assignmentId = params.get("assignmentId");
        String status = params.get("status");
        String date = params.get("date");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("student").get("username")), value),
                    cb.like(cb.lower(root.get("student").get("fullName")), value),
                    cb.like(cb.lower(root.get("student").get("email")), value),
                    cb.like(cb.lower(root.get("assignment").get("name")), value)
            ));
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("student").get("id"),
                        Integer.parseInt(studentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignment").get("course").get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (learningPathId != null && !learningPathId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("learningPathDetail")
                                .get("learningPath")
                                .get("id"),
                        Integer.parseInt(learningPathId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (assignmentId != null && !assignmentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignment").get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        AssignedAssignment.AssignedStatus.valueOf(status)
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
                        cb.greaterThanOrEqualTo(root.get("assignedAt"), start),
                        cb.lessThan(root.get("assignedAt"), end)
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