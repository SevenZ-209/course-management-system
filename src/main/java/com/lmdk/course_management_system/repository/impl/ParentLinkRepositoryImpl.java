package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.repository.ParentLinkRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ParentLinkRepositoryImpl implements ParentLinkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${parent-links.page-size:10}")
    private int pageSize;

    @Override
    public ParentLink getParentLinkById(Integer id) {
        return entityManager.find(ParentLink.class, id);
    }

    @Override
    public ParentLink getParentLinkByIdForUpdate(Integer id) {
        return entityManager.find(ParentLink.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public ParentLink getParentLinkByCode(String verificationCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        root.fetch("parent", JoinType.LEFT);
        root.fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("verificationCode"), verificationCode));

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public ParentLink getParentLinkByCodeForUpdate(String verificationCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        cq.select(root)
                .where(cb.equal(root.get("verificationCode"), verificationCode));

        try {
            return entityManager.createQuery(cq)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public ParentLink getUnusedLinkByStudent(Integer studentId, LocalDateTime now) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.UNUSED),
                        cb.greaterThan(root.get("expiresAt"), now)
                )
                .orderBy(cb.desc(root.get("id")));

        List<ParentLink> links = entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return links.isEmpty() ? null : links.get(0);
    }

    @Override
    public ParentLink addParentLink(ParentLink parentLink) {
        entityManager.persist(parentLink);
        return parentLink;
    }

    @Override
    public void updateParentLink(ParentLink parentLink) {
        entityManager.merge(parentLink);
    }

    @Override
    public List<ParentLink> getParentLinks(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        root.fetch("parent", JoinType.LEFT);
        root.fetch("student", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<ParentLink> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<ParentLink> getParentLinksByParent(Integer parentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        root.fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("parent").get("id"), parentId),
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.USED)
                )
                .orderBy(cb.asc(root.get("student").get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<ParentLink> getParentLinksByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParentLink> cq = cb.createQuery(ParentLink.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        root.fetch("parent", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.USED)
                )
                .orderBy(cb.desc(root.get("id")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countParentLinks(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsByVerificationCode(String verificationCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        cq.select(cb.count(root))
                .where(cb.equal(root.get("verificationCode"), verificationCode));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsActiveLink(Integer parentId, Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("parent").get("id"), parentId),
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.USED)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsUnusedLinkByStudent(Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ParentLink> root = cq.from(ParentLink.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.UNUSED),
                        cb.greaterThan(root.get("expiresAt"), LocalDateTime.now())
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public int expireUnusedLinks(LocalDateTime now) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ParentLink> update = cb.createCriteriaUpdate(ParentLink.class);
        Root<ParentLink> root = update.from(ParentLink.class);

        update.set("status", ParentLink.ParentLinkStatus.EXPIRED)
                .where(
                        cb.equal(root.get("status"), ParentLink.ParentLinkStatus.UNUSED),
                        cb.lessThanOrEqualTo(root.get("expiresAt"), now)
                );

        return entityManager.createQuery(update).executeUpdate();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<ParentLink> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String parentId = params.get("parentId");
        String studentId = params.get("studentId");
        String status = params.get("status");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("verificationCode")), value),
                    cb.like(cb.lower(root.get("student").get("username")), value),
                    cb.like(cb.lower(root.get("student").get("fullName")), value),
                    cb.like(cb.lower(root.get("student").get("email")), value),
                    cb.like(cb.lower(root.get("parent").get("username")), value),
                    cb.like(cb.lower(root.get("parent").get("fullName")), value),
                    cb.like(cb.lower(root.get("parent").get("email")), value)
            ));
        }

        if (parentId != null && !parentId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("parent").get("id"),
                        Integer.parseInt(parentId)
                ));
            } catch (NumberFormatException ignored) {
            }
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

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        ParentLink.ParentLinkStatus.valueOf(status)
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