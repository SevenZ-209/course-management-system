import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { MyUserContext } from "../../configs/Contexts";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/StudentDashboard.css";

const Dashboard = () => {
    const [user] = useContext(MyUserContext);
    const nav = useNavigate();

    const [dashboard, setDashboard] = useState(null);
    const [courses, setCourses] = useState([]);
    const [parentLink, setParentLink] = useState(null);
    const [linkedParents, setLinkedParents] = useState([]);
    const [linking, setLinking] = useState(false);
    const [unlinking, setUnlinking] = useState(false);
    const [unlinkingParentId, setUnlinkingParentId] = useState(null);
    const [copied, setCopied] = useState(false);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const loadDashboard = async () => {
        try {
            setLoading(true);
            setErr("");
    
            const [dashboardRes, coursesRes, linkRes, linkedParentsRes] = await Promise.all([
                authApis().get(endpoints.studentDashboard),
                authApis().get(endpoints.studentCourses),
                authApis().get(endpoints.studentCurrentParentLink),
                authApis().get(endpoints.studentLinkedParents)
            ]);
    
            setDashboard(dashboardRes.data);
            setCourses(Array.isArray(coursesRes.data) ? coursesRes.data.slice(0, 3) : []);
            setParentLink(linkRes.data?.linkId ? linkRes.data : null);
            setLinkedParents(Array.isArray(linkedParentsRes.data) ? linkedParentsRes.data : []);
        } catch (ex) {
            console.error("Student dashboard error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải tổng quan học tập!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDashboard();
    }, []);

    const enrollmentId = c => c.enrollmentId ?? c.id;
    const courseName = c => c.courseName ?? c.course?.name ?? "Khóa học";
    const className = c => c.className ?? c.courseClassName ?? c.courseClass?.name ?? "-";

    const firstName = () => {
        const name = user?.fullName || user?.username || "bạn";
        return name.trim().split(/\s+/).pop();
    };

    const createParentLink = async () => {
    
        try {
            setLinking(true);
            setErr("");
            setSuccess("");
    
            const res = await authApis().post(endpoints.studentParentLinks);
            setParentLink(res.data);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tạo mã liên kết!");
        } finally {
            setLinking(false);
        }
    };
    
    const expireParentLink = async () => {
        if (!parentLink) return;
        if (!window.confirm("Bạn có chắc muốn hủy mã liên kết này?")) return;
    
        try {
            setUnlinking(true);
            setErr("");
            setSuccess("");
    
            await authApis().delete(endpoints.studentParentLink(parentLink.linkId));
            setParentLink(null);
            setSuccess("Đã hủy mã liên kết.");
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể hủy mã liên kết!");
        } finally {
            setUnlinking(false);
        }
    };
    
    const unlinkLinkedParent = async parent => {
        if (!window.confirm(`Hủy quyền theo dõi của ${parent.fullName}?`)) return;

        try {
            setUnlinkingParentId(parent.linkId);
            setErr("");
            setSuccess("");

            const res = await authApis().delete(endpoints.studentUnlinkParent(parent.linkId));
            setLinkedParents(current => current.filter(item => item.linkId !== parent.linkId));
            setSuccess(res.data?.message || `Đã hủy liên kết với ${parent.fullName}.`);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể hủy liên kết phụ huynh!");
        } finally {
            setUnlinkingParentId(null);
        }
    };

    const copyParentLink = async () => {
        try {
            await navigator.clipboard.writeText(parentLink.verificationCode);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch {
            setErr("Không thể sao chép mã liên kết!");
        }
    };
    
    const formatDateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

    if (loading)
        return <div className="text-center py-5"><MySpinner /></div>;

    const stats = [
        {
            label: "Khóa học đang học",
            value: dashboard?.activeCourses || 0,
            note: "Khóa học đang hoạt động",
            icon: "▤",
            path: "/student/courses"
        },
        {
            label: "Bài đang làm",
            value: dashboard?.inProgressAssignments || 0,
            note: "Chưa hoàn thành",
            icon: "✎",
            path: "/student/assignments"
        },
        {
            label: "Đang chờ chấm",
            value: dashboard?.pendingGradingAssignments || 0,
            note: "Bài đã nộp",
            icon: "⌛",
            path: "/student/assignments"
        },
        {
            label: "Lộ trình hoàn thành",
            value: dashboard?.completedLearningPaths || 0,
            note: "Đã hoàn tất",
            icon: "✓",
            path: "/student/courses"
        }
    ];

    const current = dashboard?.continueAssignment;

    return (
        <div className="cm-student-dashboard">
            <div className="cm-student-container">
                <section className="cm-student-welcome">
                    <div>
                        <span>TRANG HỌC TẬP</span>
                        <h1>Xin chào, {firstName()} 👋</h1>
                        <p>Tiếp tục hành trình học tập và hoàn thành mục tiêu của bạn.</p>
                    </div>

                    <Button onClick={() => nav("/student/courses")}>Khóa học của tôi</Button>
                </section>

                {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}
                {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}

                <Row className="g-4 cm-student-stats">
                    {stats.map(item => (
                        <Col xl={3} md={6} key={item.label}>
                            <button className="cm-student-stat-card" onClick={() => nav(item.path)}>
                                <div className="cm-student-stat-top">
                                    <span className="cm-student-stat-icon">{item.icon}</span>
                                    <span className="cm-student-stat-arrow">→</span>
                                </div>

                                <strong>{item.value}</strong>
                                <h6>{item.label}</h6>
                                <small>{item.note}</small>
                            </button>
                        </Col>
                    ))}
                </Row>

                <section className="cm-student-section">
                    <div className="cm-student-section-heading">
                        <div>
                            <span>TIẾP TỤC</span>
                            <h2>Tiếp tục học</h2>
                        </div>
                    </div>

                    {current ? (
                        <div className="cm-continue-card">
                            <div className="cm-continue-left">
                                <div className="cm-continue-icon">▶</div>

                                <div>
                                    <Badge bg="light" text="primary">{current.courseName}</Badge>
                                    <h3>{current.assignmentName}</h3>
                                    <p>Lần làm #{current.attemptNumber} · Bài làm đang được lưu</p>
                                </div>
                            </div>

                            <Button size="lg"
                                onClick={() => nav(`/student/assignments/attempt/${current.attemptId}`)}>
                                Tiếp tục làm bài →
                            </Button>
                        </div>
                    ) : (
                        <div className="cm-student-empty-card">
                            <div className="cm-student-empty-icon">✓</div>

                            <div>
                                <h5>Bạn không có bài đang làm dở</h5>
                                <p>Hãy xem danh sách bài tập để tiếp tục học tập.</p>
                            </div>

                            <Button variant="outline-primary" onClick={() => nav("/student/assignments")}>
                                Xem bài tập
                            </Button>
                        </div>
                    )}
                </section>

                <section className="cm-student-section">
                    <div className="cm-student-section-heading">
                        <div>
                            <span>HỌC TẬP</span>
                            <h2>Khóa học của tôi</h2>
                        </div>

                        <button className="cm-student-view-all" onClick={() => nav("/student/courses")}>
                            Xem tất cả →
                        </button>
                    </div>

                    {courses.length === 0 ? (
                        <div className="cm-student-empty-card">
                            <div>
                                <h5>Bạn chưa có khóa học đang hoạt động</h5>
                                <p>Khám phá các khóa học và đăng ký lớp phù hợp với bạn.</p>
                            </div>

                            <Button onClick={() => nav("/")}>Khám phá khóa học</Button>
                        </div>
                    ) : (
                        <Row className="g-4">
                            {courses.map(course => (
                                <Col lg={4} md={6} key={enrollmentId(course)}>
                                    <div className="cm-my-course-card">
                                        <div className="cm-my-course-icon">▤</div>

                                        <div className="cm-my-course-content">
                                            <span>KHÓA HỌC</span>
                                            <h4>{courseName(course)}</h4>
                                            <p>Lớp: <strong>{className(course)}</strong></p>

                                            <Button variant="outline-primary" size="sm"
                                                onClick={() => nav(`/student/courses/${enrollmentId(course)}`)}>
                                                Tiếp tục học →
                                            </Button>
                                        </div>
                                    </div>
                                </Col>
                            ))}
                        </Row>
                    )}
                </section>

                <section className="cm-student-section">
                    <div className="cm-student-section-heading">
                        <div>
                            <span>PHỤ HUYNH</span>
                            <h2>Liên kết phụ huynh</h2>
                        </div>
                    </div>

                    {linkedParents.length > 0 && (
                        <div className="d-flex flex-column gap-3 mb-3">
                            {linkedParents.map(parent => (
                                <div className="cm-continue-card" key={parent.linkId}>
                                    <div className="cm-continue-left">
                                        <div className="cm-continue-icon">
                                            {(parent.fullName || "P").trim().charAt(0).toUpperCase()}
                                        </div>

                                        <div>
                                            <span className="cm-portal-label">PHỤ HUYNH ĐANG LIÊN KẾT</span>
                                            <h3>{parent.fullName}</h3>
                                            <p>
                                                @{parent.username} · Liên kết từ {formatDateTime(parent.linkedAt)}
                                            </p>
                                        </div>
                                    </div>

                                    <Button variant="outline-danger"
                                        disabled={unlinkingParentId === parent.linkId}
                                        onClick={() => unlinkLinkedParent(parent)}>
                                        {unlinkingParentId === parent.linkId ? "Đang hủy..." : "Hủy liên kết"}
                                    </Button>
                                </div>
                            ))}
                        </div>
                    )}

                    {parentLink ? (
                        <div className="cm-continue-card">
                            <div>
                                <span className="cm-portal-label">MÃ LIÊN KẾT MỚI</span>
                                <h2 className="fw-bold mt-2 mb-1" style={{ letterSpacing: 3 }}>
                                    {parentLink.verificationCode}
                                </h2>
                                <p className="mb-0">
                                    Có hiệu lực đến {formatDateTime(parentLink.expiresAt)} · Mỗi mã chỉ dùng một lần.
                                </p>
                            </div>

                            <div className="d-flex gap-2 flex-wrap">
                                <Button variant="outline-primary" onClick={copyParentLink}>
                                    {copied ? "Đã sao chép" : "Sao chép mã"}
                                </Button>

                                <Button variant="outline-danger" disabled={unlinking} onClick={expireParentLink}>
                                    {unlinking ? "Đang hủy..." : "Hủy mã"}
                                </Button>
                            </div>
                        </div>
                    ) : (
                        <div className="cm-student-empty-card">
                            <div>
                                <h5>{linkedParents.length ? "Liên kết thêm phụ huynh" : "Tạo mã liên kết cho phụ huynh"}</h5>
                                <p>
                                    Mỗi mã chỉ dùng được một lần. Muốn thêm phụ huynh hoặc người giám hộ khác, hãy tạo một mã mới.
                                </p>
                            </div>

                            <div>
                                <p className="cm-portal-muted mb-3">
                                    Mã liên kết có hiệu lực trong 5 phút sau khi tạo.
                                </p>

                                <Button disabled={linking} onClick={createParentLink}>
                                    {linking ? "Đang tạo..." : linkedParents.length ? "+ Tạo mã mới" : "Tạo mã liên kết"}
                                </Button>
                            </div>
                        </div>
                    )}
                </section>

                <section className="cm-student-section">
                    <div className="cm-student-section-heading">
                        <div>
                            <span>TRUY CẬP NHANH</span>
                            <h2>Bạn muốn làm gì?</h2>
                        </div>
                    </div>

                    <div className="cm-student-quick-grid">
                        <button onClick={() => nav("/student/courses")}>
                            <span>▤</span>
                            <div>
                                <strong>Khóa học của tôi</strong>
                                <small>Xem nội dung và tiến độ học</small>
                            </div>
                            <b>→</b>
                        </button>

                        <button onClick={() => nav("/student/assignments")}>
                            <span>✎</span>
                            <div>
                                <strong>Bài tập</strong>
                                <small>Làm bài và xem kết quả</small>
                            </div>
                            <b>→</b>
                        </button>

                        <button onClick={() => nav("/student/schedule")}>
                            <span>◷</span>
                            <div>
                                <strong>Lịch học</strong>
                                <small>Theo dõi lịch học của bạn</small>
                            </div>
                            <b>→</b>
                        </button>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default Dashboard;