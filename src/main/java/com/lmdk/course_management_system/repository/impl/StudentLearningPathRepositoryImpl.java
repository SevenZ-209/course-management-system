package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.repository.StudentLearningPathRepository;

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
public class StudentLearningPathRepositoryImpl implements StudentLearningPathRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${student-learning-paths.page-size:10}")
    private int pageSize;

    @Override
    public StudentLearningPath getStudentLearningPathById(Integer id) {
        return entityManager.find(StudentLearningPath.class, id);
    }

    @Override
    public StudentLearningPath getStudentLearningPath(Integer studentId, Integer learningPathId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);

        Fetch<StudentLearningPath, ?> learningPath = root.fetch("learningPath", JoinType.LEFT);
        learningPath.fetch("course", JoinType.LEFT);

        root.fetch("currentDetail", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("learningPath").get("id"), learningPathId)
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public StudentLearningPath getStudentLearningPathForUpdate(Integer studentId, Integer learningPathId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("learningPath", JoinType.LEFT).fetch("course", JoinType.LEFT);
        root.fetch("currentDetail", JoinType.LEFT).fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("learningPath").get("id"), learningPathId)
                );

        try {
            return entityManager.createQuery(cq)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public StudentLearningPath addStudentLearningPath(StudentLearningPath studentLearningPath) {
        entityManager.persist(studentLearningPath);
        return studentLearningPath;
    }

    @Override
    public void updateStudentLearningPath(StudentLearningPath studentLearningPath) {
        entityManager.merge(studentLearningPath);
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPaths(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);

        Fetch<StudentLearningPath, ?> learningPath = root.fetch("learningPath", JoinType.LEFT);
        learningPath.fetch("course", JoinType.LEFT);

        root.fetch("currentDetail", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<StudentLearningPath> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        Fetch<StudentLearningPath, ?> learningPath = root.fetch("learningPath", JoinType.LEFT);
        learningPath.fetch("course", JoinType.LEFT);

        root.fetch("currentDetail", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(root.get("student").get("id"), studentId))
                .orderBy(cb.desc(root.get("startedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourse(
            List<Integer> studentIds, Integer courseId) {
        if(studentIds == null || studentIds.isEmpty()) return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("learningPath", JoinType.LEFT).fetch("course", JoinType.LEFT);
        root.fetch("currentDetail", JoinType.LEFT).fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        root.get("student").get("id").in(studentIds),
                        cb.equal(root.get("learningPath").get("course").get("id"), courseId)
                )
                .orderBy(cb.desc(root.get("startedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourses(
            List<Integer> studentIds, List<Integer> courseIds) {
        if(studentIds == null || studentIds.isEmpty() || courseIds == null || courseIds.isEmpty())
            return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("learningPath", JoinType.LEFT).fetch("course", JoinType.LEFT);
        root.fetch("currentDetail", JoinType.LEFT).fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        root.get("student").get("id").in(studentIds),
                        root.get("learningPath").get("course").get("id").in(courseIds)
                )
                .orderBy(cb.desc(root.get("startedAt")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByLearningPath(Integer learningPathId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);
        root.fetch("currentDetail", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(cb.equal(
                        root.get("learningPath").get("id"),
                        learningPathId
                ))
                .orderBy(cb.asc(root.get("student").get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countStudentLearningPaths(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsStudentLearningPath(Integer studentId, Integer learningPathId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("learningPath").get("id"), learningPathId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<StudentLearningPath> getInProgressStudentLearningPaths() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentLearningPath> cq = cb.createQuery(StudentLearningPath.class);
        Root<StudentLearningPath> root = cq.from(StudentLearningPath.class);

        root.fetch("student", JoinType.LEFT);

        Fetch<StudentLearningPath, ?> path = root.fetch("learningPath", JoinType.LEFT);
        path.fetch("course", JoinType.LEFT);

        root.fetch("currentDetail", JoinType.LEFT)
                .fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(
                                root.get("status"),
                                StudentLearningPath.ProgressStatus.IN_PROGRESS
                        ),
                        cb.isNotNull(root.get("currentDetail"))
                )
                .orderBy(
                        cb.asc(root.get("student").get("fullName"))
                );

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<StudentLearningPath> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String learningPathId = params.get("learningPathId");
        String status = params.get("status");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("student").get("username")), value),
                    cb.like(cb.lower(root.get("student").get("fullName")), value),
                    cb.like(cb.lower(root.get("student").get("email")), value),
                    cb.like(cb.lower(root.get("learningPath").get("name")), value)
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
                        root.get("learningPath").get("course").get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (learningPathId != null && !learningPathId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("learningPath").get("id"),
                        Integer.parseInt(learningPathId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        StudentLearningPath.ProgressStatus.valueOf(status)
                ));
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