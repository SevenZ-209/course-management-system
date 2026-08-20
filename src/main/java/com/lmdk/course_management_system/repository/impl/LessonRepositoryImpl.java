package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.repository.LessonRepository;

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
public class LessonRepositoryImpl implements LessonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${lessons.page-size:10}")
    private int pageSize;

    @Override
    public Lesson getLessonById(Integer id) {
        return entityManager.find(Lesson.class, id);
    }

    @Override
    public Lesson addLesson(Lesson lesson) {
        entityManager.persist(lesson);
        return lesson;
    }

    @Override
    public void updateLesson(Lesson lesson) {
        entityManager.merge(lesson);
    }

    @Override
    public List<Lesson> getLessons(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Lesson> cq = cb.createQuery(Lesson.class);
        Root<Lesson> root = cq.from(Lesson.class);

        root.fetch("courseModule", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<Lesson> query = entityManager.createQuery(cq);

        int page = parsePage(params.get("page"));
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<Lesson> getLessonsByModule(Integer moduleId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Lesson> cq = cb.createQuery(Lesson.class);
        Root<Lesson> root = cq.from(Lesson.class);

        cq.select(root)
                .where(cb.equal(root.get("courseModule").get("id"), moduleId))
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countLessons(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Lesson> root = cq.from(Lesson.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsOrderNumber(Integer moduleId, Integer orderNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Lesson> root = cq.from(Lesson.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("courseModule").get("id"), moduleId),
                        cb.equal(root.get("orderNumber"), orderNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsOrderNumberExceptId(Integer moduleId, Integer orderNumber, Integer lessonId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Lesson> root = cq.from(Lesson.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("courseModule").get("id"), moduleId),
                        cb.equal(root.get("orderNumber"), orderNumber),
                        cb.notEqual(root.get("id"), lessonId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<Lesson> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String moduleId = params.get("moduleId");

        if (kw != null && !kw.isBlank()) {
            String value =
                    "%" + kw.trim().toLowerCase() + "%";

            predicates.add(
                    cb.like(
                            cb.lower(root.get("name")),
                            value
                    )
            );
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseModule").get("course").get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (moduleId != null && !moduleId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseModule").get("id"),
                        Integer.parseInt(moduleId)
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