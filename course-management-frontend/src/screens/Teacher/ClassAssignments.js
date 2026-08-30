import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, ProgressBar, Row } from "react-bootstrap";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/TeacherClassAssignments.css";

const ClassAssignments = ({ classId }) => {
    const [assignments, setAssignments] = useState([]);
    const [assignmentMeta, setAssignmentMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });
    const [pathItems, setPathItems] = useState([]);
    const [pathMeta, setPathMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });
    const [availableAssignments, setAvailableAssignments] = useState([]);
    const [studentOptions, setStudentOptions] = useState([]);
    const [studentTotal, setStudentTotal] = useState(0);
    const [completedPathCount, setCompletedPathCount] = useState(0);
    const [showModal, setShowModal] = useState(false);

    const [pathKeyword, setPathKeyword] = useState("");
    const [pathStatus, setPathStatus] = useState("ALL");
    const [pathPage, setPathPage] = useState(1);

    const [sourceFilter, setSourceFilter] = useState("ALL");
    const [assignmentKeyword, setAssignmentKeyword] = useState("");
    const [assignmentStatus, setAssignmentStatus] = useState("ALL");
    const [assignmentPage, setAssignmentPage] = useState(1);

    const [studentKeyword, setStudentKeyword] = useState("");
    const [studentId, setStudentId] = useState("");
    const [assignmentId, setAssignmentId] = useState("");
    const [availableAt, setAvailableAt] = useState("");
    const [dueAt, setDueAt] = useState("");

    const [pathLoading, setPathLoading] = useState(true);
    const [assignmentLoading, setAssignmentLoading] = useState(true);
    const [studentSearching, setStudentSearching] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const readPage = (data, key) => ({
        items: Array.isArray(data?.[key]) ? data[key] : (Array.isArray(data) ? data : []),
        currentPage: Number(data?.currentPage || 1),
        totalPages: Number(data?.totalPages || 1),
        totalRecords: Number(data?.totalRecords ?? (Array.isArray(data) ? data.length : 0))
    });

    const loadOverview = async () => {
        try {
            const [availableRes, studentRes, completedRes] = await Promise.all([
                authApis().get(endpoints.teacherClassAvailableAssignments(classId)),
                authApis().get(endpoints.teacherClassStudents(classId), { params: { page: 1 } }),
                authApis().get(endpoints.teacherClassProgress(classId), {
                    params: { page: 1, status: "COMPLETED" }
                })
            ]);

            setAvailableAssignments(Array.isArray(availableRes.data) ? availableRes.data : []);
            setStudentTotal(readPage(studentRes.data, "students").totalRecords);
            setCompletedPathCount(readPage(completedRes.data, "progress").totalRecords);
        } catch (ex) {
            console.error("Load teacher assignment overview error:", ex);
        }
    };

    const loadPaths = async () => {
        try {
            setPathLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.teacherClassProgress(classId), {
                params: {
                    page: pathPage,
                    ...(pathKeyword.trim() ? { kw: pathKeyword.trim() } : {}),
                    ...(pathStatus !== "ALL" ? { status: pathStatus } : {})
                }
            });

            const pageData = readPage(res.data, "progress");
            setPathItems(pageData.items);
            setPathMeta({
                currentPage: pageData.currentPage,
                totalPages: pageData.totalPages,
                totalRecords: pageData.totalRecords
            });

            if (pageData.currentPage !== pathPage)
                setPathPage(pageData.currentPage);
        } catch (ex) {
            console.error("Load teacher path progress error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải tiến độ lộ trình!");
        } finally {
            setPathLoading(false);
        }
    };

    const loadAssignments = async () => {
        try {
            setAssignmentLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.teacherClassAssignments(classId), {
                params: {
                    page: assignmentPage,
                    ...(assignmentKeyword.trim() ? { kw: assignmentKeyword.trim() } : {}),
                    ...(assignmentStatus !== "ALL" ? { status: assignmentStatus } : {}),
                    ...(sourceFilter === "PATH" ? { source: "LEARNING_PATH" } : {}),
                    ...(sourceFilter === "MANUAL" ? { source: "MANUAL" } : {})
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
            console.error("Load teacher assignments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách bài tập!");
        } finally {
            setAssignmentLoading(false);
        }
    };

    useEffect(() => {
        loadOverview();
    }, [classId]);

    useEffect(() => {
        const timer = setTimeout(loadPaths, 300);
        return () => clearTimeout(timer);
    }, [classId, pathPage, pathKeyword, pathStatus]);

    useEffect(() => {
        const timer = setTimeout(loadAssignments, 300);
        return () => clearTimeout(timer);
    }, [classId, assignmentPage, assignmentKeyword, assignmentStatus, sourceFilter]);

    useEffect(() => {
        if (!showModal) return;

        const timer = setTimeout(async () => {
            try {
                setStudentSearching(true);

                const res = await authApis().get(endpoints.teacherClassStudents(classId), {
                    params: {
                        page: 1,
                        ...(studentKeyword.trim() ? { kw: studentKeyword.trim() } : {})
                    }
                });

                setStudentOptions(readPage(res.data, "students").items);
            } catch (ex) {
                setStudentOptions([]);
            } finally {
                setStudentSearching(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [classId, showModal, studentKeyword]);

    const openAssignModal = () => {
        setStudentKeyword("");
        setStudentId("");
        setAssignmentId("");
        setAvailableAt("");
        setDueAt("");
        setErr("");
        setShowModal(true);
    };

    const assignManual = async e => {
        e.preventDefault();

        if (!studentId || !assignmentId)
            return setErr("Vui lòng chọn học viên và bài tập!");

        if (availableAt && dueAt && dueAt <= availableAt)
            return setErr("Hạn nộp phải sau thời gian mở bài!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            await authApis().post(endpoints.teacherManualAssignment(classId), {
                studentId: Number(studentId),
                assignmentId: Number(assignmentId),
                availableAt: availableAt || null,
                dueAt: dueAt || null
            });

            setShowModal(false);
            setSuccess("Đã giao bài bổ sung cho học viên.");
            await Promise.all([loadAssignments(), loadOverview()]);
        } catch (ex) {
            console.error("Assign manual error:", ex);
            setErr(ex.response?.data?.message || "Không thể giao bài tập!");
        } finally {
            setSaving(false);
        }
    };

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit", day: "2-digit",
            month: "2-digit", year: "numeric"
        })
        : "Không đặt";

    const progressPercent = item => !item.totalDetails
        ? 0
        : Math.min(Math.round((item.completedDetails / item.totalDetails) * 100), 100);

    const pathBadge = status => {
        const config = {
            IN_PROGRESS: ["primary", "Đang học"],
            COMPLETED: ["success", "Hoàn thành"],
            PAUSED: ["warning", "Tạm dừng"]
        }[status] || ["secondary", "Chưa có lộ trình"];

        return <Badge bg={config[0]} text={status === "PAUSED" ? "dark" : undefined}>{config[1]}</Badge>;
    };

    const assignedStatusBadge = status => {
        const config = {
            AVAILABLE: ["success", "Sẵn sàng"],
            LOCKED: ["secondary", "Chưa mở"],
            COMPLETED: ["primary", "Hoàn thành"]
        }[status] || ["secondary", status || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    const attemptBadge = status => {
        const config = {
            IN_PROGRESS: ["primary", "Đang làm"],
            SUBMITTED: ["info", "Đã nộp"],
            PENDING_GRADING: ["warning", "Chờ chấm"],
            GRADED: ["success", "Đã chấm"]
        }[status];

        return config
            ? <Badge bg={config[0]} text={status === "PENDING_GRADING" ? "dark" : undefined}>{config[1]}</Badge>
            : null;
    };

    const sourceBadge = source => source === "LEARNING_PATH"
        ? <Badge bg="primary">Lộ trình</Badge>
        : <Badge bg="secondary">Bài bổ sung</Badge>;

    const currentState = item => {
        if (!item.studentLearningPathId)
            return { label: "Chưa được gán lộ trình", type: "neutral" };

        if (item.learningPathStatus === "COMPLETED")
            return { label: "Đã hoàn thành toàn bộ lộ trình", type: "completed" };

        if (item.learningPathStatus === "PAUSED")
            return { label: "Lộ trình đang tạm dừng", type: "locked" };

        if (item.currentAssignmentName)
            return { label: "Đang học bài hiện tại", type: "active" };

        return { label: "Đang theo lộ trình", type: "neutral" };
    };

    const assignmentTypeLabel = type => {
        const config = {
            PRACTICE: "Luyện tập",
            TEST: "Bài kiểm tra",
            QUIZ: "Trắc nghiệm",
            EXAM: "Bài thi"
        };

        return config[type] || type || "Bài tập";
    };

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
            <div className="cm-class-pagination">
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

    if (pathLoading && assignmentLoading)
        return (
            <div className="text-center py-4">
                <MySpinner />
            </div>
        );

    return (
        <>
            <div className="cm-class-assignment-heading">
                <div className="cm-portal-section-heading mb-0">
                    <span>BÀI TẬP</span>
                    <h5>Quản lý bài tập của lớp</h5>
                    <p>Theo dõi lộ trình tự động và giao thêm bài tập khi học viên cần luyện tập bổ sung.</p>
                </div>

                <Button onClick={openAssignModal}>+ Giao bài bổ sung</Button>
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

            <Row className="g-3 cm-class-assignment-summary">
                <Col md={4}>
                    <div className="cm-class-assignment-stat">
                        <span>Học viên trong lớp</span>
                        <strong>{studentTotal}</strong>
                        <small>đang hoạt động</small>
                    </div>
                </Col>

                <Col md={4}>
                    <div className="cm-class-assignment-stat">
                        <span>Đã hoàn thành lộ trình</span>
                        <strong>{completedPathCount}</strong>
                        <small>tính trên toàn bộ lớp</small>
                    </div>
                </Col>

                <Col md={4}>
                    <div className="cm-class-assignment-stat">
                        <span>Bài đã giao</span>
                        <strong>{assignmentMeta.totalRecords}</strong>
                        <small>theo bộ lọc hiện tại</small>
                    </div>
                </Col>
            </Row>

            <Card className="cm-portal-card cm-class-path-card">
                <Card.Body>
                    <div className="cm-class-section-head">
                        <div className="cm-portal-section-heading mb-0">
                            <span>LỘ TRÌNH TỰ ĐỘNG</span>
                            <h5>Tiến độ theo lộ trình học tập</h5>
                            <p>Giáo viên theo dõi tại đây. Bài tiếp theo được backend tự mở khi học viên đạt yêu cầu bài trước.</p>
                        </div>

                        <Badge bg="light" text="dark">{pathMeta.totalRecords} học viên</Badge>
                    </div>

                    <div className="cm-class-controls">
                        <Form.Control value={pathKeyword}
                            onChange={e => {
                                setPathKeyword(e.target.value);
                                setPathPage(1);
                            }}
                            placeholder="Tìm học viên, lộ trình hoặc bài hiện tại..."
                        />

                        <Form.Select value={pathStatus}
                            onChange={e => {
                                setPathStatus(e.target.value);
                                setPathPage(1);
                            }}>
                            <option value="ALL">Tất cả trạng thái</option>
                            <option value="IN_PROGRESS">Đang học</option>
                            <option value="COMPLETED">Hoàn thành</option>
                            <option value="PAUSED">Tạm dừng</option>
                        </Form.Select>
                    </div>

                    {pathLoading ? (
                        <div className="text-center py-4"><MySpinner /></div>
                    ) : pathItems.length === 0 ? (
                        <div className="cm-portal-empty">
                            {pathKeyword.trim() || pathStatus !== "ALL"
                                ? "Không tìm thấy học viên phù hợp với bộ lọc hiện tại."
                                : "Chưa có dữ liệu tiến độ của lớp."}
                        </div>
                    ) : (
                        <>
                            <div className="cm-class-path-list">
                                {pathItems.map(item => {
                                    const percent = progressPercent(item);
                                    const state = currentState(item);

                                    return (
                                        <div className="cm-class-path-item" key={item.studentId}>
                                            <div className="cm-class-student">
                                                <div className="cm-class-student-avatar">
                                                    {(item.studentName || "?").trim().charAt(0).toUpperCase()}
                                                </div>

                                                <div>
                                                    <strong>{item.studentName}</strong>
                                                    <span>@{item.username}</span>
                                                </div>
                                            </div>

                                            <div className="cm-class-path-main">
                                                <div className="cm-class-path-title">
                                                    <div>
                                                        <span>LỘ TRÌNH</span>
                                                        <strong>{item.learningPathName || "Chưa có lộ trình"}</strong>
                                                    </div>

                                                    {pathBadge(item.learningPathStatus)}
                                                </div>

                                                <div className="cm-class-path-progress-head">
                                                    <span>Tiến độ</span>
                                                    <strong>{item.completedDetails || 0}/{item.totalDetails || 0} bài · {percent}%</strong>
                                                </div>

                                                <ProgressBar now={percent} className="cm-portal-progress" />

                                                <div className="cm-class-current">
                                                    <div>
                                                        <span>{item.learningPathStatus === "COMPLETED" ? "TRẠNG THÁI" : "BÀI HIỆN TẠI"}</span>
                                                        <strong>
                                                            {item.learningPathStatus === "COMPLETED"
                                                                ? "Đã hoàn thành lộ trình"
                                                                : item.currentAssignmentName || "Chưa có bài hiện tại"}
                                                        </strong>
                                                    </div>

                                                    <span className={`cm-class-current-state is-${state.type}`}>
                                                        {state.label}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            {renderPagination(pathMeta, setPathPage)}
                        </>
                    )}
                </Card.Body>
            </Card>

            <Card className="cm-portal-card cm-class-assigned-card">
                <Card.Body>
                    <div className="cm-class-section-head cm-class-assigned-head">
                        <div className="cm-portal-section-heading mb-0">
                            <span>LỊCH SỬ BÀI ĐÃ GIAO</span>
                            <h5>Bài tập của học viên</h5>
                            <p>Xem bài từ lộ trình và các bài bổ sung giáo viên đã giao thủ công.</p>
                        </div>

                        <div className="cm-class-filter">
                            <button type="button" className={sourceFilter === "ALL" ? "active" : ""}
                                onClick={() => {
                                    setSourceFilter("ALL");
                                    setAssignmentPage(1);
                                }}>
                                Tất cả
                            </button>
                            <button type="button" className={sourceFilter === "PATH" ? "active" : ""}
                                onClick={() => {
                                    setSourceFilter("PATH");
                                    setAssignmentPage(1);
                                }}>
                                Lộ trình
                            </button>
                            <button type="button" className={sourceFilter === "MANUAL" ? "active" : ""}
                                onClick={() => {
                                    setSourceFilter("MANUAL");
                                    setAssignmentPage(1);
                                }}>
                                Bổ sung
                            </button>
                        </div>
                    </div>

                    <div className="cm-class-controls cm-class-assignment-controls">
                        <Form.Control value={assignmentKeyword}
                            onChange={e => {
                                setAssignmentKeyword(e.target.value);
                                setAssignmentPage(1);
                            }}
                            placeholder="Tìm học viên hoặc tên bài tập..."
                        />

                        <Form.Select value={assignmentStatus}
                            onChange={e => {
                                setAssignmentStatus(e.target.value);
                                setAssignmentPage(1);
                            }}>
                            <option value="ALL">Tất cả trạng thái</option>
                            <optgroup label="Trạng thái bài">
                                <option value="AVAILABLE">Sẵn sàng</option>
                                <option value="LOCKED">Chưa mở</option>
                                <option value="COMPLETED">Hoàn thành</option>
                            </optgroup>
                            <optgroup label="Lần làm gần nhất">
                                <option value="IN_PROGRESS">Đang làm</option>
                                <option value="SUBMITTED">Đã nộp</option>
                                    <option value="GRADED">Đã chấm</option>
                            </optgroup>
                        </Form.Select>
                    </div>

                    {assignmentLoading ? (
                        <div className="text-center py-4"><MySpinner /></div>
                    ) : assignments.length === 0 ? (
                        <div className="cm-portal-empty">
                            {assignmentKeyword.trim() || assignmentStatus !== "ALL" || sourceFilter !== "ALL"
                                ? "Không có bài tập phù hợp với bộ lọc hiện tại."
                                : "Chưa có bài tập nào được giao cho học viên."}
                        </div>
                    ) : (
                        <>
                            <div className="cm-class-assignment-list">
                                {assignments.map(item => (
                                    <div className="cm-class-assignment-row" key={item.assignedAssignmentId}>
                                        <div className="cm-class-assignment-info">
                                            <div className="cm-class-assignment-name">
                                                <strong>{item.assignmentName}</strong>
                                                {sourceBadge(item.assignmentSource)}
                                            </div>

                                            <div className="cm-class-assignment-sub">
                                                <span>{assignmentTypeLabel(item.assignmentType)}</span>
                                                <span>{item.maximumScore} điểm</span>
                                                <span>Mã #{item.assignedAssignmentId}</span>
                                            </div>
                                        </div>

                                        <div className="cm-class-assignment-student">
                                            <span>HỌC VIÊN</span>
                                            <strong>{item.studentName}</strong>
                                            <small>@{item.username}</small>
                                        </div>

                                        <div className="cm-class-assignment-status">
                                            <span>TRẠNG THÁI BÀI</span>
                                            <div>{assignedStatusBadge(item.status)}</div>
                                        </div>

                                        <div className="cm-class-assignment-attempt">
                                            <span>LẦN LÀM GẦN NHẤT</span>
                                            {item.latestAttemptId ? (
                                                <>
                                                    <strong>#{item.latestAttemptNumber}</strong>
                                                    <div>{attemptBadge(item.latestAttemptStatus)}</div>
                                                </>
                                            ) : (
                                                <small>Chưa làm</small>
                                            )}
                                        </div>

                                        <div className="cm-class-assignment-dates">
                                            <div>
                                                <span>MỞ BÀI</span>
                                                <strong>{formatDateTime(item.availableAt)}</strong>
                                            </div>
                                            <div>
                                                <span>HẠN NỘP</span>
                                                <strong>{formatDateTime(item.dueAt)}</strong>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {renderPagination(assignmentMeta, setAssignmentPage)}
                        </>
                    )}
                </Card.Body>
            </Card>

            <Modal show={showModal} onHide={() => !saving && setShowModal(false)} centered size="lg">
                <Form onSubmit={assignManual}>
                    <Modal.Header closeButton>
                        <Modal.Title>Giao bài bổ sung</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Alert variant="info" className="cm-class-manual-note">
                            Bài được giao tại đây là <strong>bài bổ sung của giáo viên</strong>.
                            Bài này độc lập với thứ tự mở khóa tự động của Learning Path.
                        </Alert>

                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Học viên</Form.Label>
                                    <Form.Control className="mb-2" value={studentKeyword}
                                        onChange={e => {
                                            setStudentKeyword(e.target.value);
                                            setStudentId("");
                                        }}
                                        placeholder="Tìm tên hoặc tài khoản học viên..."
                                    />

                                    <Form.Select value={studentId}
                                        onChange={e => setStudentId(e.target.value)}
                                        disabled={studentSearching} required>
                                        <option value="">
                                            {studentSearching ? "Đang tìm học viên..." : "-- Chọn học viên --"}
                                        </option>

                                        {studentOptions.map(student => (
                                            <option key={student.studentId} value={student.studentId}>
                                                {student.fullName} ({student.username})
                                            </option>
                                        ))}
                                    </Form.Select>
                                    <Form.Text>Hiển thị tối đa các kết quả đầu tiên từ backend; nhập từ khóa để tìm nhanh.</Form.Text>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Bài tập</Form.Label>
                                    <Form.Select value={assignmentId}
                                        onChange={e => setAssignmentId(e.target.value)} required>
                                        <option value="">-- Chọn bài tập --</option>

                                        {availableAssignments.map(item => (
                                            <option key={item.assignmentId} value={item.assignmentId}>
                                                {item.assignmentName} - {item.maximumScore} điểm
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Thời gian mở bài</Form.Label>
                                    <Form.Control type="datetime-local" value={availableAt}
                                        onChange={e => setAvailableAt(e.target.value)} />
                                    <Form.Text>Để trống để mở bài ngay.</Form.Text>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Hạn nộp</Form.Label>
                                    <Form.Control type="datetime-local" value={dueAt}
                                        onChange={e => setDueAt(e.target.value)} />
                                    <Form.Text>Có thể để trống nếu không giới hạn hạn nộp.</Form.Text>
                                </Form.Group>
                            </Col>
                        </Row>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={() => setShowModal(false)} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving}>
                            {saving ? "Đang giao..." : "Giao bài bổ sung"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default ClassAssignments;
