import { useContext } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import cookies from "react-cookies";
import { MyUserContext } from "../configs/Contexts";
import "../styles/AdminSidebar.css";

const AdminSidebar = () => {
    const location = useLocation();
    const nav = useNavigate();
    const [user, dispatch] = useContext(MyUserContext);

    const groups = [
        {
            title: "TỔNG QUAN",
            items: [
                { path: "/admin", icon: "⌂", label: "Tổng quan", exact: true }
            ]
        },
        {
            title: "ĐÀO TẠO",
            items: [
                { path: "/admin/categories", icon: "▦", label: "Danh mục" },
                { path: "/admin/courses", icon: "▤", label: "Khóa học" },
                { path: "/admin/course-modules", icon: "≡", label: "Module khóa học" },
                { path: "/admin/lessons", icon: "□", label: "Bài học" },
                { path: "/admin/classes", icon: "▣", label: "Lớp học" },
                { path: "/admin/online-sessions", icon: "◉", label: "Buổi học" },
                { path: "/admin/attendances", icon: "✓", label: "Điểm danh" }
            ]
        },
        {
            title: "HỌC VIÊN",
            items: [
                { path: "/admin/users", icon: "♙", label: "Người dùng" },
                { path: "/admin/enrollments", icon: "＋", label: "Đăng ký khóa học" },
                { path: "/admin/payments", icon: "₫", label: "Thanh toán" }
            ]
        },
        {
            title: "LỘ TRÌNH & BÀI TẬP",
            items: [
                { path: "/admin/learning-paths", icon: "◇", label: "Lộ trình học" },
                { path: "/admin/learning-path-details", icon: "↳", label: "Chi tiết lộ trình" },
                { path: "/admin/student-learning-paths", icon: "⌁", label: "Lộ trình học viên" },
                { path: "/admin/assignments", icon: "✎", label: "Bài tập" },
                { path: "/admin/questions", icon: "?", label: "Câu hỏi" },
                { path: "/admin/answers", icon: "✓", label: "Đáp án" },
                { path: "/admin/assigned-assignments", icon: "➤", label: "Bài tập đã giao" }
            ]
        }
    ];

    const isActive = item =>
        item.exact
            ? location.pathname === item.path
            : location.pathname.startsWith(item.path);

    const logout = () => {
        cookies.remove("token", { path: "/" });
        cookies.remove("user", { path: "/" });
        dispatch({ type: "LOGOUT" });
        nav("/");
    };

    return (
        <aside className="cm-admin-sidebar">
            <Link to="/" className="cm-admin-brand">
                <div className="cm-admin-logo">C</div>
                <div>
                    <div className="cm-admin-brand-name">CourseHub</div>
                    <small>Quay lại trang chủ</small>
                </div>
            </Link>

            <div className="cm-admin-navigation">
                {groups.map(group => (
                    <div className="cm-admin-group" key={group.title}>
                        <div className="cm-admin-group-title">{group.title}</div>

                        {group.items.map(item => (
                            <Link key={item.path} to={item.path}
                                className={`cm-admin-nav-item ${isActive(item) ? "active" : ""}`}>
                                <span className="cm-admin-nav-icon">{item.icon}</span>
                                <span>{item.label}</span>
                            </Link>
                        ))}
                    </div>
                ))}
            </div>

            <div className="cm-admin-sidebar-bottom">
                <div className="cm-admin-mini-user">
                    <div className="cm-admin-avatar">
                        {(user?.fullName || user?.username || "A").charAt(0).toUpperCase()}
                    </div>

                    <div>
                        <strong>{user?.fullName || user?.username || "Admin"}</strong>
                        <small>Quản trị viên</small>
                    </div>
                </div>

                <button className="cm-admin-logout" onClick={logout}>
                    <span>↪</span> Đăng xuất
                </button>
            </div>
        </aside>
    );
};

export default AdminSidebar;