import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/Attempt.css";

const AttemptResult = () => {
    const { attemptId } = useParams();
    const nav = useNavigate();

    const [result, setResult] = useState(null);
    const [attempt, setAttempt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const loadResult = async () => {
        try {
            setLoading(true);
            setErr("");

            const [resultRes, attemptRes] = await Promise.all([
                authApis().get(`${endpoints.studentAssignments}/attempts/${attemptId}/result`),
                authApis().get(`${endpoints.studentAssignments}/attempts/${attemptId}`)
            ]);

            setResult(resultRes.data);
            setAttempt(attemptRes.data);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải kết quả!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadResult();
    }, [attemptId]);

    const statusBadge = status => {
        const config = {
            IN_PROGRESS: ["warning", "Đang làm"],
            SUBMITTED: ["info", "Đã nộp"],
            PENDING_GRADING: ["warning", "Chờ giáo viên chấm"],
            GRADED: ["success", "Đã chấm"]
        }[status] || ["secondary", status || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    const formatDuration = seconds => {
        if (seconds == null) return "-";

        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes} phút ${remainingSeconds} giây`;
    };

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN")
        : "-";

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    if (err)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container" style={{ maxWidth: 900 }}>
                    <Alert variant="danger">{err}</Alert>
                    <Button variant="outline-secondary" onClick={() => nav("/student/assignments")}>
                        ← Bài tập của tôi
                    </Button>
                </div>
            </div>
        );

    const hasEssay = (attempt?.questions || []).some(q => q.type === "ESSAY");
    const graded = result.status === "GRADED";
    const pending = result.status === "SUBMITTED" || result.status === "PENDING_GRADING";
    const scoreColumns = hasEssay ? 4 : 6;
    const courseDetailPath = attempt?.enrollmentId
        ? `/student/courses/${attempt.enrollmentId}`
        : "/student/courses";

    return (
        <div className="cm-portal-page cm-result-page">
            <div className="cm-portal-container cm-result-container">
                <Button variant="outline-secondary" size="sm" className="cm-portal-back"
                    onClick={() => nav(attempt?.enrollmentId ? courseDetailPath : "/student/assignments")}>
                    {attempt?.enrollmentId ? "← Quay về khóa học" : "← Bài tập của tôi"}
                </Button>

                <div className="cm-result-heading">
                    <span className="cm-portal-label">KẾT QUẢ BÀI LÀM</span>
                    <h1>{attempt?.assignmentName || "Kết quả bài làm"}</h1>
                    <div className="cm-result-heading-meta">
                        {statusBadge(result.status)}
                        <span>Lần làm #{result.attemptNumber}</span>
                        <span>Tối đa {attempt?.maximumScore ?? "-"} điểm</span>
                    </div>
                </div>

                {pending && (
                    <Alert variant="info" className="cm-result-alert">
                        <strong>Bài đã được nộp thành công.</strong>
                        {hasEssay
                            ? " Phần tự luận đang chờ giáo viên chấm. Điểm tổng sẽ được cập nhật sau khi hoàn tất chấm bài."
                            : " Hệ thống đang xử lý kết quả của bạn."}
                    </Alert>
                )}

                {graded && (
                    <Alert variant={result.passed === true ? "success" : result.passed === false ? "danger" : "info"}
                        className="cm-result-alert">
                        <strong>
                            {result.passed === true
                                ? "Bạn đã đạt yêu cầu của bài tập."
                                : result.passed === false
                                    ? "Bài làm chưa đạt yêu cầu."
                                    : "Bài đã được chấm."}
                        </strong>
                        {result.passed === true && " Tiến độ học tập đã được ghi nhận và bài tiếp theo sẽ được mở theo lộ trình."}
                    </Alert>
                )}

                <Card className="cm-portal-card cm-result-score-card">
                    <Card.Body>
                        <div className="cm-result-score-top">
                            <div>
                                <span className="cm-portal-label">{graded ? "TỔNG ĐIỂM" : "ĐIỂM HIỆN TẠI"}</span>
                                <div className="cm-result-main-score">
                                    <strong>{graded ? result.totalScore ?? "-" : result.autoScore ?? "-"}</strong>
                                    <span>/ {attempt?.maximumScore ?? "-"}</span>
                                </div>
                            </div>
                            <div className={`cm-result-state ${result.passed === false ? "is-failed" : ""}`}>
                                {pending ? "Đang chờ chấm" : result.passed === true ? "Đạt yêu cầu" : result.passed === false ? "Chưa đạt" : "Đã chấm"}
                            </div>
                        </div>

                        <Row className="g-3 mt-1">
                            <Col md={scoreColumns}>
                                <div className="cm-portal-summary h-100">
                                    <span>Điểm tự động</span>
                                    <strong>{result.autoScore ?? "-"}</strong>
                                </div>
                            </Col>

                            {hasEssay && (
                                <Col md={4}>
                                    <div className="cm-portal-summary h-100">
                                        <span>Điểm tự luận</span>
                                        <strong>{graded ? result.essayScore ?? "-" : "Chờ chấm"}</strong>
                                    </div>
                                </Col>
                            )}

                            <Col md={scoreColumns}>
                                <div className="cm-portal-summary h-100">
                                    <span>Tổng điểm</span>
                                    <strong>{graded ? result.totalScore ?? "-" : "-"}</strong>
                                </div>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="cm-portal-card cm-result-info-card">
                    <Card.Body>
                        <div className="cm-portal-section-heading">
                            <span>THÔNG TIN</span>
                            <h5>Thông tin bài làm</h5>
                            <p>Thông tin của lần làm bài hiện tại.</p>
                        </div>

                        <Row className="g-4">
                            <Col sm={6} md={4}>
                                <div className="cm-result-info-item">
                                    <span>Lần làm</span>
                                    <strong>#{result.attemptNumber}</strong>
                                </div>
                            </Col>

                            <Col sm={6} md={4}>
                                <div className="cm-result-info-item">
                                    <span>Thời gian làm</span>
                                    <strong>{formatDuration(result.durationSeconds)}</strong>
                                </div>
                            </Col>

                            <Col sm={12} md={4}>
                                <div className="cm-result-info-item">
                                    <span>Nộp lúc</span>
                                    <strong>{formatDateTime(result.submittedAt)}</strong>
                                </div>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                {result.comment && (
                    <Card className="cm-portal-card cm-result-comment-card">
                        <Card.Body>
                            <div className="cm-portal-section-heading">
                                <span>NHẬN XÉT</span>
                                <h5>Nhận xét của giáo viên</h5>
                            </div>
                            <p className="mb-0">{result.comment}</p>
                        </Card.Body>
                    </Card>
                )}

                <div className="cm-result-actions">
                    <Button variant="outline-secondary" onClick={() => nav("/student/assignments")}>
                        Bài tập của tôi
                    </Button>

                    {!graded && (
                        <Button variant="outline-primary" onClick={loadResult}>
                            Cập nhật kết quả
                        </Button>
                    )}

                    <Button onClick={() => nav(courseDetailPath)}>
                        {attempt?.enrollmentId ? "Quay về khóa học" : "Khóa học của tôi"}
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default AttemptResult;
