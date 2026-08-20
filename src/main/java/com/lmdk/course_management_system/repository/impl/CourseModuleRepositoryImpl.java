package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.repository.CourseModuleRepository;

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
public class CourseModuleRepositoryImpl implements CourseModuleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${modules.page-size:10}")
    private int pageSize;

    @Override
    public CourseModule getModuleById(Integer id) {
        return entityManager.find(CourseModule.class, id);
    }

    @Override
    public CourseModule addModule(CourseModule module) {
        entityManager.persist(module);
        return module;
    }

    @Override
    public void updateModule(CourseModule module) {
        entityManager.merge(module);
    }

    @Override
    public List<CourseModule> getModules(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseModule> cq = cb.createQuery(CourseModule.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        root.fetch("course", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<CourseModule> query = entityManager.createQuery(cq);

        int page = parsePage(params.get("page"));
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<CourseModule> getModulesByCourse(Integer courseId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseModule> cq = cb.createQuery(CourseModule.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("status"), CourseModule.ModuleStatus.ACTIVE)
                )
                .orderBy(cb.asc(root.get("orderNumber")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countModules(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsOrderNumber(Integer courseId, Integer orderNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("orderNumber"), orderNumber)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsOrderNumberExceptId(Integer courseId, Integer orderNumber, Integer moduleId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("orderNumber"), orderNumber),
                        cb.notEqual(root.get("id"), moduleId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<CourseModule> getAllModules() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CourseModule> cq = cb.createQuery(CourseModule.class);
        Root<CourseModule> root = cq.from(CourseModule.class);

        root.fetch("course", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("status"), CourseModule.ModuleStatus.ACTIVE))
                .orderBy(
                        cb.asc(root.get("course").get("name")),
                        cb.asc(root.get("orderNumber"))
                );

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<CourseModule> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
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

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        CourseModule.ModuleStatus.valueOf(status)
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