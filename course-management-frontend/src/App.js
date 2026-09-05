import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { useReducer } from "react";
import cookies from "react-cookies";
import "bootstrap/dist/css/bootstrap.min.css";

import { MyUserContext } from "./configs/Contexts";
import MyUserReducer from "./reducers/MyUserReducer";

import Header from "./components/Header";
import Footer from "./components/Footer";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import ManagerLayout from "./layouts/ManagerLayout";

import Login from "./screens/Auth/Login";
import Register from "./screens/Auth/Register";
import Home from "./screens/Home/Home";
import CourseDetail from "./screens/Home/CourseDetail";

import AdminDashboard from "./screens/Admin/Dashboard";
import AdminUsers from "./screens/Admin/Users";
import AdminCategories from "./screens/Admin/Categories";
import AdminCourses from "./screens/Admin/Courses";
import AdminCourseModules from "./screens/Admin/CourseModules";
import AdminLessons from "./screens/Admin/Lessons";
import AdminClasses from "./screens/Admin/Classes";
import AdminOnlineSessions from "./screens/Admin/OnlineSessions";
import AdminAttendances from "./screens/Admin/Attendances";
import AdminEnrollments from "./screens/Admin/Enrollments";
import AdminPayments from "./screens/Admin/Payments";
import AdminParentLinks from "./screens/Admin/ParentLinks";
import AdminLearningPaths from "./screens/Admin/LearningPaths";
import AdminLearningPathDetails from "./screens/Admin/LearningPathDetails";
import AdminStudentLearningPaths from "./screens/Admin/StudentLearningPaths";
import AdminAssignments from "./screens/Admin/Assignments";
import AdminQuestions from "./screens/Admin/Questions";
import AdminAnswers from "./screens/Admin/Answers";
import AdminAssignedAssignments from "./screens/Admin/AssignedAssignments";
import AdminReports from "./screens/Admin/Reports";

import ManagerDashboard from "./screens/Manager/Dashboard";
import ManagerPayments from "./screens/Manager/Payments";
import ManagerCourses from "./screens/Manager/Courses";
import ManagerClasses from "./screens/Manager/Classes";
import ManagerEnrollments from "./screens/Manager/Enrollments";
import ManagerOnlineSessions from "./screens/Manager/OnlineSessions";
import ManagerAttendances from "./screens/Manager/Attendances";
import ManagerProgress from "./screens/Manager/Progress";
import ManagerReports from "./screens/Manager/Reports";

import StudentDashboard from "./screens/Student/Dashboard";
import StudentCourses from "./screens/Student/Courses";
import StudentCourseDetail from "./screens/Student/CourseDetail";
import StudentAssignments from "./screens/Student/Assignments";
import StudentAttempt from "./screens/Student/Attempt";
import StudentAttemptResult from "./screens/Student/AttemptResult";
import StudentSchedule from "./screens/Student/Schedule";
import LessonDetail from "./screens/Student/LessonDetail";
import Payment from "./screens/Student/Payment";
import Payments from "./screens/Student/Payments";

import TeacherClasses from "./screens/Teacher/Classes";
import TeacherClassDetail from "./screens/Teacher/ClassDetail";
import TeacherAttendance from "./screens/Teacher/Attendance";
import TeacherGrading from "./screens/Teacher/Grading";
import TeacherGradingDetail from "./screens/Teacher/GradingDetail";

import ParentDashboard from "./screens/Parent/Dashboard";

const App = () => {
    const [user, dispatch] = useReducer(MyUserReducer, cookies.load("user") || null);

    return (
        <BrowserRouter>
            <MyUserContext.Provider value={[user, dispatch]}>
                <Header />

                <div className="p-0" style={{ minHeight: "80vh" }}>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/courses/:courseId" element={<CourseDetail />} />

                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />

                        <Route path="/admin" element={
                            <ProtectedRoute roles={["ADMIN"]}>
                                <AdminLayout />
                            </ProtectedRoute>
                        }>
                            <Route index element={<AdminDashboard />} />
                            <Route path="users" element={<AdminUsers />} />
                            <Route path="categories" element={<AdminCategories />} />
                            <Route path="courses" element={<AdminCourses />} />
                            <Route path="course-modules" element={<AdminCourseModules />} />
                            <Route path="lessons" element={<AdminLessons />} />
                            <Route path="classes" element={<AdminClasses />} />
                            <Route path="online-sessions" element={<AdminOnlineSessions />} />
                            <Route path="attendances" element={<AdminAttendances />} />
                            <Route path="enrollments" element={<AdminEnrollments />} />
                            <Route path="payments" element={<AdminPayments />} />
                            <Route path="parent-links" element={<AdminParentLinks />} />
                            <Route path="learning-paths" element={<AdminLearningPaths />} />
                            <Route path="learning-path-details" element={<AdminLearningPathDetails />} />
                            <Route path="student-learning-paths" element={<AdminStudentLearningPaths />} />
                            <Route path="assignments" element={<AdminAssignments />} />
                            <Route path="questions" element={<AdminQuestions />} />
                            <Route path="answers" element={<AdminAnswers />} />
                            <Route path="assigned-assignments" element={<AdminAssignedAssignments />} />
                            <Route path="reports" element={<AdminReports />} />
                        </Route>

                        <Route path="/manager" element={
                            <ProtectedRoute roles={["MANAGER"]}>
                                <ManagerLayout />
                            </ProtectedRoute>
                        }>
                            <Route index element={<ManagerDashboard />} />
                            <Route path="courses" element={<ManagerCourses />} />
                            <Route path="classes" element={<ManagerClasses />} />
                            <Route path="enrollments" element={<ManagerEnrollments />} />
                            <Route path="online-sessions" element={<ManagerOnlineSessions />} />
                            <Route path="attendances" element={<ManagerAttendances />} />
                            <Route path="progress" element={<ManagerProgress />} />
                            <Route path="reports" element={<ManagerReports />} />
                            <Route path="payments" element={<ManagerPayments />} />
                            <Route path="grading" element={<TeacherGrading />} />
                            <Route path="grading/:attemptId" element={<TeacherGradingDetail />} />
                        </Route>

                        <Route path="/student" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentDashboard />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/courses" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentCourses />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/courses/:enrollmentId" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentCourseDetail />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/assignments" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentAssignments />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/assignments/attempt/:attemptId" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentAttempt />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/assignments/result/:attemptId" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentAttemptResult />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/schedule" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <StudentSchedule />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/payments" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <Payments />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/payments/:enrollmentId" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <Payment />
                            </ProtectedRoute>
                        } />

                        <Route path="/student/lessons/:lessonId" element={
                            <ProtectedRoute roles={["STUDENT"]}>
                                <LessonDetail  />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <Navigate to="/teacher/classes" replace />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher/classes" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <TeacherClasses />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher/classes/:classId" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <TeacherClassDetail />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher/classes/:classId/sessions/:sessionId/attendance" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <TeacherAttendance />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher/grading" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <TeacherGrading />
                            </ProtectedRoute>
                        } />

                        <Route path="/teacher/grading/:attemptId" element={
                            <ProtectedRoute roles={["TEACHER"]}>
                                <TeacherGradingDetail />
                            </ProtectedRoute>
                        } />

                        <Route path="/parent" element={
                            <ProtectedRoute roles={["PARENT"]}>
                                <ParentDashboard />
                            </ProtectedRoute>
                        } />

                        <Route path="/unauthorized" element={
                            <div className="text-center mt-5">
                                <h1>403</h1>
                                <p>Bạn không có quyền truy cập trang này.</p>
                            </div>
                        } />

                        <Route path="*" element={
                            <div className="text-center mt-5">
                                <h1>404</h1>
                                <p>Không tìm thấy trang.</p>
                            </div>
                        } />
                    </Routes>
                </div>

                <Footer />
            </MyUserContext.Provider>
        </BrowserRouter>
    );
};

export default App;