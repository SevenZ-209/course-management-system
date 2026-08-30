package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.repository.EnrollmentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${enrollments.page-size:10}")
    private int pageSize;

    @Override
    public Enrollment getEnrollmentById(Integer id) {
        return entityManager.find(Enrollment.class, id);
    }

    @Override
    public Enrollment getEnrollmentByIdForUpdate(Integer id) {
        return entityManager.find(Enrollment.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public Enrollment getEnrollment(Integer studentId, Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("courseClass").get("id"), classId)
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Enrollment addEnrollment(Enrollment enrollment) {
        entityManager.persist(enrollment);
        return enrollment;
    }

    @Override
    public void updateEnrollment(Enrollment enrollment) {
        entityManager.merge(enrollment);
    }

    @Override
    public List<Enrollment> getEnrollments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, cq, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<Enrollment> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<Enrollment> getEnrollmentsByClass(Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("courseClass").get("id"), classId))
                .orderBy(cb.asc(root.get("student").get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Enrollment> getActiveEnrollmentsByClass(Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("courseClass").get("id"), classId),
                        cb.equal(root.get("status"), Enrollment.EnrollmentStatus.ACTIVE)
                )
                .orderBy(cb.asc(root.get("student").get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(Integer studentId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(
                                root.get("student").get("id"),
                                studentId
                        )
                )
                .orderBy(
                        cb.desc(root.get("createdAt"))
                );

        return entityManager
                .createQuery(cq)
                .getResultList();
    }

    @Override
    public long countEnrollments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        List<Predicate> predicates = createPredicates(cb, cq, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countOccupiedByClass(Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("courseClass").get("id"), classId),
                        cb.notEqual(root.get("status"), Enrollment.EnrollmentStatus.CANCELED)
                );

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countOccupiedByClassExceptId(Integer classId, Integer enrollmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("courseClass").get("id"), classId),
                        cb.notEqual(root.get("status"), Enrollment.EnrollmentStatus.CANCELED),
                        cb.notEqual(root.get("id"), enrollmentId)
                );

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsEnrollment(Integer studentId, Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("courseClass").get("id"), classId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsBlockingEnrollmentByStudentAndCourse(Integer studentId, Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("courseClass").get("course").get("id"), courseId),
                        root.get("status").in(
                                Enrollment.EnrollmentStatus.PENDING_PAYMENT,
                                Enrollment.EnrollmentStatus.ACTIVE
                        )
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsBlockingEnrollmentByStudentAndCourseExceptId(
            Integer studentId, Integer courseId, Integer enrollmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("courseClass").get("course").get("id"), courseId),
                        root.get("status").in(
                                Enrollment.EnrollmentStatus.PENDING_PAYMENT,
                                Enrollment.EnrollmentStatus.ACTIVE
                        ),
                        cb.notEqual(root.get("id"), enrollmentId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<Enrollment> getPendingEnrollments() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("status"),
                        Enrollment.EnrollmentStatus.PENDING_PAYMENT
                ))
                .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Enrollment> getActiveEnrollmentsByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        root.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), Enrollment.EnrollmentStatus.ACTIVE)
                )
                .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public boolean existsActiveEnrollmentByStudentAndCourse(Integer studentId, Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("courseClass").get("course").get("id"), courseId),
                        cb.equal(root.get("status"), Enrollment.EnrollmentStatus.ACTIVE)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsActiveEnrollmentByStudentCourseAndTeacher(
            Integer studentId,
            Integer courseId,
            Integer teacherId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(
                                root.get("courseClass").get("course").get("id"),
                                courseId
                        ),
                        cb.equal(
                                root.get("courseClass").get("teacher").get("id"),
                                teacherId
                        ),
                        cb.equal(
                                root.get("status"),
                                Enrollment.EnrollmentStatus.ACTIVE
                        )
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, CommonAbstractCriteria query,
                                             Root<Enrollment> root, Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String classId = params.get("classId");
        String status = params.get("status");
        String progressCourseId = params.get("progressCourseId");
        String progressStatus = params.get("progressStatus");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("student").get("username")), value),
                    cb.like(cb.lower(root.get("student").get("fullName")), value),
                    cb.like(cb.lower(root.get("student").get("email")), value)
            ));
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseClass").get("course").get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (classId != null && !classId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseClass").get("id"),
                        Integer.parseInt(classId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        Enrollment.EnrollmentStatus.valueOf(status)
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if(progressCourseId != null && !progressCourseId.isBlank()
                && progressStatus != null && !progressStatus.isBlank()) {
            try {
                Integer courseIdValue = Integer.parseInt(progressCourseId);
                StudentLearningPath.ProgressStatus statusValue =
                        StudentLearningPath.ProgressStatus.valueOf(progressStatus);

                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<StudentLearningPath> progress = subquery.from(StudentLearningPath.class);

                subquery.select(cb.literal(1)).where(
                        cb.equal(progress.get("student").get("id"), root.get("student").get("id")),
                        cb.equal(progress.get("learningPath").get("course").get("id"), courseIdValue),
                        cb.equal(progress.get("status"), statusValue)
                );

                predicates.add(cb.exists(subquery));
            } catch (IllegalArgumentException ignored) {
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