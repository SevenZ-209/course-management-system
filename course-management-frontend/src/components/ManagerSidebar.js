import { useContext } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import cookies from "react-cookies";
import { MyUserContext } from "../configs/Contexts";
import "../styles/AdminSidebar.css";

const ManagerSidebar = () => {
    const location = useLocation();
    const nav = useNavigate();
    const [user, dispatch] = useContext(MyUserContext);

    const groups = [
        { title: "TỔNG QUAN", items: [{ path: "/manager", icon: "⌂", label: "Tổng quan", exact: true }] },
        {
            title: "HỌC VỤ",
            items: [
                { path: "/manager/courses", icon: "▣", label: "Khóa học" },
                { path: "/manager/classes", icon: "▤", label: "Lớp học" },
                { path: "/manager/enrollments", icon: "◎", label: "Đăng ký học" },
                { path: "/manager/online-sessions", icon: "◷", label: "Buổi học" },
                { path: "/manager/attendances", icon: "☑", label: "Điểm danh" },
                { path: "/manager/progress", icon: "↗", label: "Tiến độ học tập" },
                { path: "/manager/reports", icon: "▥", label: "Báo cáo vận hành" }
            ]
        },
        {
            title: "VẬN HÀNH",
            items: [
                { path: "/manager/payments", icon: "₫", label: "Thanh toán" },
                { path: "/manager/grading", icon: "✓", label: "Chấm bài" }
            ]
        }
    ];

    const isActive = item => item.exact
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
                    <small>Manager Portal</small>
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
                        {(user?.fullName || user?.username || "M").charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <strong>{user?.fullName || user?.username || "Manager"}</strong>
                        <small>Quản lý vận hành</small>
                    </div>
                </div>

                <button className="cm-admin-logout" onClick={logout}>
                    <span>↪</span> Đăng xuất
                </button>
            </div>
        </aside>
    );
};

export default ManagerSidebar;
