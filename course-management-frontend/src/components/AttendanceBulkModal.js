import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Form, Modal, Table } from "react-bootstrap";
import { authApis } from "../configs/Apis";
import DependentClassSelect from "./DependentClassSelect";
import DependentSessionSelect from "./DependentSessionSelect";

const AttendanceBulkModal = ({
    show,
    onHide,
    courses,
    classOptionsEndpoint,
    sessionOptionsEndpoint,
    attendanceEndpoint,
    onSaved
}) => {
    const [courseId, setCourseId] = useState("");
    const [classId, setClassId] = useState("");
    const [sessionId, setSessionId] = useState("");
    const [roster, setRoster] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");

    useEffect(() => {
        if (!show) {
            setCourseId("");
            setClassId("");
            setSessionId("");
            setRoster([]);
            setErr("");
        }
    }, [show]);

    useEffect(() => {
        let active = true;

        const loadRoster = async () => {
            if (!sessionId) {
                setRoster([]);
                return;
            }

            try {
                setLoading(true);
                setErr("");
                const res = await authApis().get(`${attendanceEndpoint}/roster`, { params: { sessionId } });
                if (!active) return;
                setRoster((Array.isArray(res.data) ? res.data : []).map(item => ({
                    ...item,
                    present: item.attendanceStatus === "PRESENT"
                        ? "true"
                        : item.attendanceStatus === "ABSENT" ? "false" : "",
                    note: item.note || ""
                })));
            } catch (ex) {
                if (active) {
                    setRoster([]);
                    setErr(ex.response?.data?.message || "Không thể tải danh sách học viên!");
                }
            } finally {
                if (active) setLoading(false);
            }
        };

        loadRoster();
        return () => { active = false; };
    }, [sessionId, attendanceEndpoint]);

    const markedCount = useMemo(() => roster.filter(item => item.present !== "").length, [roster]);

    const changeCourse = value => {
        setCourseId(value);
        setClassId("");
        setSessionId("");
        setRoster([]);
        setErr("");
    };

    const changeClass = value => {
        setClassId(value);
        setSessionId("");
        setRoster([]);
        setErr("");
    };

    const updateRow = (studentId, field, value) => {
        setRoster(current => current.map(item =>
            Number(item.studentId) === Number(studentId) ? { ...item, [field]: value } : item
        ));
    };

    const markAll = value => setRoster(current => current.map(item => ({ ...item, present: value })));

    const save = async () => {
        if (!courseId) return setErr("Vui lòng chọn khóa học!");
        if (!classId) return setErr("Vui lòng chọn lớp học!");
        if (!sessionId) return setErr("Vui lòng chọn buổi học!");

        const attendances = roster
            .filter(item => item.present !== "")
            .map(item => ({
                studentId: Number(item.studentId),
                present: item.present === "true",
                note: item.note.trim() || null
            }));

        if (!attendances.length)
            return setErr("Hãy chọn trạng thái cho ít nhất một học viên!");

        try {
            setSaving(true);
            setErr("");
            await authApis().put(`${attendanceEndpoint}/bulk`, {
                sessionId: Number(sessionId),
                attendances
            });
            onHide();
            await onSaved?.();
        } catch (ex) {
            setErr(ex.response?.data?.message || "Lưu điểm danh thất bại!");
        } finally {
            setSaving(false);
        }
    };

    return (
        <Modal show={show} onHide={onHide} size="xl" centered scrollable>
            <Modal.Header closeButton>
                <div>
                    <Modal.Title>Điểm danh theo lớp</Modal.Title>
                    <small className="text-muted">Chọn khóa học → lớp → buổi học → cập nhật danh sách học viên.</small>
                </div>
            </Modal.Header>

            <Modal.Body>
                {err && <Alert variant="danger">{err}</Alert>}

                <div className="row g-3 mb-4">
                    <div className="col-md-4">
                        <Form.Label>Khóa học</Form.Label>
                        <Form.Select value={courseId} onChange={e => changeCourse(e.target.value)}>
                            <option value="">-- Chọn khóa học --</option>
                            {courses.map(item => {
                                const id = item.id ?? item.courseId;
                                return <option key={id} value={id}>{item.name ?? item.courseName}</option>;
                            })}
                        </Form.Select>
                    </div>

                    <div className="col-md-4">
                        <Form.Label>Lớp học</Form.Label>
                        <DependentClassSelect courseId={courseId} value={classId} onChange={changeClass}
                            endpoint={classOptionsEndpoint} emptyLabel="-- Chọn lớp học --" />
                    </div>

                    <div className="col-md-4">
                        <Form.Label>Buổi học</Form.Label>
                        <DependentSessionSelect classId={classId} value={sessionId}
                            onChange={setSessionId} endpoint={sessionOptionsEndpoint}
                            emptyLabel="-- Chọn buổi học --" />
                    </div>
                </div>

                {!sessionId ? (
                    <div className="text-center text-muted py-5">Chọn khóa học, lớp và buổi học để tải danh sách học viên.</div>
                ) : loading ? (
                    <div className="text-center text-muted py-5">Đang tải danh sách học viên...</div>
                ) : roster.length === 0 ? (
                    <Alert variant="info" className="text-center">Lớp chưa có học viên ACTIVE.</Alert>
                ) : (
                    <>
                        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                            <div>
                                <strong>{roster.length}</strong> học viên · <strong>{markedCount}</strong> đã chọn trạng thái
                            </div>
                            <div className="d-flex gap-2">
                                <Button size="sm" variant="outline-success" onClick={() => markAll("true")}>Tất cả có mặt</Button>
                                <Button size="sm" variant="outline-danger" onClick={() => markAll("false")}>Tất cả vắng</Button>
                            </div>
                        </div>

                        <div className="table-responsive">
                            <Table hover size="sm" className="align-middle">
                                <thead className="table-light">
                                    <tr>
                                        <th>Học viên</th>
                                        <th style={{ width: 190 }}>Trạng thái</th>
                                        <th>Ghi chú</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {roster.map(item => (
                                        <tr key={item.studentId}>
                                            <td>
                                                <div className="fw-semibold">{item.studentName}</div>
                                                <small className="text-muted">@{item.username}</small>
                                            </td>
                                            <td>
                                                <Form.Select size="sm" value={item.present}
                                                    onChange={e => updateRow(item.studentId, "present", e.target.value)}>
                                                    <option value="">Chưa điểm danh</option>
                                                    <option value="true">Có mặt</option>
                                                    <option value="false">Vắng</option>
                                                </Form.Select>
                                            </td>
                                            <td>
                                                <Form.Control size="sm" value={item.note}
                                                    placeholder="Ghi chú nếu có..."
                                                    onChange={e => updateRow(item.studentId, "note", e.target.value)} />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    </>
                )}
            </Modal.Body>

            <Modal.Footer>
                <Button variant="outline-secondary" onClick={onHide} disabled={saving}>Đóng</Button>
                <Button onClick={save} disabled={saving || loading || !sessionId || roster.length === 0}>
                    {saving ? "Đang lưu..." : "Lưu điểm danh"}
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default AttendanceBulkModal;
