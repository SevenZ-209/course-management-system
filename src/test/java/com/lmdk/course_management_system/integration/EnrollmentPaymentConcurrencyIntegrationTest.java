package com.lmdk.course_management_system.integration;

import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration-test")
class EnrollmentPaymentConcurrencyIntegrationTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private PaymentTransactionService paymentTransactionService;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void sameStudent_sameCourse_twoClasses_concurrentlyOnlyOneEnrollmentWins() throws Exception {
        Seed seed = seed("same-course", 30, 1);

        ConcurrentResult result = runConcurrently(
                () -> enrollmentService.addEnrollment(newEnrollment(seed.student1Id(), seed.class1Id())),
                () -> enrollmentService.addEnrollment(newEnrollment(seed.student1Id(), seed.class2Id()))
        );

        assertEquals(1, result.successes(), diagnostic(result));
        assertEquals(1, result.failures().size(), diagnostic(result));

        long blockingEnrollments = readLong("""
                select count(e)
                from Enrollment e
                where e.student.id = :studentId
                  and e.courseClass.course.id = :courseId
                  and e.status in (
                      com.lmdk.course_management_system.pojo.Enrollment$EnrollmentStatus.PENDING_PAYMENT,
                      com.lmdk.course_management_system.pojo.Enrollment$EnrollmentStatus.ACTIVE
                  )
                """, "studentId", seed.student1Id(), "courseId", seed.courseId());

        assertEquals(1L, blockingEnrollments);
    }

    @Test
    void twoStudents_lastClassSlot_concurrentlyOnlyOneEnrollmentWins() throws Exception {
        Seed seed = seed("last-slot", 1, 2);

        ConcurrentResult result = runConcurrently(
                () -> enrollmentService.addEnrollment(newEnrollment(seed.student1Id(), seed.class1Id())),
                () -> enrollmentService.addEnrollment(newEnrollment(seed.student2Id(), seed.class1Id()))
        );

        assertEquals(1, result.successes(), diagnostic(result));
        assertEquals(1, result.failures().size(), diagnostic(result));

        long occupied = readLong("""
                select count(e)
                from Enrollment e
                where e.courseClass.id = :classId
                  and e.status <> com.lmdk.course_management_system.pojo.Enrollment$EnrollmentStatus.CANCELED
                """, "classId", seed.class1Id());

        assertEquals(1L, occupied);
    }

    @Test
    void sameEnrollment_twoSuccessfulPayments_concurrentlyOnlyOnePaymentWins() throws Exception {
        Seed seed = seed("double-payment", 30, 1);
        Integer enrollmentId = seedPendingEnrollment(seed.student1Id(), seed.class1Id());

        ConcurrentResult result = runConcurrently(
                () -> paymentTransactionService.createAutoSuccess(
                        payment(enrollmentId, seed.tuitionFee(), "BANK")
                ),
                () -> paymentTransactionService.createAutoSuccess(
                        payment(enrollmentId, seed.tuitionFee(), "BANK")
                )
        );

        assertEquals(1, result.successes(), diagnostic(result));
        assertEquals(1, result.failures().size(), diagnostic(result));

        String enrollmentStatus = inTransaction(() ->
                entityManager.createQuery(
                                "select e.status from Enrollment e where e.id = :id",
                                Enrollment.EnrollmentStatus.class
                        )
                        .setParameter("id", enrollmentId)
                        .getSingleResult()
                        .name()
        );

        long successfulPayments = readLong("""
                select count(p)
                from PaymentTransaction p
                where p.enrollment.id = :enrollmentId
                  and p.status = com.lmdk.course_management_system.pojo.PaymentTransaction$TransactionStatus.SUCCESS
                """, "enrollmentId", enrollmentId);

        long totalPayments = readLong("""
                select count(p)
                from PaymentTransaction p
                where p.enrollment.id = :enrollmentId
                """, "enrollmentId", enrollmentId);

        assertEquals("ACTIVE", enrollmentStatus);
        assertEquals(1L, successfulPayments);
        assertEquals(1L, totalPayments);
    }

    private Seed seed(String key, int maxStudents, int studentCount) {
        return inTransaction(() -> {
            Category category = new Category();
            category.setName("Integration Category " + key);
            category.setStatus(Category.CategoryStatus.ACTIVE);
            entityManager.persist(category);

            Course course = new Course();
            course.setCategory(category);
            course.setName("Integration Course " + key);
            course.setTuitionFee(new BigDecimal("1500000.00"));
            course.setStatus(Course.CourseStatus.ACTIVE);
            entityManager.persist(course);

            User student1 = student("student-" + key + "-1");
            entityManager.persist(student1);

            User student2 = null;
            if (studentCount >= 2) {
                student2 = student("student-" + key + "-2");
                entityManager.persist(student2);
            }

            CourseClass class1 = courseClass(course, "Class A " + key, maxStudents);
            CourseClass class2 = courseClass(course, "Class B " + key, maxStudents);
            entityManager.persist(class1);
            entityManager.persist(class2);

            entityManager.flush();

            return new Seed(
                    course.getId(),
                    class1.getId(),
                    class2.getId(),
                    student1.getId(),
                    student2 == null ? null : student2.getId(),
                    course.getTuitionFee()
            );
        });
    }

    private Integer seedPendingEnrollment(Integer studentId, Integer classId) {
        return inTransaction(() -> {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(entityManager.find(User.class, studentId));
            enrollment.setCourseClass(entityManager.find(CourseClass.class, classId));
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
            entityManager.persist(enrollment);
            entityManager.flush();
            return enrollment.getId();
        });
    }

    private Enrollment newEnrollment(Integer studentId, Integer classId) {
        User student = new User();
        student.setId(studentId);

        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        return enrollment;
    }

    private PaymentTransaction payment(Integer enrollmentId, BigDecimal amount, String method) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentId);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setEnrollment(enrollment);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(method);
        return transaction;
    }

    private User student(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFullName("Integration " + username);
        user.setEmail(username + "@test.local");
        user.setPasswordHash("test-password-hash");
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private CourseClass courseClass(Course course, String name, int maxStudents) {
        CourseClass courseClass = new CourseClass();
        courseClass.setName(name);
        courseClass.setCourse(course);
        courseClass.setMaxStudents(maxStudents);
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);
        return courseClass;
    }

    private ConcurrentResult runConcurrently(ThrowingRunnable first, ThrowingRunnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Throwable> f1 = executor.submit(concurrentTask(first, ready, start));
            Future<Throwable> f2 = executor.submit(concurrentTask(second, ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Hai thread không sẵn sàng kịp thời");
            start.countDown();

            List<Throwable> failures = new ArrayList<>();
            Throwable t1 = f1.get(15, TimeUnit.SECONDS);
            Throwable t2 = f2.get(15, TimeUnit.SECONDS);

            if (t1 != null) failures.add(t1);
            if (t2 != null) failures.add(t2);

            return new ConcurrentResult(2 - failures.size(), failures);
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Callable<Throwable> concurrentTask(
            ThrowingRunnable task,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            try {
                if (!start.await(5, TimeUnit.SECONDS))
                    return new TimeoutException("Không nhận được tín hiệu start");

                task.run();
                return null;
            } catch (Throwable ex) {
                return ex;
            }
        };
    }

    private long readLong(String jpql, Object... params) {
        return inTransaction(() -> {
            var query = entityManager.createQuery(jpql, Long.class);
            for (int i = 0; i < params.length; i += 2)
                query.setParameter((String) params[i], params[i + 1]);
            return query.getSingleResult();
        });
    }

    private <T> T inTransaction(Callable<T> work) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> {
            try {
                return work.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private String diagnostic(ConcurrentResult result) {
        if (result.failures().isEmpty())
            return "Không có exception";
        return result.failures().stream()
                .map(t -> t.getClass().getSimpleName() + ": " + t.getMessage())
                .toList()
                .toString();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record ConcurrentResult(int successes, List<Throwable> failures) {}

    private record Seed(
            Integer courseId,
            Integer class1Id,
            Integer class2Id,
            Integer student1Id,
            Integer student2Id,
            BigDecimal tuitionFee
    ) {}
}
