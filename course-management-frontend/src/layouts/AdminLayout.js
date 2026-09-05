import { useContext } from "react";
import { Outlet, Link, useLocation } from "react-router-dom";
import { MyUserContext } from "../configs/Contexts";
import AdminSidebar from "../components/AdminSidebar";
import "../styles/AdminLayout.css";
import "../styles/AdminCommon.css";

const AdminLayout = () => {
    const [user] = useContext(MyUserContext);
    const location = useLocation();

    const titles = {
        "/admin": "Tổng quan",
        "/admin/users": "Người dùng",
        "/admin/categories": "Danh mục",
        "/admin/courses": "Khóa học",
        "/admin/course-modules": "Module khóa học",
        "/admin/lessons": "Bài học",
        "/admin/classes": "Lớp học",
        "/admin/online-sessions": "Buổi học",
        "/admin/attendances": "Điểm danh",
        "/admin/enrollments": "Đăng ký khóa học",
        "/admin/payments": "Thanh toán",
        "/admin/parent-links": "Liên kết phụ huynh",
        "/admin/learning-paths": "Lộ trình học",
        "/admin/learning-path-details": "Chi tiết lộ trình",
        "/admin/student-learning-paths": "Lộ trình học viên",
        "/admin/assignments": "Bài tập",
        "/admin/questions": "Câu hỏi",
        "/admin/answers": "Đáp án",
        "/admin/assigned-assignments": "Bài tập đã giao"
    };

    const title = titles[location.pathname] || "Quản trị hệ thống";

    return (
        <div className="cm-admin-layout">
            <AdminSidebar />

            <main className="cm-admin-main">
                <header className="cm-admin-topbar">
                    <div>
                        <div className="cm-admin-topbar-brand">
                            CourseHub Admin <span></span>
                            <small>{title}</small>
                        </div>
                    </div>

                    <div className="cm-admin-topbar-right">
                        <Link to="/" className="cm-view-site">
                            ◉ Xem trang web
                        </Link>

                        <div className="cm-topbar-user">
                            <div>
                                <strong>{user?.fullName || user?.username}</strong>
                                <small>Administrator</small>
                            </div>

                            <div className="cm-topbar-avatar">
                                {(user?.fullName || user?.username || "A").charAt(0).toUpperCase()}
                            </div>
                        </div>
                    </div>
                </header>

                <div className="cm-admin-content">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;