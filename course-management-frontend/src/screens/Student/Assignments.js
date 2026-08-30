import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Assignments = () => {
    const nav = useNavigate();

    const [assignments, setAssignments] = useState([]);
    const [page, setPage] = useState(1);
    const [keyword, setKeyword] = useState("");
    const [status, setStatus] = useState("ALL");
    const [meta, setMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });
    const [loading, setLoading] = useState(true);
    const [starting, setStarting] = useState(null);
    const [err, setErr] = useState("");

    const loadAssignments = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.studentAssignments, {
                params: {
                    page,
                    ...(keyword.trim() ? { kw: keyword.trim() } : {}),
                    ...(status !== "ALL" ? { status } : {})
                }
            });

            if (Array.isArray(res.data)) {
                setAssignments(res.data);
                setMeta({ currentPage: 1, totalPages: 1, totalRecords: res.data.length });
                return;
            }

            setAssignments(Array.isArray(res.data?.assignments) ? res.data.assignments : []);

            const nextMeta = {
                currentPage: Number(res.data?.currentPage || 1),
                totalPages: Number(res.data?.totalPages || 1),
                totalRecords: Number(res.data?.totalRecords || 0)
            };

            setMeta(nextMeta);
            if (nextMeta.currentPage !== page) setPage(nextMeta.currentPage);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải bài tập!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(loadAssignments, 300);
        return () => clearTimeout(timer);
    }, [page, keyword, status]);

    const startAssignment = async item => {
        try {
            setStarting(item.assignedAssignmentId);
            setErr("");

            const res = await authApis().post(
                `${endpoints.studentAssignments}/${item.assignedAssignmentId}/start`
            );

            nav(`/student/assignments/attempt/${res.data.attemptId}`);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể bắt đầu làm bài!");
        } finally {
            setStarting(null);
        }
    };

    const statusBadge = status => {
        const config = {
            LOCKED: ["secondary", "Đã khóa"],
            AVAILABLE: ["success", "Có thể làm"],
            COMPLETED: ["primary", "Hoàn thành"]
        }[status] || ["secondary", status || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    const attemptBadge = status => {
        const config = {
            IN_PROGRESS: ["warning", "Đang làm"],
            SUBMITTED: ["info", "Đã nộp"],
            PENDING_GRADING: ["info", "Chờ giáo viên chấm"],
            GRADED: ["success", "Đã chấm"]
        }[status];
    
        return config ? <Badge bg={config[0]}>{config[1]}</Badge> : null;
    };

    const actionButton = item => {
        if (item.status === "LOCKED")
            return <Button disabled>Chưa mở</Button>;

        if (item.latestAttemptStatus === "IN_PROGRESS")
            return (
                <Button onClick={() => nav(`/student/assignments/attempt/${item.latestAttemptId}`)}>
                    Tiếp tục làm bài
                </Button>
            );

        if (["SUBMITTED", "PENDING_GRADING"].includes(item.latestAttemptStatus) && item.latestAttemptId)
            return (
                <Button variant="outline-primary"
                    onClick={() => nav(`/student/assignments/result/${item.latestAttemptId}`)}>
                    Xem trạng thái
                </Button>
            );

        if (item.status === "COMPLETED" && item.latestAttemptId)
            return (
                <Button variant="outline-primary"
                    onClick={() => nav(`/student/assignments/result/${item.latestAttemptId}`)}>
                    Xem kết quả
                </Button>
            );

        if (item.canStart)
            return (
                <Button disabled={starting === item.assignedAssignmentId}
                    onClick={() => startAssignment(item)}>
                    {starting === item.assignedAssignmentId
                        ? "Đang bắt đầu..."
                        : item.latestAttemptStatus === "GRADED" ? "Làm lại bài" : "Bắt đầu làm bài"}
                </Button>
            );

        if (item.latestAttemptId)
            return (
                <Button variant="outline-primary"
                    onClick={() => nav(`/student/assignments/result/${item.latestAttemptId}`)}>
                    Xem kết quả
                </Button>
            );

        return <Button disabled>Chưa thể làm</Button>;
    };

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

    const dateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

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
                        <span>BÀI TẬP</span>
                        <h1>Bài tập của tôi</h1>
                        <p>Theo dõi và hoàn thành các bài tập đã được giao.</p>
                    </div>

                    <div className="cm-portal-summary">
                        <span>Tổng bài tập</span>
                        <strong>{meta.totalRecords}</strong>
                    </div>
                </div>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-3">
                        <Row className="g-2">
                            <Col md={8}>
                                <Form.Control value={keyword}
                                    onChange={e => {
                                        setKeyword(e.target.value);
                                        setPage(1);
                                    }}
                                    placeholder="Tìm theo tên bài tập..."
                                />
                            </Col>

                            <Col md={4}>
                                <Form.Select value={status}
                                    onChange={e => {
                                        setStatus(e.target.value);
                                        setPage(1);
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

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                {assignments.length === 0 ? (
                    <div className="cm-portal-empty">
                        {keyword.trim() || status !== "ALL"
                            ? "Không tìm thấy bài tập phù hợp."
                            : "Bạn chưa có bài tập nào."}
                    </div>
                ) : (
                    <Row className="g-4">
                        {assignments.map(item => (
                            <Col xl={4} md={6} key={item.assignedAssignmentId}>
                                <Card className="cm-portal-card cm-portal-hover h-100">
                                    <Card.Body className="p-4 d-flex flex-column">
                                        <div className="d-flex justify-content-between gap-2 mb-3">
                                            <Badge bg={item.assignmentType === "TEST" ? "warning" : "info"}
                                                text={item.assignmentType === "TEST" ? "dark" : undefined}>
                                                {item.assignmentType === "TEST" ? "Kiểm tra" : "Luyện tập"}
                                            </Badge>

                                            {statusBadge(item.status)}
                                        </div>

                                        <h5 className="cm-portal-title mb-1">{item.assignmentName}</h5>
                                        <div className="cm-portal-muted mb-4">{item.courseName}</div>

                                        <div className="small mb-2">
                                            Điểm tối đa: <strong>{item.maximumScore}</strong>
                                        </div>

                                        <div className="small mb-2">
                                            Thời gian:{" "}
                                            <strong>
                                                {item.durationMinutes ? `${item.durationMinutes} phút` : "Không giới hạn"}
                                            </strong>
                                        </div>

                                        <div className="cm-portal-muted mb-1">
                                            Mở: {dateTime(item.availableAt)}
                                        </div>

                                        <div className="cm-portal-muted mb-3">
                                            Hạn nộp: {dateTime(item.dueAt)}
                                        </div>

                                        {item.latestAttemptNumber && (
                                            <div className="mb-3">
                                                <small className="cm-portal-muted me-2">
                                                    Lần làm #{item.latestAttemptNumber}
                                                </small>
                                                {attemptBadge(item.latestAttemptStatus)}
                                            </div>
                                        )}

                                        <div className="mt-auto">{actionButton(item)}</div>
                                    </Card.Body>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )}

                {!loading && meta.totalRecords > 0 && (
                    <div className="cm-class-pagination mt-4">
                        <span>Tổng <b>{meta.totalRecords}</b> · Trang {meta.currentPage}/{meta.totalPages}</span>

                        {meta.totalPages > 1 && (
                            <div className="cm-class-pagination-buttons">
                                <button type="button" disabled={meta.currentPage === 1}
                                    onClick={() => setPage(meta.currentPage - 1)}>‹</button>

                                {buildPages(meta.totalPages, meta.currentPage).map((item, index) =>
                                    typeof item === "number" ? (
                                        <button type="button" key={item}
                                            className={item === meta.currentPage ? "active" : ""}
                                            onClick={() => setPage(item)}>
                                            {item}
                                        </button>
                                    ) : (
                                        <span key={`${item}-${index}`}>...</span>
                                    )
                                )}

                                <button type="button" disabled={meta.currentPage === meta.totalPages}
                                    onClick={() => setPage(meta.currentPage + 1)}>›</button>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Assignments;