package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.repository.AttendanceRepository;

import jakarta.persistence.EntityManager;
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
public class AttendanceRepositoryImpl implements AttendanceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${attendances.page-size:10}")
    private int pageSize;

    @Override
    public Attendance getAttendanceById(Integer id) {
        return entityManager.find(Attendance.class, id);
    }

    @Override
    public Attendance getAttendance(Integer sessionId, Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Attendance> cq = cb.createQuery(Attendance.class);
        Root<Attendance> root = cq.from(Attendance.class);

        cq.select(root)
                .where(
                        cb.equal(root.get("onlineSession").get("id"), sessionId),
                        cb.equal(root.get("student").get("id"), studentId)
                );

        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Attendance addAttendance(Attendance attendance) {
        entityManager.persist(attendance);
        return attendance;
    }

    @Override
    public void updateAttendance(Attendance attendance) {
        entityManager.merge(attendance);
    }

    @Override
    public List<Attendance> saveAttendances(List<Attendance> attendances) {
        if (attendances == null || attendances.isEmpty()) return List.of();

        List<Attendance> saved = new ArrayList<>();
        for (Attendance attendance : attendances) {
            if (attendance.getId() == null) {
                entityManager.persist(attendance);
                saved.add(attendance);
            } else {
                saved.add(entityManager.merge(attendance));
            }
        }

        entityManager.flush();
        return saved;
    }

    @Override
    public List<Attendance> getAttendances(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Attendance> cq = cb.createQuery(Attendance.class);
        Root<Attendance> root = cq.from(Attendance.class);

        root.fetch("onlineSession", JoinType.LEFT)
                .fetch("courseClass", JoinType.LEFT);

        root.fetch("student", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("id")));

        TypedQuery<Attendance> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<Attendance> getAttendancesBySession(Integer sessionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Attendance> cq = cb.createQuery(Attendance.class);
        Root<Attendance> root = cq.from(Attendance.class);

        root.fetch("student", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(
                        root.get("onlineSession").get("id"),
                        sessionId
                ))
                .orderBy(cb.asc(root.get("student").get("fullName")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Attendance> getAttendancesByStudentAndSessionIds(Integer studentId, List<Integer> sessionIds) {
        if (studentId == null || sessionIds == null || sessionIds.isEmpty())
            return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Attendance> cq = cb.createQuery(Attendance.class);
        Root<Attendance> root = cq.from(Attendance.class);

        root.fetch("onlineSession", JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.equal(root.get("student").get("id"), studentId),
                        root.get("onlineSession").get("id").in(sessionIds)
                );

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countAttendances(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Attendance> root = cq.from(Attendance.class);

        List<Predicate> predicates = createPredicates(cb, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsAttendance(Integer sessionId, Integer studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Attendance> root = cq.from(Attendance.class);

        cq.select(cb.count(root))
                .where(
                        cb.equal(root.get("onlineSession").get("id"), sessionId),
                        cb.equal(root.get("student").get("id"), studentId)
                );

        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<Attendance> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String sessionId = params.get("sessionId");
        String classId = params.get("classId");
        String present = params.get("present");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("student").get("username")), value),
                    cb.like(cb.lower(root.get("student").get("fullName")), value),
                    cb.like(cb.lower(root.get("student").get("email")), value)
            ));
        }

        if (sessionId != null && !sessionId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("onlineSession").get("id"),
                        Integer.parseInt(sessionId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (classId != null && !classId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("onlineSession")
                                .get("courseClass")
                                .get("id"),
                        Integer.parseInt(classId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (present != null && !present.isBlank()) {
            if ("true".equalsIgnoreCase(present))
                predicates.add(cb.isTrue(root.get("present")));

            if ("false".equalsIgnoreCase(present))
                predicates.add(cb.isFalse(root.get("present")));
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