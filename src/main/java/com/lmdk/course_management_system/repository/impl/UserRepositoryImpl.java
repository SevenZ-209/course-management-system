package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
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
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${users.page-size:10}")
    private int pageSize;

    @Override
    public User getUserById(Integer id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User getUserByIdForUpdate(Integer id) {
        return entityManager.find(User.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public User getUserByUsername(String username) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        cq.select(root).where(cb.equal(root.get("username"), username));

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        cq.select(root).where(cb.equal(root.get("email"), email));

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public User addUser(User user) {
        entityManager.persist(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        entityManager.merge(user);
    }

    @Override
    public void deleteUser(Integer id) {
        User user = entityManager.find(User.class, id);

        if (user != null)
            entityManager.remove(user);
    }

    @Override
    public List<User> getUsers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<User> query = entityManager.createQuery(cq);

        int page = parsePage(params.get("page"));
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<User> getAllUsers() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        cq.select(root)
                .where(cb.equal(root.get("status"), User.UserStatus.ACTIVE))
                .orderBy(cb.asc(root.get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countUsers(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsByUsername(String username) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.count(root))
                .where(cb.equal(root.get("username"), username));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.count(root))
                .where(cb.equal(root.get("email"), email));

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<User> getUsersByRole(User.UserRole role) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("role"), role),
                        cb.equal(root.get("status"), User.UserStatus.ACTIVE)
                )
                .orderBy(cb.asc(root.get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<User> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String role = params.get("role");
        String status = params.get("status");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), value),
                    cb.like(cb.lower(root.get("fullName")), value),
                    cb.like(cb.lower(root.get("email")), value)
            ));
        }

        if (role != null && !role.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("role"),
                        User.UserRole.valueOf(role)
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("status"),
                        User.UserStatus.valueOf(status)
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