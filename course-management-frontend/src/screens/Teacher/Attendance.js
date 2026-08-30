import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Attendance = () => {
    const { classId, sessionId } = useParams();
    const nav = useNavigate();

    const [session, setSession] = useState(null);
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const loadData = async () => {
        try {
            setLoading(true);
            setErr("");

            const [attendanceRes, sessionRes] = await Promise.all([
                authApis().get(endpoints.teacherSessionAttendance(classId, sessionId)),
                authApis().get(endpoints.teacherClassSessions(classId))
            ]);

            const attendanceData = Array.isArray(attendanceRes.data) ? attendanceRes.data : [];
            const sessionsData = Array.isArray(sessionRes.data) ? sessionRes.data : [];

            setStudents(attendanceData.map(item => ({
                ...item,
                present: item.attendanceStatus === "PRESENT"
                    ? true
                    : item.attendanceStatus === "ABSENT" ? false : null,
                note: item.note || "",
                dirty: false
            })));

            setSession(
                sessionsData.find(item => Number(item.sessionId) === Number(sessionId)) || null
            );
        } catch (ex) {
            console.error("Load teacher attendance error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách điểm danh!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [classId, sessionId]);

    const changePresent = (studentId, value) => {
        setStudents(current => current.map(student =>
            student.studentId === studentId ? { ...student, present: value, dirty: true } : student
        ));
    };

    const changeNote = (studentId, value) => {
        setStudents(current => current.map(student =>
            student.studentId === studentId ? { ...student, note: value, dirty: true } : student
        ));
    };

    const saveAllAttendances = async () => {
        const changes = students.filter(student => student.dirty);
        if (!changes.length) return;

        const invalid = changes.find(student => student.present === null);
        if (invalid)
            return setErr(`Vui lòng chọn trạng thái điểm danh cho ${invalid.studentName}!`);

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().put(
                endpoints.teacherSessionAttendance(classId, sessionId),
                {
                    attendances: changes.map(student => ({
                        studentId: student.studentId,
                        present: student.present,
                        note: student.note.trim() || null
                    }))
                }
            );

            const updatedById = new Map(
                (Array.isArray(res.data) ? res.data : []).map(item => [item.studentId, item])
            );

            setStudents(current => current.map(student => {
                const updated = updatedById.get(student.studentId);
                if (!updated) return student;

                return {
                    ...student,
                    ...updated,
                    present: updated.attendanceStatus === "PRESENT"
                        ? true
                        : updated.attendanceStatus === "ABSENT" ? false : null,
                    note: updated.note || "",
                    dirty: false
                };
            }));

            setSuccess(`Đã lưu điểm danh cho ${changes.length} học viên.`);
        } catch (ex) {
            console.error("Bulk update teacher attendance error:", ex);
            setErr(ex.response?.data?.message || "Không thể lưu điểm danh!");
        } finally {
            setSaving(false);
        }
    };

    const formatDate = value => value
        ? new Date(value).toLocaleDateString("vi-VN", {
            weekday: "long", day: "2-digit", month: "2-digit", year: "numeric"
        })
        : "-";

    const formatTime = value => value
        ? new Date(value).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
        : "-";

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit", day: "2-digit",
            month: "2-digit", year: "numeric"
        })
        : "-";

    const statusBadge = student => {
        if (student.present === true) return <Badge bg="success">Có mặt</Badge>;
        if (student.present === false) return <Badge bg="danger">Vắng</Badge>;
        return <Badge bg="secondary">Chưa điểm danh</Badge>;
    };

    const sessionStatus = status => {
        if (status === "ONGOING") return <Badge bg="success">Đang diễn ra</Badge>;
        if (status === "UPCOMING") return <Badge bg="primary">Sắp diễn ra</Badge>;
        if (status === "ENDED") return <Badge bg="secondary">Đã kết thúc</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const presentCount = students.filter(item => item.present === true).length;
    const absentCount = students.filter(item => item.present === false).length;
    const notMarkedCount = students.filter(item => item.present === null).length;
    const changedCount = students.filter(item => item.dirty).length;

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    if (err && students.length === 0)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container">
                    <Alert variant="danger">{err}</Alert>
                    <Button variant="outline-secondary"
                        onClick={() => nav(`/teacher/classes/${classId}`)}>
                        ← Quay lại lớp học
                    </Button>
                </div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <Button variant="outline-secondary" size="sm" className="cm-portal-back"
                    onClick={() => nav(`/teacher/classes/${classId}`)}>
                    ← Quay lại lớp học
                </Button>

                <div className="cm-portal-heading">
                    <div>
                        <span>ĐIỂM DANH</span>
                        <h1>{session?.title || `Buổi học #${sessionId}`}</h1>
                        <p>Quản lý trạng thái tham gia của học viên trong buổi học.</p>
                    </div>
                </div>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-4">
                        <Row className="align-items-center g-4">
                            <Col lg={6}>
                                <span className="cm-portal-label">THÔNG TIN BUỔI HỌC</span>
                                <h5 className="cm-portal-title mt-2 mb-2">
                                    {session?.title || `Buổi học #${sessionId}`}
                                </h5>

                                {session && (
                                    <>
                                        <div className="cm-portal-muted">
                                            {formatDate(session.startTime)} · {formatTime(session.startTime)} - {formatTime(session.endTime)}
                                        </div>
                                        <div className="mt-3">{sessionStatus(session.status)}</div>
                                    </>
                                )}
                            </Col>

                            <Col lg={6}>
                                <Row className="g-2">
                                    <Col xs={4}>
                                        <div className="cm-portal-summary h-100">
                                            <span>Có mặt</span>
                                            <strong>{presentCount}</strong>
                                        </div>
                                    </Col>

                                    <Col xs={4}>
                                        <div className="cm-portal-summary h-100">
                                            <span>Vắng</span>
                                            <strong>{absentCount}</strong>
                                        </div>
                                    </Col>

                                    <Col xs={4}>
                                        <div className="cm-portal-summary h-100">
                                            <span>Chưa điểm danh</span>
                                            <strong>{notMarkedCount}</strong>
                                        </div>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

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

                <Card className="cm-portal-card">
                    <Card.Body className="p-4">
                        <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div className="cm-portal-section-heading mb-0">
                                <span>HỌC VIÊN</span>
                                <h5>Danh sách điểm danh</h5>
                                <p>Chỉnh điểm danh cho nhiều học viên rồi lưu một lần.</p>
                            </div>

                            <Button onClick={saveAllAttendances} disabled={saving || changedCount === 0}>
                                {saving ? "Đang lưu..." : `Lưu điểm danh${changedCount ? ` (${changedCount})` : ""}`}
                            </Button>
                        </div>

                        {students.length === 0 ? (
                            <div className="cm-portal-empty">Lớp học chưa có học viên.</div>
                        ) : (
                            <div className="table-responsive">
                                <Table hover align="middle" className="cm-portal-table">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Học viên</th>
                                            <th>Trạng thái</th>
                                            <th style={{ minWidth: 200 }}>Điểm danh</th>
                                            <th style={{ minWidth: 230 }}>Ghi chú</th>
                                            <th>Ghi nhận lúc</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {students.map((student, index) => (
                                            <tr key={student.studentId}>
                                                <td>{index + 1}</td>

                                                <td>
                                                    <strong className="d-block">{student.studentName}</strong>
                                                    <small className="cm-portal-muted">@{student.username}</small>
                                                </td>

                                                <td>
                                                    <div className="d-flex align-items-center gap-2 flex-wrap">
                                                        {statusBadge(student)}
                                                        {student.dirty && <Badge bg="warning" text="dark">Chưa lưu</Badge>}
                                                    </div>
                                                </td>

                                                <td>
                                                    <div className="d-flex gap-3 flex-wrap">
                                                        <Form.Check type="radio"
                                                            name={`attendance-${student.studentId}`}
                                                            label="Có mặt"
                                                            checked={student.present === true}
                                                            onChange={() => changePresent(student.studentId, true)}
                                                        />

                                                        <Form.Check type="radio"
                                                            name={`attendance-${student.studentId}`}
                                                            label="Vắng"
                                                            checked={student.present === false}
                                                            onChange={() => changePresent(student.studentId, false)}
                                                        />
                                                    </div>
                                                </td>

                                                <td>
                                                    <Form.Control size="sm" value={student.note}
                                                        onChange={e => changeNote(student.studentId, e.target.value)}
                                                        placeholder="Nhập ghi chú..."
                                                    />
                                                </td>

                                                <td>
                                                    <small className="cm-portal-muted">
                                                        {formatDateTime(student.attendedAt)}
                                                    </small>
                                                </td>

                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        )}
                    </Card.Body>
                </Card>
            </div>
        </div>
    );
};

export default Attendance;