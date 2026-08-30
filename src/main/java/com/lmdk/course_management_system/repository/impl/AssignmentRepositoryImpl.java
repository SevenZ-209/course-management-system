package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.repository.AssignmentRepository;

import jakarta.persistence.EntityManager;
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
public class AssignmentRepositoryImpl implements AssignmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${assignments.page-size:10}")
    private int pageSize;

    @Override
    public Assignment getAssignmentById(Integer id) {
        return entityManager.find(Assignment.class, id);
    }

    @Override
    public Assignment addAssignment(Assignment assignment) {
        entityManager.persist(assignment);
        return assignment;
    }

    @Override
    public void updateAssignment(Assignment assignment) {
        entityManager.merge(assignment);
    }

    @Override
    public List<Assignment> getAssignments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);

        root.fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<Assignment> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public boolean existsByLessonId(Integer lessonId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<Assignment> root = cq.from(Assignment.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(
                                root.get("lesson").get("id"),
                                lessonId
                        )
                );

        Long count = entityManager
                .createQuery(cq)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Assignment> getAssignmentsByCourse(Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("status"), Assignment.AssignmentStatus.ACTIVE)
                )
                .orderBy(cb.asc(root.get("name")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Assignment> getAllAssignments() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);

        root.fetch("course", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(
                        root.get("status"),
                        Assignment.AssignmentStatus.ACTIVE
                ))
                .orderBy(
                        cb.asc(root.get("course").get("name")),
                        cb.asc(root.get("name"))
                );

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countAssignments(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Assignment> root = cq.from(Assignment.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<Assignment> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String type = params.get("type");
        String status = params.get("status");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    value
            ));
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

        if (type != null && !type.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("type"),
                        Assignment.AssignmentType.valueOf(type)
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        Assignment.AssignmentStatus.valueOf(status)
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