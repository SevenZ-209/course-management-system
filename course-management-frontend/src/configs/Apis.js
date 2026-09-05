import axios from "axios";
import cookies from "react-cookies";

const BASE_URL = "http://localhost:8080/api";

export const endpoints = {
    login: "/auth/login",
    profile: "/auth/profile",
    register: "/auth/register",

    publicCourses: "/courses",
    publicCourseClasses: courseId => `/courses/${courseId}/classes`,
    publicCategories: "/categories",

    adminUsers: "/admin/users",
    adminTeacherOptions: "/admin/users/teacher-options",
    adminStudentOptions: "/admin/users/student-options",
    adminParentOptions: "/admin/users/parent-options",

    adminCategories: "/admin/categories",
    adminCategoryOptions: "/admin/categories/options",

    adminCourses: "/admin/courses",
    adminCourseOptions: "/admin/courses/options",

    adminCourseModules: "/admin/course-modules",
    adminCourseModuleOptions: "/admin/course-modules/options",

    adminLessons: "/admin/lessons",
    adminLessonOptions: "/admin/lessons/options",

    adminClasses: "/admin/classes",
    adminClassOptions: "/admin/classes/options",

    adminOnlineSessions: "/admin/online-sessions",
    adminOnlineSessionOptions: "/admin/online-sessions/options",

    adminAttendances: "/admin/attendances",

    adminEnrollments: "/admin/enrollments",
    adminPendingEnrollmentOptions: "/admin/enrollments/pending-options",

    adminPayments: "/admin/payment-transactions",
    adminParentLinks: "/admin/parent-links",
    adminReports: "/admin/reports",

    adminLearningPaths: "/admin/learning-paths",
    adminLearningPathOptions: "/admin/learning-paths/options",

    adminLearningPathDetails: "/admin/learning-path-details",
    adminLearningPathDetailAssignments: "/admin/learning-path-details/assignments",

    adminStudentLearningPaths: "/admin/student-learning-paths",
    adminAvailableLearningPaths: "/admin/student-learning-paths/available-paths",
    adminInProgressStudentLearningPaths: "/admin/student-learning-paths/in-progress-options",

    adminAssignments: "/admin/assignments",
    adminAssignmentOptions: "/admin/assignments/options",

    adminQuestions: "/admin/questions",
    adminAnswersBulk: "/admin/answers/bulk",
    adminQuestionOptions: "/admin/questions/options",

    adminAnswers: "/admin/answers",

    adminAssignedAssignments: "/admin/assigned-assignments",
    adminAvailableAssignments: "/admin/assigned-assignments/available-assignments",

    studentDashboard: "/student/dashboard",
    studentCourses: "/student/courses",
    studentAssignments: "/student/assignments",
    courseAssignments: (id) => `/student/courses/${id}/assignments`,
    studentEnrollments: "/student/enrollments",
    studentSchedule: "/student/schedule",
    studentPayments: "/payment-transactions/me",
    studentLessonDetail: "/student/lessons",
    
    studentParentLinks: "/student/parent-links",
    studentCurrentParentLink: "/student/parent-links/current",
    studentParentLink: linkId => `/student/parent-links/${linkId}`,
    studentLinkedParents: "/student/parent-links/linked-parents",
    studentUnlinkParent: linkId => `/student/parent-links/linked-parents/${linkId}`,

    studentClassAttendance: classId => `/student/classes/${classId}/attendance`,
    studentClassOnlineSessions: classId => `/student/classes/${classId}/online-sessions`,

    paymentTransactions: "/payment-transactions",

    managerDashboard: "/manager/dashboard",
    managerPayments: "/manager/payment-transactions",
    managerCourses: "/manager/courses",
    managerClasses: "/manager/classes",
    managerEnrollments: "/manager/enrollments",
    managerOnlineSessions: "/manager/online-sessions",
    managerOnlineSessionOptions: "/manager/online-sessions/options",
    managerAttendances: "/manager/attendances",
    managerProgress: "/manager/progress",
    managerReports: "/manager/reports",
    managerCategoryOptions: "/manager/options/categories",
    managerCourseOptions: "/manager/options/courses",
    managerClassOptions: "/manager/options/classes",
    managerTeacherOptions: "/manager/options/teachers",
    managerStudentOptions: "/manager/options/students",

    teacherClasses: "/teacher/classes",
    teacherGrading: "/teacher/grading",
    teacherClassDetail: classId => `/teacher/classes/${classId}`,
    teacherClassStudents: classId => `/teacher/classes/${classId}/students`,
    teacherClassProgress: classId => `/teacher/classes/${classId}/progress`,
    teacherClassSessions: classId => `/teacher/classes/${classId}/online-sessions`,
    teacherSessionAttendance: (classId, sessionId) =>
        `/teacher/classes/${classId}/sessions/${sessionId}/attendance`,
    teacherStudentAttendance: (classId, sessionId, studentId) =>
        `/teacher/classes/${classId}/sessions/${sessionId}/attendance/${studentId}`,
    teacherClassAvailableAssignments: classId =>
        `/teacher/classes/${classId}/available-assignments`,
    teacherClassAssignments: classId =>
        `/teacher/classes/${classId}/assignments`,
    teacherManualAssignment: classId =>
        `/teacher/classes/${classId}/assignments/manual`,
    teacherReleaseCurrentAssignment: (classId, studentLearningPathId) =>
        `/teacher/classes/${classId}/learning-paths/${studentLearningPathId}/release-current`,

    parentStudents: "/parent/students",
    parentLinks: "/parent/links",
    parentStudentDashboard: studentId => `/parent/students/${studentId}/dashboard`,
    parentStudentProgress: studentId => `/parent/students/${studentId}/progress`,
    parentStudentAssignments: studentId => `/parent/students/${studentId}/assignments`,
    parentStudentAttendance: studentId => `/parent/students/${studentId}/attendance`,
    parentUnlinkStudent: linkId => `/parent/links/${linkId}`,

};

const Apis = axios.create({ baseURL: BASE_URL });

export const authApis = () => {
    const token = cookies.load("token");

    return axios.create({
        baseURL: BASE_URL,
        headers: { Authorization: `Bearer ${token}` }
    });
};

export default Apis;