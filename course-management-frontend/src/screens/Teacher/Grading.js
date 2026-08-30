import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Form, InputGroup } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/TeacherGrading.css";

const Grading = () => {
    const nav = useNavigate();
    const location = useLocation();

    const [attempts, setAttempts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState(location.state?.message || "");
    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(1);
    const [meta, setMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const loadPending = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(`${endpoints.teacherGrading}/pending`, {
                params: {
                    page,
                    ...(keyword.trim() ? { kw: keyword.trim() } : {})
                }
            });

            if (Array.isArray(res.data)) {
                setAttempts(res.data);
                setMeta({ currentPage: 1, totalPages: 1, totalRecords: res.data.length });
                return;
            }

            setAttempts(Array.isArray(res.data?.attempts) ? res.data.attempts : []);

            const nextMeta = {
                currentPage: Number(res.data?.currentPage || 1),
                totalPages: Number(res.data?.totalPages || 1),
                totalRecords: Number(res.data?.totalRecords || 0)
            };

            setMeta(nextMeta);

            if (nextMeta.currentPage !== page)
                setPage(nextMeta.currentPage);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải danh sách bài chờ chấm!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(loadPending, 300);
        return () => clearTimeout(timer);
    }, [page, keyword]);

    useEffect(() => {
        if (location.state?.message)
            nav(location.pathname, { replace: true, state: {} });
    }, []);

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

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container cm-teacher-grading-page">
                <div className="cm-portal-heading cm-grading-heading">
                    <div>
                        <span>CHẤM BÀI</span>
                        <h1>Bài chờ chấm</h1>
                        <p>Các bài có phần tự luận đang chờ giáo viên hoàn tất chấm điểm.</p>
                    </div>

                    <div className="cm-grading-summary">
                        <span>Đang chờ xử lý</span>
                        <strong>{meta.totalRecords}</strong>
                        <small>bài làm</small>
                    </div>
                </div>

                {success && (
                    <Alert variant="success" dismissible onClose={() => setSuccess("")}>
                        {success}
                    </Alert>
                )}

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                <Card className="cm-portal-card cm-grading-toolbar">
                    <Card.Body>
                        <div className="cm-grading-toolbar-content">
                            <InputGroup className="cm-grading-search">
                                <InputGroup.Text>Tìm</InputGroup.Text>
                                <Form.Control value={keyword}
                                    onChange={e => {
                                        setKeyword(e.target.value);
                                        setPage(1);
                                    }}
                                    placeholder="Học viên, khóa học hoặc bài tập..."
                                />
                            </InputGroup>

                            <Button variant="outline-primary" onClick={loadPending} disabled={loading}>
                                {loading ? "Đang tải..." : "Làm mới"}
                            </Button>
                        </div>
                    </Card.Body>
                </Card>

                {loading ? (
                    <div className="text-center py-5"><MySpinner /></div>
                ) : attempts.length === 0 ? (
                    <Card className="cm-portal-card">
                        <Card.Body>
                            <div className="cm-portal-empty cm-grading-empty">
                                <strong>{keyword.trim() ? "Không tìm thấy kết quả phù hợp" : "Không còn bài chờ chấm"}</strong>
                                <span>
                                    {keyword.trim()
                                        ? "Thử tìm bằng tên học viên, khóa học hoặc tên bài tập khác."
                                        : "Các bài học viên nộp có tự luận sẽ xuất hiện tại đây."}
                                </span>
                            </div>
                        </Card.Body>
                    </Card>
                ) : (
                    <div className="cm-grading-list">
                        {attempts.map(item => (
                            <Card className="cm-portal-card cm-grading-item" key={item.attemptId}>
                                <Card.Body>
                                    <div className="cm-grading-item-main">
                                        <div className="cm-grading-student-avatar">
                                            {(item.studentName || "?").trim().charAt(0).toUpperCase()}
                                        </div>

                                        <div className="cm-grading-item-info">
                                            <div className="cm-grading-item-top">
                                                <div>
                                                    <span className="cm-grading-item-label">HỌC VIÊN</span>
                                                    <h5>{item.studentName}</h5>
                                                </div>

                                            </div>

                                            <div className="cm-grading-assignment">
                                                <strong>{item.assignmentName}</strong>
                                                <span>{item.courseName}</span>
                                            </div>

                                            <div className="cm-grading-meta">
                                                <span>Lần làm <b>#{item.attemptNumber}</b></span>
                                                <span>Nộp lúc <b>{dateTime(item.submittedAt)}</b></span>
                                                <span>Mã bài làm <b>#{item.attemptId}</b></span>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="d-flex align-items-center gap-3 flex-shrink-0">
                                        <Badge bg="warning" text="dark">Chờ chấm</Badge>

                                        <Button onClick={() => nav(`/teacher/grading/${item.attemptId}`)}>
                                            Chấm bài
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        ))}
                    </div>
                )}

                {!loading && meta.totalRecords > 0 && (
                    <div className="cm-class-pagination mt-3">
                        <span>Tổng <b>{meta.totalRecords}</b> · Trang {meta.currentPage}/{meta.totalPages}</span>

                        {meta.totalPages > 1 && (
                            <div className="cm-class-pagination-buttons">
                                <button type="button" disabled={meta.currentPage === 1}
                                    onClick={() => setPage(meta.currentPage - 1)}>
                                    ‹
                                </button>

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
                                    onClick={() => setPage(meta.currentPage + 1)}>
                                    ›
                                </button>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Grading;
