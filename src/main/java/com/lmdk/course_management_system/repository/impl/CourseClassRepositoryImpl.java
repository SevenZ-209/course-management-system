package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.repository.CourseClassRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class CourseClassRepositoryImpl implements CourseClassRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${classes.page-size:10}")
    private int pageSize;

    @Override
    public CourseClass getClassById(Integer id) {
        return entityManager.find(CourseClass.class, id);
    }

    @Override
    public CourseClass getClassByIdForUpdate(Integer id) {
        return entityManager.find(CourseClass.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public CourseClass addClass(CourseClass courseClass) {
        entityManager.persist(courseClass);
        return courseClass;
    }

    @Override
    public void updateClass(CourseClass courseClass) {
        entityManager.merge(courseClass);
    }

    @Override
    public List<CourseClass> getClasses(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseClass> cq = cb.createQuery(CourseClass.class);
        Root<CourseClass> root = cq.from(CourseClass.class);

        root.fetch("course", JoinType.LEFT);
        root.fetch("teacher", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<CourseClass> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<CourseClass> getClassesByCourse(Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseClass> cq = cb.createQuery(CourseClass.class);
        Root<CourseClass> root = cq.from(CourseClass.class);

        cq.select(root)
                .where(cb.equal(root.get("course").get("id"), courseId))
                .orderBy(cb.desc(root.get("startDate")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<CourseClass> getAllClasses() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseClass> cq = cb.createQuery(CourseClass.class);
        Root<CourseClass> root = cq.from(CourseClass.class);

        root.fetch("course", JoinType.LEFT);

        LocalDate today = LocalDate.now();

        cq.select(root)
                .where(
                        cb.notEqual(root.get("status"), CourseClass.ClassStatus.CANCELED),
                        cb.isNotNull(root.get("startDate")),
                        cb.isNotNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), today)
                )
                .orderBy(cb.asc(root.get("startDate")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<CourseClass> getClassesByTeacher(Integer teacherId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseClass> cq = cb.createQuery(CourseClass.class);
        Root<CourseClass> root = cq.from(CourseClass.class);

        root.fetch("course", JoinType.LEFT);
        root.fetch("teacher", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(
                        cb.equal(
                                root.get("teacher").get("id"),
                                teacherId
                        )
                )
                .orderBy(cb.desc(root.get("startDate")));

        return entityManager
                .createQuery(cq)
                .getResultList();
    }

    @Override
    public long countClasses(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<CourseClass> root = cq.from(CourseClass.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<CourseClass> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String teacherId = params.get("teacherId");
        String status = params.get("status");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), value));
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("course").get("id"),
                        Integer.parseInt(courseId)
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

        if (status != null && !status.isBlank()) {
            try {
                CourseClass.ClassStatus classStatus = CourseClass.ClassStatus.valueOf(status);
                LocalDate today = LocalDate.now();
                Predicate notCanceled = cb.notEqual(root.get("status"), CourseClass.ClassStatus.CANCELED);

                switch (classStatus) {
                    case CANCELED -> predicates.add(
                            cb.equal(root.get("status"), CourseClass.ClassStatus.CANCELED)
                    );
                    case UPCOMING -> predicates.add(cb.and(
                            notCanceled,
                            cb.isNotNull(root.get("startDate")),
                            cb.greaterThan(root.get("startDate"), today)
                    ));
                    case ACTIVE -> predicates.add(cb.and(
                            notCanceled,
                            cb.isNotNull(root.get("startDate")),
                            cb.isNotNull(root.get("endDate")),
                            cb.lessThanOrEqualTo(root.get("startDate"), today),
                            cb.greaterThanOrEqualTo(root.get("endDate"), today)
                    ));
                    case COMPLETED -> predicates.add(cb.and(
                            notCanceled,
                            cb.isNotNull(root.get("endDate")),
                            cb.lessThan(root.get("endDate"), today)
                    ));
                }
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