package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.repository.QuestionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class QuestionRepositoryImpl implements QuestionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${questions.page-size:10}")
    private int pageSize;

    @Override
    public Question getQuestionById(Integer id) {
        return entityManager.find(Question.class, id);
    }

    @Override
    public Question addQuestion(Question question) {
        entityManager.persist(question);
        return question;
    }

    @Override
    public void updateQuestion(Question question) {
        entityManager.merge(question);
    }

    @Override
    public List<Question> getQuestions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Question> cq = cb.createQuery(Question.class);
        Root<Question> root = cq.from(Question.class);

        root.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(
                        cb.desc(root.get("id"))
                );

        TypedQuery<Question> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<Question> getQuestionsByAssignment(Integer assignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Question> cq = cb.createQuery(Question.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(root)
                .where(cb.equal(
                        root.get("assignment").get("id"),
                        assignmentId
                ))
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countQuestions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Question> root = cq.from(Question.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsOrderNumber(Integer assignmentId, Integer orderNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("assignment").get("id"), assignmentId),
                        cb.equal(root.get("orderNumber"), orderNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsOrderNumberExceptId(Integer assignmentId,
                                             Integer orderNumber,
                                             Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("assignment").get("id"), assignmentId),
                        cb.equal(root.get("orderNumber"), orderNumber),
                        cb.notEqual(root.get("id"), questionId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public BigDecimal sumScoresByAssignment(Integer assignmentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(cb.coalesce(
                        cb.sum(root.<BigDecimal>get("score")),
                        BigDecimal.ZERO
                ))
                .where(cb.equal(
                        root.get("assignment").get("id"),
                        assignmentId
                ));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public BigDecimal sumScoresByAssignmentExceptQuestion(
            Integer assignmentId,
            Integer questionId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Question> root = cq.from(Question.class);

        cq.select(cb.coalesce(
                        cb.sum(root.<BigDecimal>get("score")),
                        BigDecimal.ZERO
                ))
                .where(
                        cb.equal(
                                root.get("assignment").get("id"),
                                assignmentId
                        ),
                        cb.notEqual(root.get("id"), questionId)
                );

        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<Question> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String assignmentId = params.get("assignmentId");
        String type = params.get("type");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.like(
                    cb.lower(root.get("content")),
                    value
            ));
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignment")
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
                        root.get("assignment").get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (type != null && !type.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("type"),
                        Question.QuestionType.valueOf(type)
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