import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Row } from "react-bootstrap";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Schedule = () => {
    const [sessions, setSessions] = useState([]);
    const [page, setPage] = useState(1);
    const [meta, setMeta] = useState({ currentPage: 1, totalPages: 1, totalRecords: 0 });

    const [includeEnded, setIncludeEnded] = useState(false);
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");

    const [appliedIncludeEnded, setAppliedIncludeEnded] = useState(false);
    const [appliedFrom, setAppliedFrom] = useState("");
    const [appliedTo, setAppliedTo] = useState("");

    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const loadSchedule = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.studentSchedule, {
                params: {
                    page,
                    includeEnded: appliedIncludeEnded,
                    ...(appliedFrom ? { from: appliedFrom } : {}),
                    ...(appliedTo ? { to: appliedTo } : {})
                }
            });

            if (Array.isArray(res.data)) {
                setSessions(res.data);
                setMeta({ currentPage: 1, totalPages: 1, totalRecords: res.data.length });
                return;
            }

            setSessions(Array.isArray(res.data?.sessions) ? res.data.sessions : []);

            const nextMeta = {
                currentPage: Number(res.data?.currentPage || 1),
                totalPages: Number(res.data?.totalPages || 1),
                totalRecords: Number(res.data?.totalRecords || 0)
            };

            setMeta(nextMeta);
            if (nextMeta.currentPage !== page) setPage(nextMeta.currentPage);
        } catch (ex) {
            console.error("Load student schedule error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải lịch học!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSchedule();
    }, [page, appliedIncludeEnded, appliedFrom, appliedTo]);

    const applyFilter = () => {
        if (from && to && to < from)
            return setErr("Ngày kết thúc không được trước ngày bắt đầu!");

        setErr("");
        setPage(1);
        setAppliedIncludeEnded(includeEnded);
        setAppliedFrom(from);
        setAppliedTo(to);
    };

    const resetFilter = () => {
        setIncludeEnded(false);
        setFrom("");
        setTo("");
        setPage(1);
        setAppliedIncludeEnded(false);
        setAppliedFrom("");
        setAppliedTo("");
        setErr("");
    };

    const changeIncludeEnded = value => {
        setIncludeEnded(value);
        setPage(1);
        setAppliedIncludeEnded(value);
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

    const formatDate = value => value
        ? new Date(value).toLocaleDateString("vi-VN", {
            weekday: "long", day: "2-digit", month: "2-digit", year: "numeric"
        })
        : "-";

    const formatTime = value => value
        ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
        : "-";

    const statusBadge = status => {
        if (status === "ONGOING") return <Badge bg="success">Đang diễn ra</Badge>;
        if (status === "UPCOMING") return <Badge bg="primary">Sắp diễn ra</Badge>;
        if (status === "ENDED") return <Badge bg="secondary">Đã kết thúc</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const joinButton = session => {
        if (session.status === "ONGOING") {
            if (!session.meetingUrl)
                return <Button size="sm" variant="secondary" disabled>Chưa có link học</Button>;

            return (
                <Button as="a" size="sm" variant="success" href={session.meetingUrl}
                    target="_blank" rel="noreferrer">
                    Tham gia buổi học
                </Button>
            );
        }

        if (session.status === "UPCOMING")
            return <Button size="sm" variant="outline-secondary" disabled>Chưa đến giờ</Button>;

        if (session.status === "ENDED")
            return <Button size="sm" variant="outline-secondary" disabled>Đã kết thúc</Button>;

        return null;
    };

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <div className="cm-portal-heading">
                    <div>
                        <span>LỊCH HỌC</span>
                        <h1>Lịch học của tôi</h1>
                        <p>Theo dõi các buổi học online sắp tới và lịch sử buổi học.</p>
                    </div>

                    <div className="cm-portal-summary">
                        <span>Tổng buổi học</span>
                        <strong>{meta.totalRecords}</strong>
                    </div>
                </div>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-4">
                        <Row className="g-3 align-items-end">
                            <Col md={4}>
                                <Form.Label>Từ ngày</Form.Label>
                                <Form.Control type="date" value={from} onChange={e => setFrom(e.target.value)} />
                            </Col>

                            <Col md={4}>
                                <Form.Label>Đến ngày</Form.Label>
                                <Form.Control type="date" value={to} onChange={e => setTo(e.target.value)} />
                            </Col>

                            <Col md={4}>
                                <div className="d-flex gap-2">
                                    <Button onClick={applyFilter}>Lọc</Button>
                                    <Button variant="outline-secondary" onClick={resetFilter}>Đặt lại</Button>
                                </div>
                            </Col>

                            <Col xs={12}>
                                <Form.Check type="switch" id="include-ended"
                                    label="Hiển thị các buổi đã kết thúc"
                                    checked={includeEnded}
                                    onChange={e => changeIncludeEnded(e.target.checked)}
                                />
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                {err && <Alert variant="danger">{err}</Alert>}

                {loading ? (
                    <div className="text-center py-5"><MySpinner /></div>
                ) : sessions.length === 0 ? (
                    <div className="cm-portal-empty">
                        Chưa có buổi học nào trong khoảng thời gian này.
                    </div>
                ) : (
                    <div className="d-flex flex-column gap-3">
                        {sessions.map(session => (
                            <Card key={session.sessionId} className="cm-portal-card cm-portal-hover">
                                <Card.Body className="p-4">
                                    <Row className="align-items-center g-3">
                                        <Col lg={3}>
                                            <span className="cm-portal-label">THỜI GIAN</span>

                                            <div className="cm-portal-title mt-2">
                                                {formatTime(session.startTime)} - {formatTime(session.endTime)}
                                            </div>

                                            <small className="cm-portal-muted">
                                                {formatDate(session.startTime)}
                                            </small>
                                        </Col>

                                        <Col lg={6}>
                                            <div className="d-flex align-items-center gap-2 mb-2">
                                                <h5 className="cm-portal-title mb-0">{session.title}</h5>
                                                {statusBadge(session.status)}
                                            </div>

                                            <div>
                                                <span className="cm-portal-muted">Khóa học: </span>
                                                <strong>{session.courseName}</strong>
                                            </div>

                                            <div className="cm-portal-muted mt-1">
                                                Lớp: {session.className}
                                            </div>

                                            {session.teacherName && (
                                                <div className="cm-portal-muted">
                                                    Giảng viên: {session.teacherName}
                                                </div>
                                            )}
                                        </Col>

                                        <Col lg={3} className="text-lg-end">
                                            {joinButton(session)}
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        ))}
                    </div>
                )}

                {!loading && meta.totalRecords > 0 && (
                    <div className="cm-class-pagination mt-4">
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

export default Schedule;
