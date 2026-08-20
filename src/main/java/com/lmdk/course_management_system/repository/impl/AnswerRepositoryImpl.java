package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.repository.AnswerRepository;

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
public class AnswerRepositoryImpl implements AnswerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${answers.page-size:10}")
    private int pageSize;

    @Override
    public Answer getAnswerById(Integer id) {
        return entityManager.find(Answer.class, id);
    }

    @Override
    public Answer addAnswer(Answer answer) {
        entityManager.persist(answer);
        return answer;
    }

    @Override
    public void updateAnswer(Answer answer) {
        entityManager.merge(answer);
    }

    @Override
    public List<Answer> getAnswers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Answer> cq = cb.createQuery(Answer.class);
        Root<Answer> root = cq.from(Answer.class);

        Fetch<Answer, ?> question = root.fetch("question", JoinType.LEFT);
        question.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(
                        cb.asc(root.get("question").get("id")),
                        cb.asc(root.get("orderNumber"))
                );

        TypedQuery<Answer> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<Answer> getAnswersByQuestion(Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Answer> cq = cb.createQuery(Answer.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(root)
                .where(cb.equal(
                        root.get("question").get("id"),
                        questionId
                ))
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Answer> getCorrectAnswersByQuestion(Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Answer> cq = cb.createQuery(Answer.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("question").get("id"), questionId),
                        cb.isTrue(root.get("correct"))
                )
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countAnswers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Answer> root = cq.from(Answer.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countAnswersByQuestion(Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(cb.count(root))
                .where(cb.equal(
                        root.get("question").get("id"),
                        questionId
                ));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countCorrectAnswersByQuestion(Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("question").get("id"), questionId),
                        cb.isTrue(root.get("correct"))
                );

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsOrderNumber(Integer questionId, Integer orderNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("question").get("id"), questionId),
                        cb.equal(root.get("orderNumber"), orderNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsOrderNumberExceptId(Integer questionId,
                                             Integer orderNumber,
                                             Integer answerId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Answer> root = cq.from(Answer.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("question").get("id"), questionId),
                        cb.equal(root.get("orderNumber"), orderNumber),
                        cb.notEqual(root.get("id"), answerId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<Answer> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String assignmentId = params.get("assignmentId");
        String questionId = params.get("questionId");
        String type = params.get("type");
        String correct = params.get("correct");

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
                        root.get("question")
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
                        root.get("question")
                                .get("assignment")
                                .get("id"),
                        Integer.parseInt(assignmentId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (questionId != null && !questionId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("question").get("id"),
                        Integer.parseInt(questionId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (type != null && !type.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("type"),
                        Answer.AnswerType.valueOf(type)
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (correct != null && !correct.isBlank()) {
            if ("true".equalsIgnoreCase(correct))
                predicates.add(cb.isTrue(root.get("correct")));

            if ("false".equalsIgnoreCase(correct))
                predicates.add(cb.isFalse(root.get("correct")));
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