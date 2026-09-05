package com.lmdk.course_management_system.repository.impl;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.OnlineSessionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class OnlineSessionRepositoryImpl implements OnlineSessionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${online-sessions.page-size:10}")
    private int pageSize;

    @Override
    public OnlineSession getSessionById(Integer id) {
        return entityManager.find(OnlineSession.class, id);
    }

    @Override
    public OnlineSession addSession(OnlineSession onlineSession) {
        entityManager.persist(onlineSession);
        return onlineSession;
    }

    @Override
    public void updateSession(OnlineSession onlineSession) {
        entityManager.merge(onlineSession);
    }

    @Override
    public void lockScheduleResources(Integer classId, Integer teacherId) {
        if (classId != null)
            entityManager.find(CourseClass.class, classId, LockModeType.PESSIMISTIC_WRITE);
        if (teacherId != null)
            entityManager.find(User.class, teacherId, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public boolean existsClassScheduleConflict(Integer classId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeSessionId) {
        if (classId == null || startTime == null || endTime == null)
            return false;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("courseClass").get("id"), classId));
        predicates.add(cb.lessThan(root.get("startTime"), endTime));
        predicates.add(cb.greaterThan(root.get("endTime"), startTime));
        predicates.add(cb.notEqual(root.get("courseClass").get("status"),
                CourseClass.ClassStatus.CANCELED));

        if (excludeSessionId != null)
            predicates.add(cb.notEqual(root.get("id"), excludeSessionId));

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public boolean existsTeacherScheduleConflict(Integer teacherId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeSessionId) {
        if (teacherId == null || startTime == null || endTime == null)
            return false;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("teacher").get("id"), teacherId));
        predicates.add(cb.lessThan(root.get("startTime"), endTime));
        predicates.add(cb.greaterThan(root.get("endTime"), startTime));
        predicates.add(cb.notEqual(root.get("courseClass").get("status"),
                CourseClass.ClassStatus.CANCELED));

        if (excludeSessionId != null)
            predicates.add(cb.notEqual(root.get("id"), excludeSessionId));

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getSingleResult() > 0;
    }

    @Override
    public List<OnlineSession> getSessions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OnlineSession> cq = cb.createQuery(OnlineSession.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);

        root.fetch("courseClass", JoinType.LEFT)
                .fetch("course", JoinType.LEFT);
        root.fetch("teacher", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(cb, cq, root, params);

        cq.select(root)
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]));

        if ("asc".equalsIgnoreCase(params.get("sort")))
            cq.orderBy(cb.asc(root.get("startTime")));
        else
            cq.orderBy(cb.desc(root.get("id")));

        TypedQuery<OnlineSession> query = entityManager.createQuery(cq);
        int page = parsePage(params.get("page"));

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public List<OnlineSession> getSessionsByClass(Integer classId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OnlineSession> cq = cb.createQuery(OnlineSession.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);

        root.fetch("teacher", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("courseClass").get("id"), classId))
                .orderBy(cb.asc(root.get("startTime")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<OnlineSession> getEndedSessionsByStudent(Integer studentId) {
        if (studentId == null)
            return List.of();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OnlineSession> cq = cb.createQuery(OnlineSession.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);

        Subquery<Integer> subquery = cq.subquery(Integer.class);
        Root<Enrollment> enrollment = subquery.from(Enrollment.class);

        subquery.select(cb.literal(1))
                .where(
                        cb.equal(enrollment.get("student").get("id"), studentId),
                        cb.equal(enrollment.get("status"), Enrollment.EnrollmentStatus.ACTIVE),
                        cb.equal(
                                enrollment.get("courseClass").get("id"),
                                root.get("courseClass").get("id")
                        )
                );

        cq.select(root)
                .where(
                        cb.exists(subquery),
                        cb.isNotNull(root.get("endTime")),
                        cb.lessThan(root.get("endTime"), LocalDateTime.now())
                )
                .orderBy(cb.asc(root.get("startTime")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public long countSessions(Map<String, String> params) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);

        List<Predicate> predicates = createPredicates(cb, cq, root, params);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public List<OnlineSession> getAllSessions() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OnlineSession> cq = cb.createQuery(OnlineSession.class);
        Root<OnlineSession> root = cq.from(OnlineSession.class);

        root.fetch("courseClass", JoinType.LEFT);

        cq.select(root)
                .orderBy(cb.desc(root.get("startTime")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder cb,
                                             CriteriaQuery<?> query,
                                             Root<OnlineSession> root,
                                             Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();

        String kw = params.get("kw");
        String courseId = params.get("courseId");
        String classId = params.get("classId");
        String teacherId = params.get("teacherId");
        String studentId = params.get("studentId");
        String date = params.get("date");
        String from = params.get("from");
        String to = params.get("to");
        String includeEnded = params.get("includeEnded");

        if (kw != null && !kw.isBlank()) {
            String value = "%" + kw.trim().toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), value),
                    cb.like(cb.lower(root.get("meetingUrl")), value)
            ));
        }

        if (courseId != null && !courseId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseClass").get("course").get("id"),
                        Integer.parseInt(courseId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (classId != null && !classId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("courseClass").get("id"),
                        Integer.parseInt(classId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (teacherId != null && !teacherId.isBlank()) {
            try {
                predicates.add(cb.equal(
                        root.get("teacher").get("id"),
                        Integer.parseInt(teacherId)
                ));
            } catch (NumberFormatException ignored) {
            }
        }

        if (studentId != null && !studentId.isBlank()) {
            try {
                Integer parsedStudentId = Integer.parseInt(studentId);

                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<Enrollment> enrollment = subquery.from(Enrollment.class);

                subquery.select(cb.literal(1))
                        .where(
                                cb.equal(enrollment.get("student").get("id"), parsedStudentId),
                                cb.equal(enrollment.get("status"), Enrollment.EnrollmentStatus.ACTIVE),
                                cb.equal(
                                        enrollment.get("courseClass").get("id"),
                                        root.get("courseClass").get("id")
                                )
                        );

                predicates.add(cb.exists(subquery));
            } catch (NumberFormatException ignored) {
            }
        }

        if (date != null && !date.isBlank()) {
            try {
                LocalDate selectedDate = LocalDate.parse(date);
                LocalDateTime start = selectedDate.atStartOfDay();
                LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

                predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("startTime"), start),
                        cb.lessThan(root.get("startTime"), end)
                ));
            } catch (Exception ignored) {
            }
        }

        if (from != null && !from.isBlank()) {
            try {
                LocalDate selectedFrom = LocalDate.parse(from);
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("startTime"),
                        selectedFrom.atStartOfDay()
                ));
            } catch (Exception ignored) {
            }
        }

        if (to != null && !to.isBlank()) {
            try {
                LocalDate selectedTo = LocalDate.parse(to);
                predicates.add(cb.lessThan(
                        root.get("startTime"),
                        selectedTo.plusDays(1).atStartOfDay()
                ));
            } catch (Exception ignored) {
            }
        }

        if ("false".equalsIgnoreCase(includeEnded)) {
            LocalDateTime now = LocalDateTime.now();

            predicates.add(cb.or(
                    cb.isNull(root.get("endTime")),
                    cb.greaterThanOrEqualTo(root.get("endTime"), now)
            ));
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