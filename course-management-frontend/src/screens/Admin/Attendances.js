import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Attendances = () => {
    const [q, setQ] = useSearchParams();

    const [attendances, setAttendances] = useState([]);
    const [classes, setClasses] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingAttendance, setEditingAttendance] = useState(null);
    const [form, setForm] = useState({
        sessionId: "", studentId: "", present: "true", note: ""
    });

    const page = Number(q.get("page")) || 1;
    const classId = q.get("classId") || "";
    const sessionId = q.get("sessionId") || "";
    const present = q.get("present") || "";

    const getAttendanceId = a => a.id ?? a.attendanceId;
    const getClassId = c => c.id ?? c.classId;
    const getSessionId = s => s.id ?? s.sessionId;
    const getStudentId = s => s.id ?? s.studentId ?? s.userId;
    const getSessionClassId = s => s.classId ?? s.courseClass?.id;

    const loadAttendances = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (classId) params.classId = classId;
            if (sessionId) params.sessionId = sessionId;
            if (present !== "") params.present = present;

            const res = await authApis().get(endpoints.adminAttendances, { params });
            const data = res.data;

            setAttendances(data.attendances || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load attendances error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải dữ liệu điểm danh!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const [classRes, sessionRes] = await Promise.all([
                authApis().get(endpoints.adminClassOptions),
                authApis().get(endpoints.adminOnlineSessionOptions)
            ]);

            setClasses(Array.isArray(classRes.data) ? classRes.data : []);
            setSessions(Array.isArray(sessionRes.data) ? sessionRes.data : []);
        } catch (ex) {
            console.error("Load attendance options error:", ex);
        }
    };

    const loadStudents = async selectedSessionId => {
        if (!selectedSessionId) {
            setStudents([]);
            return;
        }

        try {
            const res = await authApis().get(`${endpoints.adminAttendances}/students`, {
                params: { sessionId: selectedSessionId }
            });

            setStudents(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load students error:", ex);
            setStudents([]);
            setErr(ex.response?.data?.message || "Không thể tải danh sách học viên!");
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadAttendances();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (classId) params.classId = classId;
        if (sessionId) params.sessionId = sessionId;
        if (present !== "") params.present = present;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const params = Object.fromEntries(q);

        if (value !== "") params[name] = value;
        else delete params[name];

        if (name === "classId") delete params.sessionId;

        params.page = "1";
        setQ(params);
    };

    const clearFilters = () => {
        setKw("");
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const openAddModal = () => {
        setEditingAttendance(null);
        setStudents([]);
        setForm({ sessionId: "", studentId: "", present: "true", note: "" });
        setShowModal(true);
    };

    const openEditModal = attendance => {
        setEditingAttendance(attendance);
        setForm({
            sessionId: "",
            studentId: "",
            present: attendance.present === false ? "false" : "true",
            note: attendance.note || ""
        });
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingAttendance(null);
        setStudents([]);
    };

    const changeSession = async value => {
        setForm(prev => ({ ...prev, sessionId: value, studentId: "" }));
        await loadStudents(value);
    };

    const saveAttendance = async e => {
        e.preventDefault();

        if (!editingAttendance && !form.sessionId)
            return setErr("Vui lòng chọn buổi học!");

        if (!editingAttendance && !form.studentId)
            return setErr("Vui lòng chọn học viên!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                present: form.present === "true",
                note: form.note.trim() || null
            };

            let res;

            if (editingAttendance) {
                res = await authApis().put(
                    `${endpoints.adminAttendances}/${getAttendanceId(editingAttendance)}`,
                    body
                );
            } else {
                res = await authApis().post(endpoints.adminAttendances, {
                    sessionId: Number(form.sessionId),
                    studentId: Number(form.studentId),
                    ...body
                });
            }

            setSuccess(
                res.data?.message ||
                (editingAttendance ? "Cập nhật điểm danh thành công!" : "Điểm danh thành công!")
            );

            closeModal();
            await loadAttendances();
        } catch (ex) {
            console.error("Save attendance error:", ex);
            setErr(ex.response?.data?.message || "Lưu điểm danh thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const className = a =>
        a.className ||
        a.courseClassName ||
        a.onlineSession?.courseClass?.name ||
        "-";

    const sessionName = a =>
        a.sessionTitle ||
        a.onlineSessionTitle ||
        a.session?.title ||
        a.onlineSession?.title ||
        "-";

    const studentName = a =>
        a.studentName ||
        a.studentFullName ||
        a.student?.fullName ||
        "-";

    const username = a =>
        a.studentUsername ||
        a.student?.username ||
        "";

    const filteredSessions = classId
        ? sessions.filter(s => String(getSessionClassId(s)) === String(classId))
        : sessions;

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý điểm danh</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> lượt điểm danh
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Điểm danh</Button>
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

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={3}>
                                <Form.Control placeholder="Tìm học viên..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={classId}
                                    onChange={e => changeFilter("classId", e.target.value)}>
                                    <option value="">Tất cả lớp</option>

                                    {classes.map(c => (
                                        <option key={getClassId(c)} value={getClassId(c)}>
                                            {c.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={3}>
                                <Form.Select value={sessionId}
                                    onChange={e => changeFilter("sessionId", e.target.value)}>
                                    <option value="">Tất cả buổi học</option>

                                    {filteredSessions.map(s => (
                                        <option key={getSessionId(s)} value={getSessionId(s)}>
                                            {s.title}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={present}
                                    onChange={e => changeFilter("present", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="true">Có mặt</option>
                                    <option value="false">Vắng mặt</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2} className="d-flex gap-2">
                                <Button type="submit">Tìm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>
                                    Xóa lọc
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? (
                        <div className="text-center p-5"><MySpinner /></div>
                    ) : attendances.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy dữ liệu điểm danh.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Học viên</th>
                                        <th>Lớp</th>
                                        <th>Buổi học</th>
                                        <th>Điểm danh</th>
                                        <th>Ghi chú</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {attendances.map(a => (
                                        <tr key={getAttendanceId(a)}>
                                            <td>{getAttendanceId(a)}</td>

                                            <td>
                                                <div className="fw-semibold">{studentName(a)}</div>
                                                {username(a) && (
                                                    <small className="text-muted">@{username(a)}</small>
                                                )}
                                            </td>

                                            <td>{className(a)}</td>
                                            <td>{sessionName(a)}</td>

                                            <td>
                                                {a.present
                                                    ? <Badge bg="success">Có mặt</Badge>
                                                    : <Badge bg="danger">Vắng mặt</Badge>}
                                            </td>

                                            <td>{a.note || "-"}</td>

                                            <td>
                                                <Button size="sm" variant="outline-primary"
                                                    onClick={() => openEditModal(a)}>
                                                    Sửa
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            {!loading && totalPages > 1 && (
                <div className="d-flex justify-content-center mt-4">
                    <Pagination>
                        <Pagination.First disabled={page === 1} onClick={() => changePage(1)} />

                        <Pagination.Prev disabled={page === 1}
                            onClick={() => changePage(Math.max(page - 1, 1))} />

                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                            <Pagination.Item key={number} active={number === page}
                                onClick={() => changePage(number)}>
                                {number}
                            </Pagination.Item>
                        ))}

                        <Pagination.Next disabled={page === totalPages}
                            onClick={() => changePage(Math.min(page + 1, totalPages))} />

                        <Pagination.Last disabled={page === totalPages}
                            onClick={() => changePage(totalPages)} />
                    </Pagination>
                </div>
            )}

            <Modal show={showModal} onHide={closeModal} centered>
                <Form onSubmit={saveAttendance}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingAttendance ? "Cập nhật điểm danh" : "Điểm danh học viên"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        {!editingAttendance && (
                            <>
                                <Form.Group className="mb-3">
                                    <Form.Label>Buổi học</Form.Label>

                                    <Form.Select value={form.sessionId}
                                        onChange={e => changeSession(e.target.value)}
                                        required>
                                        <option value="">-- Chọn buổi học --</option>

                                        {sessions.map(s => (
                                            <option key={getSessionId(s)} value={getSessionId(s)}>
                                                {s.title}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Học viên</Form.Label>

                                    <Form.Select value={form.studentId}
                                        onChange={e => setForm({ ...form, studentId: e.target.value })}
                                        disabled={!form.sessionId}
                                        required>
                                        <option value="">-- Chọn học viên --</option>

                                        {students.map(s => (
                                            <option key={getStudentId(s)} value={getStudentId(s)}>
                                                {s.fullName} ({s.username})
                                            </option>
                                        ))}
                                    </Form.Select>

                                    {form.sessionId && students.length === 0 && (
                                        <Form.Text className="text-muted">
                                            Không có học viên đang hoạt động trong lớp này.
                                        </Form.Text>
                                    )}
                                </Form.Group>
                            </>
                        )}

                        <Form.Group className="mb-3">
                            <Form.Label>Trạng thái điểm danh</Form.Label>

                            <Form.Select value={form.present}
                                onChange={e => setForm({ ...form, present: e.target.value })}>
                                <option value="true">Có mặt</option>
                                <option value="false">Vắng mặt</option>
                            </Form.Select>
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Ghi chú</Form.Label>

                            <Form.Control as="textarea" rows={3}
                                placeholder="Ghi chú nếu có..."
                                value={form.note}
                                onChange={e => setForm({ ...form, note: e.target.value })} />
                        </Form.Group>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving}>
                            {saving ? "Đang lưu..." : "Lưu"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default Attendances;