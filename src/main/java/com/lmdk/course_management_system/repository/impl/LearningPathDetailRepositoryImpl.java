package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.repository.LearningPathDetailRepository;

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
public class LearningPathDetailRepositoryImpl implements LearningPathDetailRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${learning-path-details.page-size:10}")
    private int pageSize;

    @Override
    public LearningPathDetail getDetailById(Integer id) {
        return entityManager.find(LearningPathDetail.class, id);
    }

    @Override
    public LearningPathDetail addDetail(LearningPathDetail detail) {
        entityManager.persist(detail);
        return detail;
    }

    @Override
    public void updateDetail(LearningPathDetail detail) {
        entityManager.merge(detail);
    }

    @Override
    public List<LearningPathDetail> getDetails(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPathDetail> cq =
                cb.createQuery(LearningPathDetail.class);
        Root<LearningPathDetail> root =
                cq.from(LearningPathDetail.class);

        root.fetch("learningPath", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("assignment", JoinType.LEFT);

        List<Predicate> predicates =
                createPredicates(cb, root, params);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(
                        cb.asc(
                                root.get("learningPath").get("name")
                        ),
                        cb.asc(root.get("orderNumber"))
                );

        TypedQuery<LearningPathDetail> query =
                entityManager.createQuery(cq);

        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<LearningPathDetail> getDetailsByLearningPath(Integer learningPathId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPathDetail> cq = cb.createQuery(LearningPathDetail.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        root.fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(
                        root.get("learningPath").get("id"),
                        learningPathId
                ))
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<LearningPathDetail> getDetailsByLearningPaths(List<Integer> learningPathIds) {
        if(learningPathIds == null || learningPathIds.isEmpty()) return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LearningPathDetail> cq = cb.createQuery(LearningPathDetail.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        root.fetch("learningPath", JoinType.LEFT);
        root.fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .distinct(true)
                .where(root.get("learningPath").get("id").in(learningPathIds))
                .orderBy(
                        cb.asc(root.get("learningPath").get("id")),
                        cb.asc(root.get("orderNumber"))
                );

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countDetails(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsOrderNumber(Integer learningPathId, Integer orderNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("learningPath").get("id"), learningPathId),
                        cb.equal(root.get("orderNumber"), orderNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsOrderNumberExceptId(Integer learningPathId,
                                             Integer orderNumber,
                                             Integer detailId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("learningPath").get("id"), learningPathId),
                        cb.equal(root.get("orderNumber"), orderNumber),
                        cb.notEqual(root.get("id"), detailId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsAssignment(Integer learningPathId, Integer assignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("learningPath").get("id"), learningPathId),
                        cb.equal(root.get("assignment").get("id"), assignmentId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsAssignmentExceptId(Integer learningPathId,
                                            Integer assignmentId,
                                            Integer detailId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LearningPathDetail> root = cq.from(LearningPathDetail.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("learningPath").get("id"), learningPathId),
                        cb.equal(root.get("assignment").get("id"), assignmentId),
                        cb.notEqual(root.get("id"), detailId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public LearningPathDetail getNextDetail(Integer learningPathId, Integer currentOrderNumber) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<LearningPathDetail> cq =
                cb.createQuery(LearningPathDetail.class);

        Root<LearningPathDetail> root =
                cq.from(LearningPathDetail.class);

        root.fetch("assignment", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(
                                root.get("learningPath").get("id"),
                                learningPathId
                        ),
                        cb.greaterThan(
                                root.get("orderNumber"),
                                currentOrderNumber
                        )
                )
                .orderBy(
                        cb.asc(root.get("orderNumber"))
                );

        List<LearningPathDetail> result =
                entityManager.createQuery(cq)
                        .setMaxResults(1)
                        .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<LearningPathDetail> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String learningPathId = params.get("learningPathId");
        String assignmentId = params.get("assignmentId");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("learningPath").get("name")), value),
                    cb.like(cb.lower(root.get("assignment").get("name")), value)
            ));
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

        if (assignmentId != null && !assignmentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignment").get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
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