import { useEffect, useState } from "react";
import { Accordion, Alert, Badge, Button, Card, Col, ListGroup, Nav, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/CourseAssignments.css";
import CourseAssignments from "./CourseAssignments";

const CourseDetail = () => {
    const { enrollmentId } = useParams();
    const nav = useNavigate();

    const [detail, setDetail] = useState(null);
    const [content, setContent] = useState(null);
    const [paths, setPaths] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [attendance, setAttendance] = useState([]);
    const [activeTab, setActiveTab] = useState("content");
    const [attendanceLoaded, setAttendanceLoaded] = useState(false);
    const [attendanceLoading, setAttendanceLoading] = useState(false);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [attendanceErr, setAttendanceErr] = useState("");

    const loadCourse = async () => {
        try {
            setLoading(true);
            setErr("");

            const detailRes = await authApis().get(`${endpoints.studentCourses}/detail/${enrollmentId}`);
            const courseDetail = detailRes.data;
            setDetail(courseDetail);

            const [contentRes, pathRes, courseAssignmentRes] = await Promise.all([
                authApis().get(`${endpoints.studentCourses}/${courseDetail.courseId}/content`),
                authApis().get(`${endpoints.studentCourses}/${courseDetail.courseId}/learning-paths`),
                authApis().get(endpoints.courseAssignments(courseDetail.courseId))
            ]);

            const courseAssignments = Array.isArray(courseAssignmentRes.data)
                ? courseAssignmentRes.data.map(item => ({
                    ...item,
                    latestAttemptId: item.attemptId ?? null,
                    latestAttemptNumber: item.latestAttemptNumber ?? null,
                    latestAttemptStatus: item.latestAttemptStatus ?? null,
                    canStart: item.status === "AVAILABLE" ? item.canStart !== false : false
                }))
                : [];

            setContent(contentRes.data);
            setPaths(Array.isArray(pathRes.data) ? pathRes.data : []);
            setAssignments(courseAssignments);
        } catch (ex) {
            console.error("Load course detail error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải khóa học!");
        } finally {
            setLoading(false);
        }
    };

    const loadAttendance = async () => {
        if (attendanceLoaded || attendanceLoading || !detail?.classId) return;

        try {
            setAttendanceLoading(true);
            setAttendanceErr("");

            const res = await authApis().get(endpoints.studentClassAttendance(detail.classId));
            setAttendance(Array.isArray(res.data) ? res.data : []);
            setAttendanceLoaded(true);
        } catch (ex) {
            console.error("Load attendance error:", ex);
            setAttendanceErr(ex.response?.data?.message || "Không thể tải điểm danh!");
        } finally {
            setAttendanceLoading(false);
        }
    };


    useEffect(() => {
        loadCourse();
    }, [enrollmentId]);

    const changeTab = key => {
        setActiveTab(key);
    
        if (key === "attendance")
            loadAttendance();
    };

    const modules = content?.modules || [];
    const presentCount = attendance.filter(x => x.attendanceStatus === "PRESENT").length;
    const absentCount = attendance.filter(x => x.attendanceStatus === "ABSENT").length;
    const notMarkedCount = attendance.filter(x =>
        x.sessionStatus === "ENDED" && x.attendanceStatus === "NOT_MARKED"
    ).length;

    const formatDate = value => value
        ? new Date(value).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" })
        : "-";

    const formatTime = value => value
        ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
        : "-";

    const getPathStatus = status => {
        if (status === "COMPLETED") return { label: "Hoàn thành", className: "completed" };
        if (status === "PAUSED") return { label: "Tạm dừng", className: "paused" };
        return { label: "Đang học", className: "in-progress" };
    };

    const getDetailStatusText = status => {
        if (status === "COMPLETED") return "Đã hoàn thành";
        if (status === "CURRENT") return "Bài hiện tại";
        return "Chưa mở";
    };

    const assignmentById = new Map(
        assignments.map(item => [item.assignmentId, item])
    );

    const getAssignmentName = assignmentId =>
        assignmentById.get(assignmentId)?.assignmentName || `Assignment #${assignmentId}`;

    const getPathProgress = path => {
        const details = path?.details || [];
        const completed = details.filter(item => item.status === "COMPLETED").length;
        return details.length ? Math.round(completed / details.length * 100) : 0;
    };

    const getCurrentPathAssignment = path => {
        const current = (path?.details || []).find(item => item.status === "CURRENT");
        return current ? getAssignmentName(current.assignmentId) : null;
    };

    const attendanceStatus = item => {
        if (item.attendanceStatus === "PRESENT") return <Badge bg="success">Có mặt</Badge>;
        if (item.attendanceStatus === "ABSENT") return <Badge bg="danger">Vắng</Badge>;
        if (item.sessionStatus === "UPCOMING") return <Badge bg="secondary">Chưa diễn ra</Badge>;
        if (["ONGOING", "LIVE", "IN_PROGRESS"].includes(item.sessionStatus))
            return <Badge bg="info">Đang diễn ra</Badge>;
        return <Badge bg="warning" text="dark">Chưa ghi nhận</Badge>;
    };

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
                    <Button variant="outline-secondary" onClick={() => nav("/student/courses")}>
                        ← Khóa học của tôi
                    </Button>
                </div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <Button variant="outline-secondary" size="sm" className="cm-portal-back"
                    onClick={() => nav("/student/courses")}>
                    ← Khóa học của tôi
                </Button>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-4">
                        <Row className="align-items-center g-4">
                            <Col md={8}>
                                <span className="cm-portal-label">KHÓA HỌC</span>
                                <h2 className="cm-portal-title mt-2 mb-2">{detail?.courseName}</h2>
                                <div className="cm-portal-muted">
                                    Lớp: <strong>{detail?.className}</strong>
                                    {" · "}
                                    {formatDate(detail?.startDate)} → {formatDate(detail?.endDate)}
                                </div>
                            </Col>

                            <Col md={4} className="text-md-end">
                                <Badge bg={detail?.enrollmentStatus === "ACTIVE" ? "success" : "secondary"}>
                                    {detail?.enrollmentStatus || "-"}
                                </Badge>

                                {detail?.learningPathName && (
                                    <div className="cm-portal-muted mt-2">
                                        Lộ trình: <strong>{detail.learningPathName}</strong>
                                    </div>
                                )}
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                <Row className="g-4">
                    <Col lg={8}>
                        <Card className="cm-portal-card">
                            <Nav className="cm-portal-tabs" activeKey={activeTab} onSelect={changeTab}>
                                <Nav.Item><Nav.Link eventKey="content">Nội dung khóa học</Nav.Link></Nav.Item>
                                <Nav.Item>
                                    <Nav.Link eventKey="assignments">
                                        Bài tập
                                    </Nav.Link>
                                </Nav.Item>
                                <Nav.Item><Nav.Link eventKey="attendance">Điểm danh</Nav.Link></Nav.Item>
                            </Nav>

                            <Card.Body className="p-4">
                                {activeTab === "content" && (
                                    <>
                                        <div className="cm-portal-section-heading">
                                            <span>NỘI DUNG</span>
                                            <h5>Nội dung khóa học</h5>
                                            <p>Các module và bài học thuộc khóa học.</p>
                                        </div>

                                        {modules.length === 0 ? (
                                            <div className="cm-portal-empty">Khóa học chưa có nội dung.</div>
                                        ) : (
                                            <Accordion alwaysOpen>
                                                {modules.map((module, index) => (
                                                    <Accordion.Item eventKey={String(index)}
                                                        key={module.id ?? module.moduleId}>
                                                        <Accordion.Header>
                                                            <strong>
                                                                Module {module.orderNumber}: {module.moduleName}
                                                            </strong>
                                                        </Accordion.Header>

                                                        <Accordion.Body className="p-0">
                                                            {!module.lessons?.length ? (
                                                                <div className="p-3 cm-portal-muted">
                                                                    Module chưa có bài học.
                                                                </div>
                                                            ) : (
                                                                <ListGroup variant="flush">
                                                                    {module.lessons.map(lesson => (
                                                                <ListGroup.Item
                                                                    key={lesson.lessonId}
                                                                    className="d-flex justify-content-between align-items-center gap-3 py-3"
                                                                >
                                                                    <div>
                                                                        <span className="cm-portal-muted me-2">
                                                                            {lesson.orderNumber}.
                                                                        </span>

                                                                        <strong>
                                                                            {lesson.lessonName}
                                                                        </strong>
                                                                    </div>

                                                                    <Button
                                                                        size="sm"
                                                                        variant={lesson.locked ? "secondary" : "outline-primary"}
                                                                        disabled={lesson.locked}
                                                                        onClick={() =>
                                                                            !lesson.locked &&
                                                                            nav(`/student/lessons/${lesson.lessonId}`)
                                                                        }
                                                                    >
                                                                        {lesson.locked ? "Đã khóa" : "Học bài"}
                                                                    </Button>
                                                                </ListGroup.Item>
                                                            ))}
                                                                </ListGroup>
                                                            )}
                                                        </Accordion.Body>
                                                    </Accordion.Item>
                                                ))}
                                            </Accordion>
                                        )}
                                    </>
                                )}

                                {activeTab === "assignments" && (
                                    <CourseAssignments assignments={assignments} />
                                )}
                                {activeTab === "attendance" && (
                                    <>
                                        <div className="cm-portal-section-heading">
                                            <span>ĐIỂM DANH</span>
                                            <h5>Lịch sử điểm danh</h5>
                                            <p>Theo dõi trạng thái tham gia các buổi học của bạn.</p>
                                        </div>

                                        {attendanceErr && <Alert variant="danger">{attendanceErr}</Alert>}

                                        {attendanceLoading ? (
                                            <div className="text-center py-4"><MySpinner /></div>
                                        ) : attendanceLoaded && attendance.length === 0 ? (
                                            <div className="cm-portal-empty">
                                                Lớp học chưa có dữ liệu điểm danh.
                                            </div>
                                        ) : attendanceLoaded && (
                                            <>
                                                <Row className="g-2 mb-4">
                                                    <Col>
                                                        <div className="cm-portal-summary">
                                                            <span>Có mặt</span>
                                                            <strong>{presentCount}</strong>
                                                        </div>
                                                    </Col>

                                                    <Col>
                                                        <div className="cm-portal-summary">
                                                            <span>Vắng</span>
                                                            <strong>{absentCount}</strong>
                                                        </div>
                                                    </Col>

                                                    <Col>
                                                        <div className="cm-portal-summary">
                                                            <span>Chưa ghi nhận</span>
                                                            <strong>{notMarkedCount}</strong>
                                                        </div>
                                                    </Col>
                                                </Row>

                                                <div className="d-flex flex-column gap-3">
                                                    {attendance.map(item => (
                                                        <Card key={item.sessionId} className="cm-portal-card">
                                                            <Card.Body className="p-3">
                                                                <div className="d-flex justify-content-between align-items-start gap-3">
                                                                    <div>
                                                                        <strong className="cm-portal-title d-block">
                                                                            {item.sessionTitle || `Buổi học #${item.sessionId}`}
                                                                        </strong>

                                                                        <small className="cm-portal-muted">
                                                                            {formatDate(item.startTime)}
                                                                            {" · "}
                                                                            {formatTime(item.startTime)} - {formatTime(item.endTime)}
                                                                        </small>
                                                                    </div>

                                                                    {attendanceStatus(item)}
                                                                </div>

                                                                {item.attendedAt && (
                                                                    <div className="cm-portal-muted mt-2">
                                                                        Điểm danh lúc: <strong>{formatTime(item.attendedAt)}</strong>
                                                                    </div>
                                                                )}

                                                                {item.note && (
                                                                    <div className="cm-portal-muted mt-1">
                                                                        Ghi chú: {item.note}
                                                                    </div>
                                                                )}
                                                            </Card.Body>
                                                        </Card>
                                                    ))}
                                                </div>
                                            </>
                                        )}
                                    </>
                                )}
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col lg={4}>
                        <Card className="cm-portal-card cm-learning-card mb-4">
                            <Card.Body className="p-4">
                                <div className="cm-portal-section-heading">
                                    <span>LỘ TRÌNH</span>
                                    <h5>Tiến độ học tập</h5>
                                    <p>Xem bạn đang ở đâu và bài nào sẽ được mở tiếp theo.</p>
                                </div>

                                {paths.length === 0 ? (
                                    <div className="cm-portal-empty">
                                        Bạn chưa được gán lộ trình học.
                                    </div>
                                ) : paths.map(path => {
                                    const pathDetails = path.details || [];
                                    const completedCount = pathDetails.filter(item => item.status === "COMPLETED").length;
                                    const pathProgress = getPathProgress(path);
                                    const pathState = getPathStatus(path.status);
                                    const currentName = getCurrentPathAssignment(path);

                                    return (
                                        <div className="cm-learning-path" key={path.studentLearningPathId ?? path.learningPathId}>
                                            <div className="cm-learning-head">
                                                <h6>{path.learningPathName || "Lộ trình học"}</h6>
                                                <span className={`cm-learning-status ${pathState.className}`}>
                                                    {pathState.label}
                                                </span>
                                            </div>

                                            <div className="cm-learning-progress-meta">
                                                <span>Tiến độ</span>
                                                <strong>{completedCount}/{pathDetails.length} bài · {pathProgress}%</strong>
                                            </div>

                                            <div className="cm-learning-progress-track">
                                                <div style={{ width: `${pathProgress}%` }} />
                                            </div>

                                            <div className="cm-path-timeline">
                                                {pathDetails.map((item, index) => {
                                                    const stateClass = item.status === "CURRENT"
                                                        ? "current"
                                                        : item.status === "COMPLETED" ? "completed" : "locked";
                                                    const marker = item.status === "COMPLETED"
                                                        ? "✓"
                                                        : item.status === "CURRENT" ? "▶" : "🔒";

                                                    return (
                                                        <div className={`cm-path-item ${stateClass}`} key={item.detailId ?? item.id}>
                                                            <div className="cm-path-rail">
                                                                <div className={`cm-path-dot ${stateClass}`}>{marker}</div>
                                                                {index < pathDetails.length - 1 && <div className="cm-path-line" />}
                                                            </div>

                                                            <div className="cm-path-copy">
                                                                <small>Bài {item.orderNumber}</small>
                                                                <strong>{getAssignmentName(item.assignmentId)}</strong>
                                                                <span>{getDetailStatusText(item.status)}</span>
                                                            </div>
                                                        </div>
                                                    );
                                                })}
                                            </div>

                                            <div className="cm-learning-next">
                                                {path.status === "COMPLETED" ? (
                                                    <>Bạn đã hoàn thành toàn bộ lộ trình này.</>
                                                ) : currentName ? (
                                                    <>Bài bạn nên tập trung tiếp theo:<strong>{currentName}</strong></>
                                                ) : (
                                                    <>Tiến độ đang được cập nhật theo kết quả bài làm.</>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </div>
        </div>
    );
};

export default CourseDetail;