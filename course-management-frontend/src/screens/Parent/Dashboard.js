import { useEffect, useState } from "react";
import {
    Alert, Badge, Button, Card, Col, Form, Modal,
    Nav, ProgressBar, Row, Table
} from "react-bootstrap";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Dashboard = () => {
    const [students, setStudents] = useState([]);
    const [selectedId, setSelectedId] = useState(null);

    const [dashboard, setDashboard] = useState(null);
    const [progress, setProgress] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [attendance, setAttendance] = useState([]);

    const [assignmentPage, setAssignmentPage] = useState(1);
    const [assignmentKeyword, setAssignmentKeyword] = useState("");
    const [assignmentStatus, setAssignmentStatus] = useState("ALL");
    const [assignmentMeta, setAssignmentMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const [attendancePage, setAttendancePage] = useState(1);
    const [attendanceMeta, setAttendanceMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const [activeTab, setActiveTab] = useState("overview");
    const [loading, setLoading] = useState(true);
    const [detailLoading, setDetailLoading] = useState(false);
    const [progressLoading, setProgressLoading] = useState(false);
    const [assignmentLoading, setAssignmentLoading] = useState(false);
    const [attendanceLoading, setAttendanceLoading] = useState(false);
    const [progressLoaded, setProgressLoaded] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const [showLink, setShowLink] = useState(false);
    const [linkCode, setLinkCode] = useState("");
    const [linking, setLinking] = useState(false);
    const [unlinking, setUnlinking] = useState(false);

    const selectedStudent = students.find(s => Number(s.studentId) === Number(selectedId));

    const readPage = (data, key) => ({
        items: Array.isArray(data?.[key]) ? data[key] : (Array.isArray(data) ? data : []),
        currentPage: Number(data?.currentPage || 1),
        totalPages: Number(data?.totalPages || 1),
        totalRecords: Number(data?.totalRecords ?? (Array.isArray(data) ? data.length : 0))
    });

    const loadStudents = async preferredId => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.parentStudents);
            const data = Array.isArray(res.data) ? res.data : [];

            setStudents(data);

            if (!data.length) {
                setSelectedId(null);
                setDashboard(null);
                return;
            }

            const exists = data.some(s => Number(s.studentId) === Number(preferredId));
            setSelectedId(exists ? Number(preferredId) : data[0].studentId);
        } catch (ex) {
            console.error("Load parent students error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách học viên!");
        } finally {
            setLoading(false);
        }
    };

    const loadDashboard = async studentId => {
        if (!studentId) return;

        try {
            setDetailLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.parentStudentDashboard(studentId));
            setDashboard(res.data);
        } catch (ex) {
            console.error("Load parent dashboard error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải tổng quan học tập!");
        } finally {
            setDetailLoading(false);
        }
    };

    const loadProgress = async studentId => {
        if (!studentId) return;

        try {
            setProgressLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.parentStudentProgress(studentId));
            setProgress(Array.isArray(res.data) ? res.data : []);
            setProgressLoaded(true);
        } catch (ex) {
            console.error("Load parent progress error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải tiến độ học tập!");
        } finally {
            setProgressLoading(false);
        }
    };

    const loadAssignments = async studentId => {
        if (!studentId) return;

        try {
            setAssignmentLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.parentStudentAssignments(studentId), {
                params: {
                    page: assignmentPage,
                    ...(assignmentKeyword.trim() ? { kw: assignmentKeyword.trim() } : {}),
                    ...(assignmentStatus !== "ALL" ? { status: assignmentStatus } : {})
                }
            });

            const pageData = readPage(res.data, "assignments");
            setAssignments(pageData.items);
            setAssignmentMeta({
                currentPage: pageData.currentPage,
                totalPages: pageData.totalPages,
                totalRecords: pageData.totalRecords
            });

            if (pageData.currentPage !== assignmentPage)
                setAssignmentPage(pageData.currentPage);
        } catch (ex) {
            console.error("Load parent assignments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải bài tập!");
        } finally {
            setAssignmentLoading(false);
        }
    };

    const loadAttendance = async studentId => {
        if (!studentId) return;

        try {
            setAttendanceLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.parentStudentAttendance(studentId), {
                params: { page: attendancePage }
            });

            const pageData = readPage(res.data, "attendance");
            setAttendance(pageData.items);
            setAttendanceMeta({
                currentPage: pageData.currentPage,
                totalPages: pageData.totalPages,
                totalRecords: pageData.totalRecords
            });

            if (pageData.currentPage !== attendancePage)
                setAttendancePage(pageData.currentPage);
        } catch (ex) {
            console.error("Load parent attendance error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải điểm danh!");
        } finally {
            setAttendanceLoading(false);
        }
    };

    useEffect(() => {
        loadStudents();
    }, []);

    useEffect(() => {
        if (!selectedId) return;

        setActiveTab("overview");
        setDashboard(null);
        setProgress([]);
        setAssignments([]);
        setAttendance([]);
        setProgressLoaded(false);
        setAssignmentPage(1);
        setAssignmentKeyword("");
        setAssignmentStatus("ALL");
        setAssignmentMeta({ currentPage: 1, totalPages: 1, totalRecords: 0 });
        setAttendancePage(1);
        setAttendanceMeta({ currentPage: 1, totalPages: 1, totalRecords: 0 });
        loadDashboard(selectedId);
    }, [selectedId]);

    useEffect(() => {
        if (activeTab === "progress" && selectedId && !progressLoaded)
            loadProgress(selectedId);
    }, [activeTab, selectedId, progressLoaded]);

    useEffect(() => {
        if (activeTab !== "assignments" || !selectedId) return;

        const timer = setTimeout(() => loadAssignments(selectedId), 300);
        return () => clearTimeout(timer);
    }, [activeTab, selectedId, assignmentPage, assignmentKeyword, assignmentStatus]);

    useEffect(() => {
        if (activeTab === "attendance" && selectedId)
            loadAttendance(selectedId);
    }, [activeTab, selectedId, attendancePage]);

    const linkStudent = async e => {
        e.preventDefault();

        if (!linkCode.trim())
            return setErr("Vui lòng nhập mã liên kết!");

        try {
            setLinking(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(endpoints.parentLinks, {
                verificationCode: linkCode.trim().toUpperCase()
            });

            setShowLink(false);
            setLinkCode("");
            setSuccess(`Đã liên kết với ${res.data.fullName} thành công.`);
            await loadStudents(res.data.studentId);
        } catch (ex) {
            console.error("Link student error:", ex);
            setErr(ex.response?.data?.message || "Không thể liên kết học viên!");
        } finally {
            setLinking(false);
        }
    };

    const unlinkStudent = async () => {
        if (!selectedStudent) return;

        if (!window.confirm(`Hủy liên kết với ${selectedStudent.fullName}?`))
            return;

        try {
            setUnlinking(true);
            setErr("");
            setSuccess("");

            await authApis().delete(endpoints.parentUnlinkStudent(selectedStudent.linkId));
            setSuccess(`Đã hủy liên kết với ${selectedStudent.fullName}.`);
            await loadStudents();
        } catch (ex) {
            console.error("Unlink student error:", ex);
            setErr(ex.response?.data?.message || "Không thể hủy liên kết!");
        } finally {
            setUnlinking(false);
        }
    };

    const formatDate = value => value
        ? new Date(value).toLocaleDateString("vi-VN")
        : "-";

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit", day: "2-digit",
            month: "2-digit", year: "numeric"
        })
        : "-";

    const score = value => value == null ? "-" : Number(value).toLocaleString("vi-VN");

    const pathBadge = status => {
        if (status === "IN_PROGRESS") return <Badge bg="primary">Đang học</Badge>;
        if (status === "COMPLETED") return <Badge bg="success">Hoàn thành</Badge>;
        if (status === "PAUSED") return <Badge bg="warning" text="dark">Tạm dừng</Badge>;
        return <Badge bg="secondary">Chưa có lộ trình</Badge>;
    };

    const assignmentBadge = status => {
        if (status === "AVAILABLE") return <Badge bg="success">Có thể làm</Badge>;
        if (status === "COMPLETED") return <Badge bg="primary">Hoàn thành</Badge>;
        if (status === "LOCKED") return <Badge bg="secondary">Đã khóa</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const attemptBadge = status => {
        if (status === "IN_PROGRESS") return <Badge bg="warning" text="dark">Đang làm</Badge>;
        if (["SUBMITTED", "PENDING_GRADING"].includes(status))
            return <Badge bg="info">Chờ chấm</Badge>;
        if (status === "GRADED") return <Badge bg="success">Đã chấm</Badge>;
        return null;
    };

    const attendanceBadge = item => {
        if (item.attendanceStatus === "PRESENT") return <Badge bg="success">Có mặt</Badge>;
        if (item.attendanceStatus === "ABSENT") return <Badge bg="danger">Vắng</Badge>;
        if (item.sessionStatus === "UPCOMING") return <Badge bg="secondary">Chưa diễn ra</Badge>;
        if (item.sessionStatus === "ONGOING") return <Badge bg="info">Đang diễn ra</Badge>;
        return <Badge bg="warning" text="dark">Chưa ghi nhận</Badge>;
    };

    const progressPercent = item => !item.totalDetails
        ? 0
        : Math.min(Math.round(item.completedDetails / item.totalDetails * 100), 100);

    const buildPages = (totalPages, currentPage) => {
        if (totalPages <= 5)
            return Array.from({ length: totalPages }, (_, index) => index + 1);

        const pages = [1];
        const start = Math.max(2, currentPage - 1);
        const end = Math.min(totalPages - 1, currentPage + 1);

        if (start > 2) pages.push("left-dots");
        for (let item = start; item <= end; item++) pages.push(item);
        if (end < totalPages - 1) pages.push("right-dots");
        pages.push(totalPages);

        return pages;
    };

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <div className="cm-portal-heading">
                    <div>
                        <span>PHỤ HUYNH</span>
                        <h1>Theo dõi học tập</h1>
                        <p>Theo dõi tiến độ, bài tập và tình hình học tập của học viên.</p>
                    </div>

                    <Button onClick={() => setShowLink(true)}>+ Liên kết học viên</Button>
                </div>

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                {success && (
                    <Alert variant="success" dismissible onClose={() => setSuccess("")}>
                        {success}
                    </Alert>
                )}

                {students.length === 0 ? (
                    <Card className="cm-portal-card">
                        <Card.Body className="p-5 text-center">
                            <div className="cm-portal-empty">
                                <h5>Chưa có học viên được liên kết</h5>
                                <p className="cm-portal-muted">
                                    Nhập mã liên kết do học viên cung cấp để bắt đầu theo dõi.
                                </p>

                                <Button className="mt-2" onClick={() => setShowLink(true)}>
                                    Liên kết học viên
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>
                ) : (
                    <>
                        <Card className="cm-portal-card mb-4">
                            <Card.Body className="p-4">
                                <Row className="align-items-end g-3">
                                    <Col md={8}>
                                        <Form.Label>Học viên đang theo dõi</Form.Label>

                                        <Form.Select value={selectedId || ""}
                                            onChange={e => {
                                                setActiveTab("overview");
                                                setSelectedId(Number(e.target.value));
                                            }}>
                                            {students.map(student => (
                                                <option key={student.studentId} value={student.studentId}>
                                                    {student.fullName} (@{student.username})
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Col>

                                    <Col md={4} className="text-md-end">
                                        <Button variant="outline-danger" disabled={unlinking}
                                            onClick={unlinkStudent}>
                                            {unlinking ? "Đang hủy..." : "Hủy liên kết"}
                                        </Button>
                                    </Col>
                                </Row>

                                {selectedStudent && (
                                    <div className="cm-portal-muted mt-3">
                                        Liên kết từ {formatDate(selectedStudent.linkedAt)}
                                    </div>
                                )}
                            </Card.Body>
                        </Card>

                        {detailLoading ? (
                            <div className="text-center py-5"><MySpinner /></div>
                        ) : dashboard && (
                            <Card className="cm-portal-card">
                                <Nav className="cm-portal-tabs" activeKey={activeTab} onSelect={setActiveTab}>
                                    <Nav.Item><Nav.Link eventKey="overview">Tổng quan</Nav.Link></Nav.Item>
                                    <Nav.Item><Nav.Link eventKey="progress">Tiến độ</Nav.Link></Nav.Item>
                                    <Nav.Item><Nav.Link eventKey="assignments">Bài tập</Nav.Link></Nav.Item>
                                    <Nav.Item><Nav.Link eventKey="attendance">Điểm danh</Nav.Link></Nav.Item>
                                </Nav>

                                <Card.Body className="p-4">
                                    {activeTab === "overview" && (
                                        <>
                                            <div className="cm-portal-section-heading">
                                                <span>TỔNG QUAN</span>
                                                <h5>{dashboard.studentName}</h5>
                                                <p>Tổng quan tình hình học tập hiện tại.</p>
                                            </div>

                                            <Row className="g-3 mb-4">
                                                <Col lg={3} md={6}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Khóa học đang học</span>
                                                        <strong>{dashboard.activeCourses || 0}</strong>
                                                    </div>
                                                </Col>

                                                <Col lg={3} md={6}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Lộ trình đang học</span>
                                                        <strong>{dashboard.inProgressLearningPaths || 0}</strong>
                                                    </div>
                                                </Col>

                                                <Col lg={3} md={6}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Bài đang làm</span>
                                                        <strong>{dashboard.inProgressAssignments || 0}</strong>
                                                    </div>
                                                </Col>

                                                <Col lg={3} md={6}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Điểm gần nhất</span>
                                                        <strong>{score(dashboard.latestScore)}</strong>
                                                    </div>
                                                </Col>
                                            </Row>

                                            <Row className="g-3 mb-4">
                                                <Col md={4}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Có mặt</span>
                                                        <strong>{dashboard.presentSessions || 0}</strong>
                                                    </div>
                                                </Col>

                                                <Col md={4}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Vắng</span>
                                                        <strong>{dashboard.absentSessions || 0}</strong>
                                                    </div>
                                                </Col>

                                                <Col md={4}>
                                                    <div className="cm-portal-summary h-100">
                                                        <span>Chưa ghi nhận</span>
                                                        <strong>{dashboard.notMarkedSessions || 0}</strong>
                                                    </div>
                                                </Col>
                                            </Row>

                                            <Row className="g-3">
                                                <Col md={6}>
                                                    <Card className="cm-portal-card h-100">
                                                        <Card.Body className="p-4">
                                                            <span className="cm-portal-label">LỘ TRÌNH</span>
                                                            <h5 className="cm-portal-title mt-2">
                                                                Đã hoàn thành
                                                            </h5>
                                                            <strong className="fs-2">
                                                                {dashboard.completedLearningPaths || 0}
                                                            </strong>
                                                        </Card.Body>
                                                    </Card>
                                                </Col>

                                                <Col md={6}>
                                                    <Card className="cm-portal-card h-100">
                                                        <Card.Body className="p-4">
                                                            <span className="cm-portal-label">BÀI TẬP</span>
                                                            <h5 className="cm-portal-title mt-2">
                                                                Đang chờ chấm
                                                            </h5>
                                                            <strong className="fs-2">
                                                                {dashboard.pendingGradingAssignments || 0}
                                                            </strong>
                                                        </Card.Body>
                                                    </Card>
                                                </Col>
                                            </Row>

                                            {dashboard.continueAssignment && (
                                                <Card className="cm-portal-card mt-4">
                                                    <Card.Body className="p-4">
                                                        <span className="cm-portal-label">ĐANG LÀM DỞ</span>

                                                        <h5 className="cm-portal-title mt-2 mb-1">
                                                            {dashboard.continueAssignment.assignmentName}
                                                        </h5>

                                                        <div className="cm-portal-muted">
                                                            {dashboard.continueAssignment.courseName}
                                                            {" · "}
                                                            Lần làm #{dashboard.continueAssignment.attemptNumber}
                                                        </div>

                                                        <div className="mt-3">
                                                            {attemptBadge(dashboard.continueAssignment.attemptStatus)}
                                                        </div>
                                                    </Card.Body>
                                                </Card>
                                            )}
                                        </>
                                    )}

                                    {activeTab === "progress" && (
                                        <>
                                            <div className="cm-portal-section-heading">
                                                <span>TIẾN ĐỘ</span>
                                                <h5>Tiến độ học tập</h5>
                                                <p>Theo dõi lộ trình của từng khóa học.</p>
                                            </div>

                                            {progressLoading ? (
                                                <div className="text-center py-4"><MySpinner /></div>
                                            ) : progress.length === 0 ? (
                                                <div className="cm-portal-empty">
                                                    Chưa có dữ liệu tiến độ.
                                                </div>
                                            ) : (
                                                <Row className="g-3">
                                                    {progress.map(item => {
                                                        const percent = progressPercent(item);

                                                        return (
                                                            <Col xs={12} key={item.classId}>
                                                                <Card className="cm-portal-card">
                                                                    <Card.Body className="p-4">
                                                                        <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
                                                                            <div>
                                                                                <span className="cm-portal-label">
                                                                                    {item.courseName}
                                                                                </span>

                                                                                <h5 className="cm-portal-title mt-2 mb-1">
                                                                                    {item.className}
                                                                                </h5>

                                                                                <small className="cm-portal-muted">
                                                                                    {item.learningPathName || "Chưa có lộ trình"}
                                                                                </small>
                                                                            </div>

                                                                            {pathBadge(item.learningPathStatus)}
                                                                        </div>

                                                                        <div className="d-flex justify-content-between mb-2">
                                                                            <span className="cm-portal-muted">
                                                                                Tiến độ
                                                                            </span>

                                                                            <strong>
                                                                                {item.completedDetails}/{item.totalDetails}
                                                                            </strong>
                                                                        </div>

                                                                        <ProgressBar now={percent}
                                                                            className="cm-portal-progress" />

                                                                        {item.currentAssignmentName && (
                                                                            <div className="cm-portal-muted mt-3">
                                                                                Bài hiện tại:{" "}
                                                                                <strong>{item.currentAssignmentName}</strong>
                                                                            </div>
                                                                        )}
                                                                    </Card.Body>
                                                                </Card>
                                                            </Col>
                                                        );
                                                    })}
                                                </Row>
                                            )}
                                        </>
                                    )}

                                    {activeTab === "assignments" && (
                                        <>
                                            <div className="cm-portal-section-heading">
                                                <span>BÀI TẬP</span>
                                                <h5>Bài tập của học viên</h5>
                                                <p>Theo dõi tình trạng làm bài và kết quả.</p>
                                            </div>

                                            <Card className="cm-portal-card mb-3">
                                                <Card.Body className="p-3">
                                                    <Row className="g-2">
                                                        <Col md={8}>
                                                            <Form.Control value={assignmentKeyword}
                                                                onChange={e => {
                                                                    setAssignmentKeyword(e.target.value);
                                                                    setAssignmentPage(1);
                                                                }}
                                                                placeholder="Tìm theo tên bài tập..."
                                                            />
                                                        </Col>

                                                        <Col md={4}>
                                                            <Form.Select value={assignmentStatus}
                                                                onChange={e => {
                                                                    setAssignmentStatus(e.target.value);
                                                                    setAssignmentPage(1);
                                                                }}>
                                                                <option value="ALL">Tất cả trạng thái</option>
                                                                <option value="AVAILABLE">Có thể làm</option>
                                                                <option value="LOCKED">Đã khóa</option>
                                                                <option value="COMPLETED">Hoàn thành</option>
                                                            </Form.Select>
                                                        </Col>
                                                    </Row>
                                                </Card.Body>
                                            </Card>

                                            {assignmentLoading ? (
                                                <div className="text-center py-4"><MySpinner /></div>
                                            ) : assignments.length === 0 ? (
                                                <div className="cm-portal-empty">
                                                    {assignmentKeyword.trim() || assignmentStatus !== "ALL"
                                                        ? "Không tìm thấy bài tập phù hợp."
                                                        : "Học viên chưa có bài tập."}
                                                </div>
                                            ) : (
                                                <>
                                                    <div className="table-responsive">
                                                        <Table hover align="middle" className="cm-portal-table">
                                                            <thead>
                                                                <tr>
                                                                    <th>Bài tập</th>
                                                                    <th>Khóa học</th>
                                                                    <th>Trạng thái</th>
                                                                    <th>Lần làm</th>
                                                                    <th>Kết quả</th>
                                                                    <th>Hạn nộp</th>
                                                                </tr>
                                                            </thead>

                                                            <tbody>
                                                                {assignments.map(item => (
                                                                    <tr key={item.assignedAssignmentId}>
                                                                        <td>
                                                                            <strong className="d-block">
                                                                                {item.assignmentName}
                                                                            </strong>

                                                                            <small className="cm-portal-muted">
                                                                                {item.assignmentType} · {score(item.maximumScore)} điểm
                                                                            </small>
                                                                        </td>

                                                                        <td>{item.courseName}</td>
                                                                        <td>{assignmentBadge(item.status)}</td>

                                                                        <td>
                                                                            {item.latestAttemptNumber ? (
                                                                                <>
                                                                                    <div>#{item.latestAttemptNumber}</div>
                                                                                    {attemptBadge(item.latestAttemptStatus)}
                                                                                </>
                                                                            ) : (
                                                                                <span className="cm-portal-muted">
                                                                                    Chưa làm
                                                                                </span>
                                                                            )}
                                                                        </td>

                                                                        <td>
                                                                            {item.totalScore != null ? (
                                                                                <>
                                                                                    <strong>{score(item.totalScore)}</strong>

                                                                                    {item.passed != null && (
                                                                                        <div className="mt-1">
                                                                                            <Badge bg={item.passed ? "success" : "danger"}>
                                                                                                {item.passed ? "Đạt" : "Chưa đạt"}
                                                                                            </Badge>
                                                                                        </div>
                                                                                    )}
                                                                                </>
                                                                            ) : (
                                                                                <span className="cm-portal-muted">-</span>
                                                                            )}
                                                                        </td>

                                                                        <td>{formatDateTime(item.dueAt)}</td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </Table>
                                                    </div>

                                                    <div className="cm-class-pagination mt-3">
                                                        <span>
                                                            Tổng <b>{assignmentMeta.totalRecords}</b> · Trang{" "}
                                                            {assignmentMeta.currentPage}/{assignmentMeta.totalPages}
                                                        </span>

                                                        {assignmentMeta.totalPages > 1 && (
                                                            <div className="cm-class-pagination-buttons">
                                                                <button type="button"
                                                                    disabled={assignmentMeta.currentPage === 1}
                                                                    onClick={() => setAssignmentPage(assignmentMeta.currentPage - 1)}>
                                                                    ‹
                                                                </button>

                                                                {buildPages(
                                                                    assignmentMeta.totalPages,
                                                                    assignmentMeta.currentPage
                                                                ).map((item, index) =>
                                                                    typeof item === "number" ? (
                                                                        <button type="button" key={item}
                                                                            className={item === assignmentMeta.currentPage ? "active" : ""}
                                                                            onClick={() => setAssignmentPage(item)}>
                                                                            {item}
                                                                        </button>
                                                                    ) : (
                                                                        <span key={`${item}-${index}`}>...</span>
                                                                    )
                                                                )}

                                                                <button type="button"
                                                                    disabled={assignmentMeta.currentPage === assignmentMeta.totalPages}
                                                                    onClick={() => setAssignmentPage(assignmentMeta.currentPage + 1)}>
                                                                    ›
                                                                </button>
                                                            </div>
                                                        )}
                                                    </div>
                                                </>
                                            )}
                                        </>
                                    )}

                                    {activeTab === "attendance" && (
                                        <>
                                            <div className="cm-portal-section-heading">
                                                <span>ĐIỂM DANH</span>
                                                <h5>Lịch sử điểm danh</h5>
                                                <p>Theo dõi tình trạng tham gia các buổi học.</p>
                                            </div>

                                            {attendanceLoading ? (
                                                <div className="text-center py-4"><MySpinner /></div>
                                            ) : attendance.length === 0 ? (
                                                <div className="cm-portal-empty">
                                                    Chưa có dữ liệu điểm danh.
                                                </div>
                                            ) : (
                                                <>
                                                    <div className="table-responsive">
                                                        <Table hover align="middle" className="cm-portal-table">
                                                            <thead>
                                                                <tr>
                                                                    <th>Buổi học</th>
                                                                    <th>Khóa học</th>
                                                                    <th>Thời gian</th>
                                                                    <th>Điểm danh</th>
                                                                    <th>Ghi chú</th>
                                                                </tr>
                                                            </thead>

                                                            <tbody>
                                                                {attendance.map(item => (
                                                                    <tr key={item.sessionId}>
                                                                        <td>
                                                                            <strong className="d-block">
                                                                                {item.sessionTitle}
                                                                            </strong>

                                                                            <small className="cm-portal-muted">
                                                                                {item.className}
                                                                            </small>
                                                                        </td>

                                                                        <td>{item.courseName}</td>

                                                                        <td>
                                                                            <div>{formatDate(item.startTime)}</div>
                                                                            <small className="cm-portal-muted">
                                                                                {new Date(item.startTime).toLocaleTimeString("vi-VN", {
                                                                                    hour: "2-digit", minute: "2-digit"
                                                                                })}
                                                                            </small>
                                                                        </td>

                                                                        <td>{attendanceBadge(item)}</td>
                                                                        <td>{item.note || "-"}</td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </Table>
                                                    </div>

                                                    <div className="cm-class-pagination mt-3">
                                                        <span>
                                                            Tổng <b>{attendanceMeta.totalRecords}</b> · Trang{" "}
                                                            {attendanceMeta.currentPage}/{attendanceMeta.totalPages}
                                                        </span>

                                                        {attendanceMeta.totalPages > 1 && (
                                                            <div className="cm-class-pagination-buttons">
                                                                <button type="button"
                                                                    disabled={attendanceMeta.currentPage === 1}
                                                                    onClick={() => setAttendancePage(attendanceMeta.currentPage - 1)}>
                                                                    ‹
                                                                </button>

                                                                {buildPages(
                                                                    attendanceMeta.totalPages,
                                                                    attendanceMeta.currentPage
                                                                ).map((item, index) =>
                                                                    typeof item === "number" ? (
                                                                        <button type="button" key={item}
                                                                            className={item === attendanceMeta.currentPage ? "active" : ""}
                                                                            onClick={() => setAttendancePage(item)}>
                                                                            {item}
                                                                        </button>
                                                                    ) : (
                                                                        <span key={`${item}-${index}`}>...</span>
                                                                    )
                                                                )}

                                                                <button type="button"
                                                                    disabled={attendanceMeta.currentPage === attendanceMeta.totalPages}
                                                                    onClick={() => setAttendancePage(attendanceMeta.currentPage + 1)}>
                                                                    ›
                                                                </button>
                                                            </div>
                                                        )}
                                                    </div>
                                                </>
                                            )}
                                        </>
                                    )}
                                </Card.Body>
                            </Card>
                        )}
                    </>
                )}

                <Modal show={showLink} onHide={() => !linking && setShowLink(false)} centered>
                    <Form onSubmit={linkStudent}>
                        <Modal.Header closeButton>
                            <Modal.Title>Liên kết học viên</Modal.Title>
                        </Modal.Header>

                        <Modal.Body>
                            <Form.Group>
                                <Form.Label>Mã liên kết</Form.Label>

                                <Form.Control value={linkCode}
                                    onChange={e => setLinkCode(e.target.value.toUpperCase())}
                                    placeholder="Nhập mã liên kết 8 ký tự"
                                    maxLength={8}
                                    autoFocus
                                />

                                <Form.Text className="text-muted">
                                    Nhập mã liên kết do tài khoản học viên cung cấp.
                                </Form.Text>
                            </Form.Group>
                        </Modal.Body>

                        <Modal.Footer>
                            <Button variant="outline-secondary"
                                onClick={() => setShowLink(false)} disabled={linking}>
                                Hủy
                            </Button>

                            <Button type="submit" disabled={linking}>
                                {linking ? "Đang liên kết..." : "Liên kết"}
                            </Button>
                        </Modal.Footer>
                    </Form>
                </Modal>
            </div>
        </div>
    );
};

export default Dashboard;