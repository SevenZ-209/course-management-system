import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Nav, ProgressBar, Row, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import ClassAssignments from "./ClassAssignments";
import "../../styles/Portal.css";

const ClassDetail = () => {
    const { classId } = useParams();
    const nav = useNavigate();

    const [classInfo, setClassInfo] = useState(null);
    const [students, setStudents] = useState([]);
    const [progress, setProgress] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [activeTab, setActiveTab] = useState("students");

    const [studentKeyword, setStudentKeyword] = useState("");
    const [studentPage, setStudentPage] = useState(1);
    const [studentMeta, setStudentMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const [progressKeyword, setProgressKeyword] = useState("");
    const [progressStatus, setProgressStatus] = useState("ALL");
    const [progressPage, setProgressPage] = useState(1);
    const [progressMeta, setProgressMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const [loading, setLoading] = useState(true);
    const [studentLoading, setStudentLoading] = useState(false);
    const [progressLoading, setProgressLoading] = useState(false);
    const [err, setErr] = useState("");
    const [studentErr, setStudentErr] = useState("");
    const [progressErr, setProgressErr] = useState("");

    const readPage = (data, key) => {
        if (Array.isArray(data))
            return { items: data, currentPage: 1, totalPages: 1, totalRecords: data.length };

        return {
            items: Array.isArray(data?.[key]) ? data[key] : [],
            currentPage: Number(data?.currentPage || 1),
            totalPages: Number(data?.totalPages || 1),
            totalRecords: Number(data?.totalRecords || 0)
        };
    };

    const loadBaseData = async () => {
        try {
            setLoading(true);
            setErr("");

            const [classRes, sessionRes] = await Promise.all([
                authApis().get(endpoints.teacherClassDetail(classId)),
                authApis().get(endpoints.teacherClassSessions(classId))
            ]);

            setClassInfo(classRes.data);
            setSessions(Array.isArray(sessionRes.data) ? sessionRes.data : []);
        } catch (ex) {
            console.error("Load teacher class detail error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải thông tin lớp học!");
        } finally {
            setLoading(false);
        }
    };

    const loadStudents = async () => {
        try {
            setStudentLoading(true);
            setStudentErr("");

            const res = await authApis().get(endpoints.teacherClassStudents(classId), {
                params: {
                    page: studentPage,
                    ...(studentKeyword.trim() ? { kw: studentKeyword.trim() } : {})
                }
            });

            const pageData = readPage(res.data, "students");
            setStudents(pageData.items);
            setStudentMeta({
                currentPage: pageData.currentPage,
                totalPages: pageData.totalPages,
                totalRecords: pageData.totalRecords
            });

            if (pageData.currentPage !== studentPage)
                setStudentPage(pageData.currentPage);
        } catch (ex) {
            console.error("Load teacher students error:", ex);
            setStudentErr(ex.response?.data?.message || "Không thể tải danh sách học viên!");
        } finally {
            setStudentLoading(false);
        }
    };

    const loadProgress = async () => {
        try {
            setProgressLoading(true);
            setProgressErr("");

            const res = await authApis().get(endpoints.teacherClassProgress(classId), {
                params: {
                    page: progressPage,
                    ...(progressKeyword.trim() ? { kw: progressKeyword.trim() } : {}),
                    ...(progressStatus !== "ALL" ? { status: progressStatus } : {})
                }
            });

            const pageData = readPage(res.data, "progress");
            setProgress(pageData.items);
            setProgressMeta({
                currentPage: pageData.currentPage,
                totalPages: pageData.totalPages,
                totalRecords: pageData.totalRecords
            });

            if (pageData.currentPage !== progressPage)
                setProgressPage(pageData.currentPage);
        } catch (ex) {
            console.error("Load teacher progress error:", ex);
            setProgressErr(ex.response?.data?.message || "Không thể tải tiến độ học tập!");
        } finally {
            setProgressLoading(false);
        }
    };

    useEffect(() => {
        loadBaseData();
    }, [classId]);

    useEffect(() => {
        const timer = setTimeout(loadStudents, 300);
        return () => clearTimeout(timer);
    }, [classId, studentPage, studentKeyword]);

    useEffect(() => {
        const timer = setTimeout(loadProgress, 300);
        return () => clearTimeout(timer);
    }, [classId, progressPage, progressKeyword, progressStatus]);

    const buildPages = (totalPages, currentPage) => {
        if (totalPages <= 5)
            return Array.from({ length: totalPages }, (_, index) => index + 1);

        const pages = [1];
        const start = Math.max(2, currentPage - 1);
        const end = Math.min(totalPages - 1, currentPage + 1);

        if (start > 2) pages.push("left-dots");
        for (let page = start; page <= end; page++) pages.push(page);
        if (end < totalPages - 1) pages.push("right-dots");
        pages.push(totalPages);

        return pages;
    };

    const renderPagination = (meta, setPage) => {
        if (!meta.totalRecords) return null;

        return (
            <div className="cm-class-pagination mt-3">
                <span>Tổng <b>{meta.totalRecords}</b> · Trang {meta.currentPage}/{meta.totalPages}</span>

                {meta.totalPages > 1 && (
                    <div className="cm-class-pagination-buttons">
                        <button type="button" disabled={meta.currentPage === 1}
                            onClick={() => setPage(meta.currentPage - 1)}>‹</button>

                        {buildPages(meta.totalPages, meta.currentPage).map((page, index) =>
                            typeof page === "number" ? (
                                <button type="button" key={page}
                                    className={page === meta.currentPage ? "active" : ""}
                                    onClick={() => setPage(page)}>
                                    {page}
                                </button>
                            ) : (
                                <span key={`${page}-${index}`}>...</span>
                            )
                        )}

                        <button type="button" disabled={meta.currentPage === meta.totalPages}
                            onClick={() => setPage(meta.currentPage + 1)}>›</button>
                    </div>
                )}
            </div>
        );
    };

    const formatDate = value => value ? new Date(value).toLocaleDateString("vi-VN") : "-";

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit", day: "2-digit",
            month: "2-digit", year: "numeric"
        })
        : "-";

    const formatTime = value => value
        ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
        : "-";

    const classStatus = status => {
        if (status === "ACTIVE") return <Badge bg="success">Đang hoạt động</Badge>;
        if (status === "UPCOMING") return <Badge bg="primary">Sắp bắt đầu</Badge>;
        if (status === "COMPLETED") return <Badge bg="secondary">Đã kết thúc</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const pathStatus = status => {
        if (status === "IN_PROGRESS") return <Badge bg="primary">Đang học</Badge>;
        if (status === "COMPLETED") return <Badge bg="success">Hoàn thành</Badge>;
        if (status === "PAUSED") return <Badge bg="warning" text="dark">Tạm dừng</Badge>;
        return <Badge bg="secondary">Chưa có lộ trình</Badge>;
    };

    const sessionStatus = status => {
        if (status === "ONGOING") return <Badge bg="success">Đang diễn ra</Badge>;
        if (status === "UPCOMING") return <Badge bg="primary">Sắp diễn ra</Badge>;
        if (status === "ENDED") return <Badge bg="secondary">Đã kết thúc</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const progressPercent = item => !item.totalDetails
        ? 0
        : Math.min(Math.round((item.completedDetails / item.totalDetails) * 100), 100);

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    if (err)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container">
                    <Alert variant="danger">{err}</Alert>
                    <Button variant="outline-secondary" onClick={() => nav("/teacher/classes")}>
                        ← Lớp học của tôi
                    </Button>
                </div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <Button variant="outline-secondary" size="sm" className="cm-portal-back"
                    onClick={() => nav("/teacher/classes")}>
                    ← Lớp học của tôi
                </Button>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-4">
                        <Row className="align-items-center g-4">
                            <Col md={8}>
                                <span className="cm-portal-label">{classInfo?.courseName}</span>
                                <h2 className="cm-portal-title mt-2 mb-2">{classInfo?.className}</h2>
                                <div className="cm-portal-muted">
                                    {formatDate(classInfo?.startDate)} → {formatDate(classInfo?.endDate)}
                                </div>
                            </Col>

                            <Col md={4}>
                                <Row className="g-2">
                                    <Col xs={6}>
                                        <div className="cm-portal-summary">
                                            <span>Học viên</span>
                                            <strong>
                                                {classInfo?.studentCount || 0}
                                                <small className="fs-6 fw-normal">/{classInfo?.maxStudents || 0}</small>
                                            </strong>
                                        </div>
                                    </Col>

                                    <Col xs={6}>
                                        <div className="cm-portal-summary h-100">
                                            <span>Trạng thái</span>
                                            <div className="mt-2">{classStatus(classInfo?.status)}</div>
                                        </div>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="cm-portal-card">
                    <Nav className="cm-portal-tabs" activeKey={activeTab} onSelect={setActiveTab}>
                        <Nav.Item><Nav.Link eventKey="students">Học viên</Nav.Link></Nav.Item>
                        <Nav.Item><Nav.Link eventKey="progress">Tiến độ</Nav.Link></Nav.Item>
                        <Nav.Item><Nav.Link eventKey="sessions">Buổi học</Nav.Link></Nav.Item>
                        <Nav.Item><Nav.Link eventKey="assignments">Bài tập</Nav.Link></Nav.Item>
                    </Nav>

                    <Card.Body className="p-4">
                        {activeTab === "students" && (
                            <>
                                <div className="cm-portal-section-heading">
                                    <span>HỌC VIÊN</span>
                                    <h5>Danh sách học viên</h5>
                                    <p>Các học viên đang hoạt động trong lớp.</p>
                                </div>

                                <div className="mb-3">
                                    <Form.Control value={studentKeyword}
                                        onChange={e => {
                                            setStudentKeyword(e.target.value);
                                            setStudentPage(1);
                                        }}
                                        placeholder="Tìm theo tên hoặc tài khoản học viên..."
                                    />
                                </div>

                                {studentErr && <Alert variant="danger">{studentErr}</Alert>}

                                {studentLoading ? (
                                    <div className="text-center py-4"><MySpinner /></div>
                                ) : students.length === 0 ? (
                                    <div className="cm-portal-empty">
                                        {studentKeyword.trim() ? "Không tìm thấy học viên phù hợp." : "Lớp chưa có học viên."}
                                    </div>
                                ) : (
                                    <>
                                        <div className="table-responsive">
                                            <Table hover align="middle" className="cm-portal-table">
                                                <thead>
                                                    <tr>
                                                        <th>#</th>
                                                        <th>Học viên</th>
                                                        <th>Tài khoản</th>
                                                        <th>Trạng thái</th>
                                                        <th>Ngày tham gia</th>
                                                    </tr>
                                                </thead>

                                                <tbody>
                                                    {students.map((student, index) => (
                                                        <tr key={student.studentId}>
                                                            <td>{index + 1}</td>
                                                            <td><strong>{student.fullName}</strong></td>
                                                            <td>@{student.username}</td>
                                                            <td><Badge bg="success">Đang học</Badge></td>
                                                            <td>{formatDateTime(student.enrolledAt)}</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </Table>
                                        </div>

                                        {renderPagination(studentMeta, setStudentPage)}
                                    </>
                                )}
                            </>
                        )}

                        {activeTab === "progress" && (
                            <>
                                <div className="cm-portal-section-heading">
                                    <span>TIẾN ĐỘ</span>
                                    <h5>Tiến độ học tập</h5>
                                    <p>Theo dõi lộ trình và bài hiện tại của từng học viên.</p>
                                </div>

                                <Row className="g-2 mb-3">
                                    <Col md={8}>
                                        <Form.Control value={progressKeyword}
                                            onChange={e => {
                                                setProgressKeyword(e.target.value);
                                                setProgressPage(1);
                                            }}
                                            placeholder="Tìm học viên..."
                                        />
                                    </Col>

                                    <Col md={4}>
                                        <Form.Select value={progressStatus}
                                            onChange={e => {
                                                setProgressStatus(e.target.value);
                                                setProgressPage(1);
                                            }}>
                                            <option value="ALL">Tất cả trạng thái</option>
                                            <option value="IN_PROGRESS">Đang học</option>
                                            <option value="COMPLETED">Hoàn thành</option>
                                            <option value="PAUSED">Tạm dừng</option>
                                        </Form.Select>
                                    </Col>
                                </Row>

                                {progressErr && <Alert variant="danger">{progressErr}</Alert>}

                                {progressLoading ? (
                                    <div className="text-center py-4"><MySpinner /></div>
                                ) : progress.length === 0 ? (
                                    <div className="cm-portal-empty">
                                        {progressKeyword.trim() || progressStatus !== "ALL"
                                            ? "Không tìm thấy dữ liệu tiến độ phù hợp."
                                            : "Chưa có dữ liệu tiến độ."}
                                    </div>
                                ) : (
                                    <>
                                        <Row className="g-3">
                                            {progress.map(item => {
                                                const percent = progressPercent(item);

                                                return (
                                                    <Col xs={12} key={item.studentId}>
                                                        <Card className="cm-portal-card">
                                                            <Card.Body className="p-3">
                                                                <Row className="align-items-center g-3">
                                                                    <Col lg={3}>
                                                                        <strong className="cm-portal-title d-block">
                                                                            {item.studentName}
                                                                        </strong>
                                                                        <small className="cm-portal-muted">@{item.username}</small>
                                                                    </Col>

                                                                    <Col lg={3}>
                                                                        <small className="cm-portal-muted d-block">Lộ trình</small>
                                                                        <strong className="d-block mb-1">
                                                                            {item.learningPathName || "Chưa có lộ trình"}
                                                                        </strong>
                                                                        {pathStatus(item.learningPathStatus)}
                                                                    </Col>

                                                                    <Col lg={3}>
                                                                        <small className="cm-portal-muted d-block">Bài hiện tại</small>
                                                                        <strong>{item.currentAssignmentName || "-"}</strong>
                                                                    </Col>

                                                                    <Col lg={3}>
                                                                        <div className="d-flex justify-content-between mb-2">
                                                                            <small className="cm-portal-muted">Tiến độ</small>
                                                                            <small className="fw-bold">
                                                                                {item.completedDetails}/{item.totalDetails}
                                                                            </small>
                                                                        </div>

                                                                        <ProgressBar now={percent} className="cm-portal-progress" />
                                                                        <small className="cm-portal-muted d-block mt-1 text-end">
                                                                            {percent}%
                                                                        </small>
                                                                    </Col>
                                                                </Row>
                                                            </Card.Body>
                                                        </Card>
                                                    </Col>
                                                );
                                            })}
                                        </Row>

                                        {renderPagination(progressMeta, setProgressPage)}
                                    </>
                                )}
                            </>
                        )}

                        {activeTab === "sessions" && (
                            <>
                                <div className="cm-portal-section-heading">
                                    <span>BUỔI HỌC</span>
                                    <h5>Buổi học online</h5>
                                    <p>Theo dõi, tham gia và điểm danh các buổi học.</p>
                                </div>

                                {sessions.length === 0 ? (
                                    <div className="cm-portal-empty">Lớp học chưa có buổi học online.</div>
                                ) : (
                                    <div className="d-flex flex-column gap-3">
                                        {sessions.map(session => (
                                            <Card key={session.sessionId} className="cm-portal-card">
                                                <Card.Body className="p-3">
                                                    <Row className="align-items-center g-3">
                                                        <Col lg={5}>
                                                            <div className="d-flex align-items-center gap-2 mb-1">
                                                                <strong className="cm-portal-title">{session.title}</strong>
                                                                {sessionStatus(session.status)}
                                                            </div>
                                                            <small className="cm-portal-muted">
                                                                {formatDate(session.startTime)}
                                                            </small>
                                                        </Col>

                                                        <Col lg={3}>
                                                            <small className="cm-portal-muted d-block">Thời gian</small>
                                                            <strong>
                                                                {formatTime(session.startTime)} - {formatTime(session.endTime)}
                                                            </strong>
                                                        </Col>

                                                        <Col lg={4}>
                                                            <div className="d-flex justify-content-lg-end gap-2 flex-wrap">
                                                                {session.status === "ONGOING" && session.meetingUrl && (
                                                                    <Button as="a" href={session.meetingUrl} target="_blank"
                                                                        rel="noreferrer" variant="success" size="sm">
                                                                        Vào lớp học
                                                                    </Button>
                                                                )}

                                                                {session.status === "UPCOMING" && (
                                                                    <Button variant="outline-secondary" size="sm" disabled>
                                                                        Chưa đến giờ
                                                                    </Button>
                                                                )}

                                                                {session.status === "ENDED" && (
                                                                    <Button variant="outline-secondary" size="sm" disabled>
                                                                        Đã kết thúc
                                                                    </Button>
                                                                )}

                                                                <Button variant="outline-primary" size="sm"
                                                                    onClick={() => nav(`/teacher/classes/${classId}/sessions/${session.sessionId}/attendance`)}>
                                                                    Điểm danh
                                                                </Button>
                                                            </div>
                                                        </Col>
                                                    </Row>
                                                </Card.Body>
                                            </Card>
                                        ))}
                                    </div>
                                )}
                            </>
                        )}

                        {activeTab === "assignments" && (
                            <ClassAssignments classId={classId} />
                        )}
                    </Card.Body>
                </Card>
            </div>
        </div>
    );
};

export default ClassDetail;