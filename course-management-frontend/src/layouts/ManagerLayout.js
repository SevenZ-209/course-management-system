import { useContext } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { MyUserContext } from "../configs/Contexts";
import ManagerSidebar from "../components/ManagerSidebar";
import "../styles/AdminLayout.css";
import "../styles/AdminCommon.css";

const ManagerLayout = () => {
    const [user] = useContext(MyUserContext);
    const location = useLocation();

    const titles = {
        "/manager": "Tổng quan",
        "/manager/payments": "Thanh toán",
        "/manager/grading": "Chấm bài"
    };

    const title = location.pathname.startsWith("/manager/grading")
        ? "Chấm bài"
        : titles[location.pathname] || "Quản lý vận hành";

    return (
        <div className="cm-admin-layout">
            <ManagerSidebar />

            <main className="cm-admin-main">
                <header className="cm-admin-topbar">
                    <div className="cm-admin-topbar-brand">
                        CourseHub Manager <span></span>
                        <small>{title}</small>
                    </div>

                    <div className="cm-admin-topbar-right">
                        <Link to="/" className="cm-view-site">◉ Xem trang web</Link>

                        <div className="cm-topbar-user">
                            <div>
                                <strong>{user?.fullName || user?.username}</strong>
                                <small>Manager</small>
                            </div>

                            <div className="cm-topbar-avatar">
                                {(user?.fullName || user?.username || "M").charAt(0).toUpperCase()}
                            </div>
                        </div>
                    </div>
                </header>

                <div className="cm-admin-content"><Outlet /></div>
            </main>
        </div>
    );
};

export default ManagerLayout;
