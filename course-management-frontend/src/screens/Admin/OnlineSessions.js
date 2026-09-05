import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import AsyncUserSelect from "../../components/AsyncUserSelect";

const OnlineSessions = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "classId", "teacherId", "date"]);

    const [sessions, setSessions] = useState([]);
    const [courses, setCourses] = useState([]);
    const [filterClasses, setFilterClasses] = useState([]);
    const [formClasses, setFormClasses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingSession, setEditingSession] = useState(null);
    const [form, setForm] = useState({
        title: "", courseId: "", classId: "", teacherId: "",
        startTime: "", endTime: "", meetingUrl: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const classId = q.get("classId") || "";
    const teacherId = q.get("teacherId") || "";
    const date = q.get("date") || "";

    const getSessionId = s => s.id ?? s.sessionId;
    const getClassId = c => c.id ?? c.classId;
    const getTeacherId = t => t.id ?? t.userId ?? t.teacherId;
    const getTeacherName = t => t.fullName || t.name || t.username || "-";

    const loadSessions = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (classId) params.classId = classId;
            if (teacherId) params.teacherId = teacherId;
            if (date) params.date = date;

            const res = await authApis().get(endpoints.adminOnlineSessions, { params });
            const data = res.data;

            setSessions(data.sessions || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load sessions error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách buổi học!");
        } finally {
            setLoading(false);
        }
    };

    const loadCourses = async () => {
        try {
            const res = await authApis().get(endpoints.adminCourseOptions);
            setCourses(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load course options error:", ex);
        }
    };

    const loadClassOptions = async (selectedCourseId, target = "filter") => {
        if (!selectedCourseId) {
            target === "filter" ? setFilterClasses([]) : setFormClasses([]);
            return;
        }

        try {
            const res = await authApis().get(endpoints.adminClassOptions, {
                params: { courseId: selectedCourseId }
            });
            const data = Array.isArray(res.data) ? res.data : [];
            target === "filter" ? setFilterClasses(data) : setFormClasses(data);
        } catch (ex) {
            target === "filter" ? setFilterClasses([]) : setFormClasses([]);
        }
    };

    useEffect(() => { loadCourses(); }, []);
    useEffect(() => { loadClassOptions(draftFilters.courseId, "filter"); }, [draftFilters.courseId]);

    useEffect(() => {
        loadSessions();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.classId) params.classId = draftFilters.classId;
        if (draftFilters.teacherId) params.teacherId = draftFilters.teacherId;
        if (draftFilters.date) params.date = draftFilters.date;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["classId"] : [];
        setDraftFilter(name, value, resetKeys);
    };

    const clearFilters = () => {
        setKw("");
        resetDraftFilters();
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const openAddModal = () => {
        setEditingSession(null);
        setForm({
            title: "", courseId: "", classId: "", teacherId: "",
            startTime: "", endTime: "", meetingUrl: ""
        });
        setFormClasses([]);
        setShowModal(true);
    };

    const toDateTimeInput = value => {
        if (!value) return "";
        return value.length >= 16 ? value.substring(0, 16) : value;
    };

    const openEditModal = session => {
        setEditingSession(session);
        const selectedCourseId = session.courseId ?? session.courseClass?.course?.id ?? "";
        setForm({
            title: session.title || "",
            courseId: selectedCourseId,
            classId: session.classId ?? session.courseClass?.id ?? "",
            teacherId: session.teacherId ?? session.teacher?.id ?? "",
            startTime: toDateTimeInput(session.startTime),
            endTime: toDateTimeInput(session.endTime),
            meetingUrl: session.meetingUrl || ""
        });
        loadClassOptions(selectedCourseId, "form");
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingSession(null);
    };

    const saveSession = async e => {
        e.preventDefault();

        if (!form.title.trim()) return setErr("Tiêu đề buổi học không được để trống!");
        if (!form.classId) return setErr("Vui lòng chọn lớp học!");
        if (!form.teacherId) return setErr("Vui lòng chọn giáo viên!");
        if (!form.meetingUrl.trim())
            return setErr("Vui lòng nhập link phòng học!");
        if (!form.startTime || !form.endTime)
            return setErr("Vui lòng nhập thời gian bắt đầu và kết thúc!");

        if (new Date(form.endTime) <= new Date(form.startTime))
            return setErr("Thời gian kết thúc phải sau thời gian bắt đầu!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                title: form.title.trim(),
                classId: Number(form.classId),
                teacherId: Number(form.teacherId),
                startTime: form.startTime,
                endTime: form.endTime,
                meetingUrl: form.meetingUrl.trim()
            };

            let res;

            if (editingSession)
                res = await authApis().put(
                    `${endpoints.adminOnlineSessions}/${getSessionId(editingSession)}`,
                    body
                );
            else
                res = await authApis().post(endpoints.adminOnlineSessions, body);

            setSuccess(
                res.data?.message ||
                (editingSession ? "Cập nhật buổi học thành công!" : "Thêm buổi học thành công!")
            );

            closeModal();
            await loadSessions();
        } catch (ex) {
            console.error("Save session error:", ex);
            setErr(ex.response?.data?.message || "Lưu buổi học thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const className = session =>
        session.className || session.courseClassName || session.courseClass?.name ||
        (session.classId ? `ID ${session.classId}` : "-");

    const teacherName = session =>
        session.teacherName || session.teacherFullName ||
        (session.teacher ? getTeacherName(session.teacher) : session.teacherId ? `ID ${session.teacherId}` : "-");

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit",
            day: "2-digit", month: "2-digit", year: "numeric"
        })
        : "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý buổi học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> buổi học
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm buổi học</Button>
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
                            <Col lg={2}>
                                <Form.Control placeholder="Tìm buổi học..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.courseId}
                                    onChange={e => changeFilter("courseId", e.target.value)}>
                                    <option value="">Tất cả khóa học</option>
                                    {courses.map(c => (
                                        <option key={c.id ?? c.courseId} value={c.id ?? c.courseId}>{c.name}</option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.classId} disabled={!draftFilters.courseId}
                                    onChange={e => changeFilter("classId", e.target.value)}>
                                    <option value="">{courseId ? "Tất cả lớp của khóa học" : "Chọn khóa học trước"}</option>
                                    {filterClasses.map(c => (
                                        <option key={getClassId(c)} value={getClassId(c)}>{c.name}</option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <AsyncUserSelect endpoint={endpoints.adminTeacherOptions} value={draftFilters.teacherId}
                                    onChange={option => changeFilter("teacherId", option?.id || "")}
                                    placeholder="Tìm giáo viên..." />
                            </Col>

                            <Col lg={2}>
                                <Form.Control type="date" value={draftFilters.date}
                                    onChange={e => changeFilter("date", e.target.value)} />
                            </Col>

                            <Col lg={2} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? (
                        <div className="text-center p-5"><MySpinner /></div>
                    ) : sessions.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy buổi học.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Buổi học</th>
                                        <th>Lớp</th>
                                        <th>Giáo viên</th>
                                        <th>Bắt đầu</th>
                                        <th>Kết thúc</th>
                                        <th>Phòng học</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {sessions.map(session => (
                                        <tr key={getSessionId(session)}>
                                            <td>{getSessionId(session)}</td>
                                            <td className="fw-semibold">{session.title}</td>
                                            <td>{className(session)}</td>
                                            <td>{teacherName(session)}</td>
                                            <td>{formatDateTime(session.startTime)}</td>
                                            <td>{formatDateTime(session.endTime)}</td>

                                            <td>
                                                {session.meetingUrl ? (
                                                    <a href={session.meetingUrl} target="_blank" rel="noreferrer">
                                                        Tham gia
                                                    </a>
                                                ) : "-"}
                                            </td>

                                            <td>
                                                <Button size="sm" variant="outline-primary"
                                                    onClick={() => openEditModal(session)}>
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

            <Modal show={showModal} onHide={closeModal} centered size="lg">
                <Form onSubmit={saveSession}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingSession ? "Cập nhật buổi học" : "Thêm buổi học"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tiêu đề buổi học</Form.Label>
                            <Form.Control value={form.title}
                                onChange={e => setForm({ ...form, title: e.target.value })}
                                required />
                        </Form.Group>

                        <Row>
                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Khóa học</Form.Label>
                                    <Form.Select value={form.courseId} onChange={e => {
                                        const value = e.target.value;
                                        setForm({ ...form, courseId: value, classId: "" });
                                        loadClassOptions(value, "form");
                                    }} required>
                                        <option value="">-- Chọn khóa học --</option>
                                        {courses.map(c => (
                                            <option key={c.id ?? c.courseId} value={c.id ?? c.courseId}>{c.name}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Lớp học</Form.Label>
                                    <Form.Select value={form.classId} disabled={!form.courseId}
                                        onChange={e => setForm({ ...form, classId: e.target.value })} required>
                                        <option value="">{form.courseId ? "-- Chọn lớp học --" : "-- Chọn khóa học trước --"}</option>
                                        {formClasses.map(c => (
                                            <option key={getClassId(c)} value={getClassId(c)}>{c.name}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Giáo viên</Form.Label>
                                    <AsyncUserSelect endpoint={endpoints.adminTeacherOptions} value={form.teacherId}
                                        onChange={option => setForm({ ...form, teacherId: option?.id || "" })}
                                        placeholder="Tìm giáo viên..." required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Thời gian bắt đầu</Form.Label>

                                    <Form.Control type="datetime-local" value={form.startTime}
                                        onChange={e => setForm({ ...form, startTime: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Thời gian kết thúc</Form.Label>

                                    <Form.Control type="datetime-local"
                                        min={form.startTime || undefined}
                                        value={form.endTime}
                                        onChange={e => setForm({ ...form, endTime: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label>Link phòng học</Form.Label>
                                    <Form.Control
                                        type="url"
                                        placeholder="https://meet.google.com/..."
                                        value={form.meetingUrl}
                                        onChange={e => setForm({ ...form, meetingUrl: e.target.value })}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
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

export default OnlineSessions;
