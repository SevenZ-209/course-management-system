import { useEffect, useState } from "react";
import { Alert, Badge, Card, Col, Row, Table } from "react-bootstrap";
import { Link } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Dashboard.css";

const Dashboard = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setErr("");
                const res = await authApis().get(endpoints.managerDashboard);
                setData(res.data);
            } catch (ex) {
                setErr(ex.response?.data?.message || "Không thể tải dữ liệu quản lý!");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const money = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;
    const dateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";
    const paymentBadge = status => {
        if (status === "SUCCESS") return <Badge bg="success">Thành công</Badge>;
        if (status === "FAILED") return <Badge bg="danger">Thất bại</Badge>;
        return <Badge bg="warning" text="dark">Chờ xử lý</Badge>;
    };

    if (loading) return <div className="text-center py-5"><MySpinner /></div>;

    const stats = data || {};
    const cards = [
        { label: "Khóa học hoạt động", value: stats.activeCourses || 0, note: "Course ACTIVE", icon: "▤", path: "/manager/courses" },
        { label: "Lớp đang hoạt động", value: stats.activeClasses || 0, note: "Class ACTIVE", icon: "▣", path: "/manager/classes" },
        { label: "Học viên đang học", value: stats.activeEnrollments || 0, note: "Enrollment ACTIVE", icon: "◉", path: "/manager/progress" },
        { label: "Chờ thanh toán", value: stats.pendingPayments || 0, note: "Cần xử lý", icon: "₫", path: "/manager/payments" }
    ];

    return (
        <>
            <div className="cm-dashboard-heading">
                <div>
                    <span>MANAGER</span>
                    <h2>Tổng quan vận hành</h2>
                    <p>Theo dõi nhanh hoạt động đào tạo và các công việc cần xử lý.</p>
                </div>
                <div className="d-flex align-items-center gap-2">
                    <Link className="btn btn-sm btn-outline-primary" to="/manager/reports">Báo cáo vận hành</Link>
                    <div className="cm-dashboard-date">{new Date().toLocaleDateString("vi-VN")}</div>
                </div>
            </div>

            {err && <Alert variant="danger">{err}</Alert>}

            <Row className="g-4 mb-4">
                {cards.map(card => (
                    <Col xl={3} md={6} key={card.label}>
                        {card.path ? (
                            <Link to={card.path}>
                                <div className="cm-stat-card">
                                    <div className="cm-stat-top"><div className="cm-stat-icon">{card.icon}</div><span>→</span></div>
                                    <div className="cm-stat-value">{card.value}</div>
                                    <div className="cm-stat-label">{card.label}</div><small>{card.note}</small>
                                </div>
                            </Link>
                        ) : (
                            <div className="cm-stat-card">
                                <div className="cm-stat-top"><div className="cm-stat-icon">{card.icon}</div></div>
                                <div className="cm-stat-value">{card.value}</div>
                                <div className="cm-stat-label">{card.label}</div><small>{card.note}</small>
                            </div>
                        )}
                    </Col>
                ))}
            </Row>

            <div className="cm-mini-stats mb-4">
                <Link to="/manager/progress"><strong>{stats.activeStudents || 0}</strong><span>Học viên active</span></Link>
                <Link to="/manager/classes"><strong>{stats.activeTeachers || 0}</strong><span>Giảng viên active</span></Link>
                <Link to="/manager/enrollments?status=PENDING_PAYMENT"><strong>{stats.pendingEnrollments || 0}</strong><span>Enrollment chờ phí</span></Link>
                <Link to="/manager/payments?status=PENDING"><strong>{stats.pendingPayments || 0}</strong><span>Payment chờ xử lý</span></Link>
            </div>

            <Row className="g-4">
                <Col xl={7}>
                    <Card className="cm-admin-card h-100">
                        <Card.Body className="p-0">
                            <div className="cm-card-heading">
                                <div><h5>Giao dịch gần đây</h5><p>Theo dõi thanh toán học phí</p></div>
                                <Link to="/manager/payments">Xem tất cả →</Link>
                            </div>
                            {(stats.recentPayments || []).length === 0 ? (
                                <div className="cm-dashboard-empty">Chưa có giao dịch.</div>
                            ) : (
                                <Table hover responsive className="cm-dashboard-table mb-0">
                                    <thead><tr><th>HỌC VIÊN</th><th>SỐ TIỀN</th><th>TRẠNG THÁI</th></tr></thead>
                                    <tbody>
                                        {(stats.recentPayments || []).map(p => (
                                            <tr key={p.id}>
                                                <td>{p.studentName || p.username || "-"}</td>
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
                                <div><h5>Buổi học hôm nay</h5><p>Lịch vận hành trong ngày</p></div>
                                <Link to="/manager/online-sessions">Xem lịch →</Link>
                            </div>
                            {(stats.todaySessions || []).length === 0 ? (
                                <div className="cm-dashboard-empty">Hôm nay chưa có buổi học.</div>
                            ) : (
                                (stats.todaySessions || []).map(session => (
                                    <div className="cm-session-item" key={session.id}>
                                        <div className="cm-session-time">
                                            {session.startTime ? new Date(session.startTime).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) : "--:--"}
                                        </div>
                                        <div>
                                            <strong>{session.title}</strong>
                                            <small>{session.className || "Chưa xác định lớp"}</small>
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
