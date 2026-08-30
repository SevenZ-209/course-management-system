import { useEffect, useState } from "react";
import { Alert, Badge, Card, Col, Row, Table } from "react-bootstrap";
import { Link } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Dashboard.css";

const Dashboard = () => {
    const [stats, setStats] = useState({
        users: 0, courses: 0, classes: 0, activeEnrollments: 0,
        pendingPayments: 0, assignments: 0, learningPaths: 0, assignedAssignments: 0
    });
    const [payments, setPayments] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const today = () => {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    };

    const list = (data, keys) => {
        for (const key of keys)
            if (Array.isArray(data?.[key])) return data[key];
        return [];
    };

    const loadDashboard = async () => {
        try {
            setLoading(true);
            setErr("");

            const [
                usersRes, coursesRes, classesRes, enrollmentsRes, pendingPaymentsRes,
                assignmentsRes, pathsRes, assignedRes, paymentsRes, sessionsRes
            ] = await Promise.all([
                authApis().get(endpoints.adminUsers, { params: { page: 1 } }),
                authApis().get(endpoints.adminCourses, { params: { page: 1 } }),
                authApis().get(endpoints.adminClasses, { params: { page: 1 } }),
                authApis().get(endpoints.adminEnrollments, { params: { page: 1, status: "ACTIVE" } }),
                authApis().get(endpoints.adminPayments, { params: { page: 1, status: "PENDING" } }),
                authApis().get(endpoints.adminAssignments, { params: { page: 1 } }),
                authApis().get(endpoints.adminLearningPaths, { params: { page: 1 } }),
                authApis().get(endpoints.adminAssignedAssignments, { params: { page: 1 } }),
                authApis().get(endpoints.adminPayments, { params: { page: 1 } }),
                authApis().get(endpoints.adminOnlineSessions, { params: { page: 1, date: today() } })
            ]);

            setStats({
                users: usersRes.data?.totalRecords || 0,
                courses: coursesRes.data?.totalRecords || 0,
                classes: classesRes.data?.totalRecords || 0,
                activeEnrollments: enrollmentsRes.data?.totalRecords || 0,
                pendingPayments: pendingPaymentsRes.data?.totalRecords || 0,
                assignments: assignmentsRes.data?.totalRecords || 0,
                learningPaths: pathsRes.data?.totalRecords || 0,
                assignedAssignments: assignedRes.data?.totalRecords || 0
            });

            setPayments(list(paymentsRes.data, ["transactions", "payments", "items", "content"]).slice(0, 5));
            setSessions(list(sessionsRes.data, ["sessions", "items", "content"]).slice(0, 5));
        } catch (ex) {
            console.error("Load admin dashboard error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải dữ liệu tổng quan!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDashboard();
    }, []);

    const cards = [
        { label: "Người dùng", value: stats.users, note: "Tổng tài khoản", path: "/admin/users", icon: "♙" },
        { label: "Khóa học", value: stats.courses, note: "Khóa học hệ thống", path: "/admin/courses", icon: "▤" },
        { label: "Học viên đang học", value: stats.activeEnrollments, note: "Enrollment ACTIVE", path: "/admin/enrollments", icon: "◉" },
        { label: "Chờ thanh toán", value: stats.pendingPayments, note: "Cần xử lý", path: "/admin/payments", icon: "₫" }
    ];

    const miniStats = [
        { label: "Lớp học", value: stats.classes },
        { label: "Bài tập", value: stats.assignments },
        { label: "Lộ trình", value: stats.learningPaths },
        { label: "Bài đã giao", value: stats.assignedAssignments }
    ];

    const money = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;
    const dateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

    const paymentBadge = status => {
        if (status === "SUCCESS") return <Badge bg="success">Thành công</Badge>;
        if (status === "FAILED") return <Badge bg="danger">Thất bại</Badge>;
        return <Badge bg="warning" text="dark">Chờ xử lý</Badge>;
    };

    if (loading)
        return <div className="text-center py-5"><MySpinner /></div>;

    return (
        <>
            <div className="cm-dashboard-heading">
                <div>
                    <span>TỔNG QUAN</span>
                    <h2>Hoạt động hệ thống</h2>
                    <p>Theo dõi nhanh tình hình vận hành của nền tảng.</p>
                </div>

                <div className="cm-dashboard-date">
                    {new Date().toLocaleDateString("vi-VN")}
                </div>
            </div>

            {err && <Alert variant="danger">{err}</Alert>}

            <Row className="g-4 mb-4">
                {cards.map(card => (
                    <Col xl={3} md={6} key={card.label}>
                        <Link to={card.path}>
                            <div className="cm-stat-card">
                                <div className="cm-stat-top">
                                    <div className="cm-stat-icon">{card.icon}</div>
                                    <span>→</span>
                                </div>

                                <div className="cm-stat-value">{card.value}</div>
                                <div className="cm-stat-label">{card.label}</div>
                                <small>{card.note}</small>
                            </div>
                        </Link>
                    </Col>
                ))}
            </Row>

            <div className="cm-mini-stats mb-4">
                {miniStats.map(item => (
                    <div key={item.label}>
                        <strong>{item.value}</strong>
                        <span>{item.label}</span>
                    </div>
                ))}
            </div>

            <Row className="g-4">
                <Col xl={7}>
                    <Card className="cm-admin-card h-100">
                        <Card.Body className="p-0">
                            <div className="cm-card-heading">
                                <div>
                                    <h5>Giao dịch gần đây</h5>
                                    <p>Theo dõi thanh toán học phí</p>
                                </div>

                                <Link to="/admin/payments">Xem tất cả →</Link>
                            </div>

                            {payments.length === 0 ? (
                                <div className="cm-dashboard-empty">Chưa có giao dịch.</div>
                            ) : (
                                <Table hover responsive className="cm-dashboard-table mb-0">
                                    <thead>
                                        <tr>
                                            <th>HỌC VIÊN</th>
                                            <th>SỐ TIỀN</th>
                                            <th>TRẠNG THÁI</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {payments.map(p => (
                                            <tr key={p.id ?? p.transactionId}>
                                                <td>
                                                    {p.studentName || p.studentFullName ||
                                                        p.enrollment?.student?.fullName || "-"}
                                                </td>

                                                <td className="fw-semibold">{money(p.amount)}</td>
                                                <td>{paymentBadge(p.status)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={5}>
                    <Card className="cm-admin-card h-100">
                        <Card.Body>
                            <div className="cm-card-heading px-0 pt-0">
                                <div>
                                    <h5>Buổi học hôm nay</h5>
                                    <p>{today()}</p>
                                </div>

                                <Link to="/admin/online-sessions">Xem tất cả →</Link>
                            </div>

                            {sessions.length === 0 ? (
                                <div className="cm-dashboard-empty">Hôm nay chưa có buổi học.</div>
                            ) : (
                                sessions.map(session => (
                                    <div className="cm-session-item" key={session.id ?? session.sessionId}>
                                        <div className="cm-session-time">
                                            {session.startTime
                                                ? new Date(session.startTime).toLocaleTimeString("vi-VN", {
                                                    hour: "2-digit", minute: "2-digit"
                                                })
                                                : "--:--"}
                                        </div>

                                        <div>
                                            <strong>{session.title}</strong>

                                            <small>
                                                {session.className || session.courseClassName ||
                                                    session.courseClass?.name || "Chưa xác định lớp"}
                                            </small>

                                            <span>{dateTime(session.startTime)}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </>
    );
};

export default Dashboard;