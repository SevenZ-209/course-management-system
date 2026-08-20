package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.pojo.StudentAnswer;
import com.lmdk.course_management_system.repository.StudentAnswerRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
public class StudentAnswerRepositoryImpl implements StudentAnswerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${student-answers.page-size:10}")
    private int pageSize;

    @Override
    public StudentAnswer getStudentAnswerById(Integer id) {
        return entityManager.find(StudentAnswer.class, id);
    }

    @Override
    public StudentAnswer getStudentAnswer(Integer attemptId, Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentAnswer> cq = cb.createQuery(StudentAnswer.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        root.fetch("question", JoinType.LEFT);
        root.fetch("selectedAnswer", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("assignmentAttempt").get("id"), attemptId),
                        cb.equal(root.get("question").get("id"), questionId)
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public StudentAnswer addStudentAnswer(StudentAnswer studentAnswer) {
        entityManager.persist(studentAnswer);
        return studentAnswer;
    }

    @Override
    public void updateStudentAnswer(StudentAnswer studentAnswer) {
        entityManager.merge(studentAnswer);
    }

    @Override
    public List<StudentAnswer> getStudentAnswers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentAnswer> cq = cb.createQuery(StudentAnswer.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        Fetch<StudentAnswer, ?> attempt = root.fetch("assignmentAttempt", JoinType.LEFT);
        Fetch<?, ?> assigned = attempt.fetch("assignedAssignment", JoinType.LEFT);

        assigned.fetch("student", JoinType.LEFT);
        assigned.fetch("assignment", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        root.fetch("question", JoinType.LEFT);
        root.fetch("selectedAnswer", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(
                        cb.desc(root.get("assignmentAttempt").get("id")),
                        cb.asc(root.get("question").get("orderNumber"))
                );

        TypedQuery<StudentAnswer> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<StudentAnswer> getStudentAnswersByAttempt(Integer attemptId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentAnswer> cq = cb.createQuery(StudentAnswer.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        root.fetch("question", JoinType.LEFT);
        root.fetch("selectedAnswer", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        attemptId
                ))
                .orderBy(cb.asc(
                        root.get("question").get("orderNumber")
                ));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countStudentAnswers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public long countStudentAnswersByAttempt(Integer attemptId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        cq.select(cb.count(root))
                .where(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        attemptId
                ));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsStudentAnswer(Integer attemptId, Integer questionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(
                                root.get("assignmentAttempt").get("id"),
                                attemptId
                        ),
                        cb.equal(
                                root.get("question").get("id"),
                                questionId
                        )
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public BigDecimal sumScoreByAttempt(Integer attemptId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<StudentAnswer> root = cq.from(StudentAnswer.class);

        cq.select(cb.coalesce(
                        cb.sum(root.<BigDecimal>get("score")),
                        BigDecimal.ZERO
                ))
                .where(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        attemptId
                ));

        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             Root<StudentAnswer> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String attemptId = params.get("attemptId");
        String studentId = params.get("studentId");
        String courseId = params.get("courseId");
        String assignmentId = params.get("assignmentId");
        String questionId = params.get("questionId");
        String questionType = params.get("questionType");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("student")
                                    .get("username")
                    ), value),
                    cb.like(cb.lower(
                            root.get("assignmentAttempt")
                                    .get("assignedAssignment")
                                    .get("student")
                                    .get("fullName")
                    ), value),
                    cb.like(cb.lower(
                            root.get("question").get("content")
                    ), value),
                    cb.like(cb.lower(
                            root.get("answerContent")
                    ), value)
            ));
        }

        if (attemptId != null && !attemptId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignmentAttempt").get("id"),
                        Integer.parseInt(attemptId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
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
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
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
                        root.get("assignmentAttempt")
                                .get("assignedAssignment")
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

        if (questionType != null && !questionType.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("question").get("type"),
                        Question.QuestionType.valueOf(questionType)
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