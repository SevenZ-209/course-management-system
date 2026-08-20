package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.repository.LearningPathRepository;

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
public class LearningPathRepositoryImpl implements LearningPathRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${learning-paths.page-size:10}")
    private int pageSize;

    @Override
    public LearningPath getLearningPathById(Integer id) {
        return entityManager.find(LearningPath.class, id);
    }

    @Override
    public LearningPath addLearningPath(LearningPath learningPath) {
        entityManager.persist(learningPath);
        return learningPath;
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        entityManager.merge(learningPath);
    }

    @Override
    public List<LearningPath> getLearningPaths(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPath> cq = cb.createQuery(LearningPath.class);
        Root<LearningPath> root = cq.from(LearningPath.class);

        root.fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<LearningPath> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<LearningPath> getLearningPathsByCourse(Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPath> cq = cb.createQuery(LearningPath.class);
        Root<LearningPath> root = cq.from(LearningPath.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("status"), LearningPath.LearningPathStatus.ACTIVE)
                )
                .orderBy(cb.asc(root.get("name")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<LearningPath> getAllLearningPaths() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPath> cq = cb.createQuery(LearningPath.class);
        Root<LearningPath> root = cq.from(LearningPath.class);

        root.fetch("course", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(
                        root.get("status"),
                        LearningPath.LearningPathStatus.ACTIVE
                ))
                .orderBy(
                        cb.asc(root.get("course").get("name")),
                        cb.asc(root.get("name"))
                );

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countLearningPaths(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPath> root = cq.from(LearningPath.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<LearningPath> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
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

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        LearningPath.LearningPathStatus.valueOf(status)
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