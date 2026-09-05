import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../hooks/useExplicitSearchFilters";
import { authApis } from "../configs/Apis";
import MySpinner from "./MySpinner";
import AttendanceBulkModal from "./AttendanceBulkModal";
import DependentClassSelect from "./DependentClassSelect";
import DependentSessionSelect from "./DependentSessionSelect";

const AttendanceManagement = ({ attendanceEndpoint, courseOptionsEndpoint, classOptionsEndpoint, sessionOptionsEndpoint }) => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "classId", "sessionId", "present"]);
    const [attendances, setAttendances] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");
    const [showBulk, setShowBulk] = useState(false);
    const [editing, setEditing] = useState(null);
    const [editForm, setEditForm] = useState({ present: "true", note: "" });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const classId = q.get("classId") || "";
    const sessionId = q.get("sessionId") || "";
    const present = q.get("present") || "";

    const loadAttendances = async () => {
        try {
            setLoading(true);
            setErr("");
            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (classId) params.classId = classId;
            if (sessionId) params.sessionId = sessionId;
            if (present !== "") params.present = present;

            const res = await authApis().get(attendanceEndpoint, { params });
            const data = res.data || {};
            setAttendances(data.attendances || data.content || data.items || []);
            setTotalPages(Number(data.totalPages || 1));
            setTotalRecords(Number(data.totalRecords || 0));
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải dữ liệu điểm danh!");
        } finally {
            setLoading(false);
        }
    };

    const loadCourses = async () => {
        try {
            const res = await authApis().get(courseOptionsEndpoint);
            setCourses(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            setCourses([]);
        }
    };

    useEffect(() => { loadCourses(); }, [courseOptionsEndpoint]);
    useEffect(() => { loadAttendances(); }, [q]);

    const search = e => {
        e.preventDefault();
        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.classId) params.classId = draftFilters.classId;
        if (draftFilters.sessionId) params.sessionId = draftFilters.sessionId;
        if (draftFilters.present !== "") params.present = draftFilters.present;
        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["classId", "sessionId"] : name === "classId" ? ["sessionId"] : [];
        setDraftFilter(name, value, resetKeys);
    };

    const clearFilters = () => {
        setKw("");
        resetDraftFilters();
        setQ({ page: "1" });
    };

    const openEdit = item => {
        setEditing(item);
        setEditForm({ present: item.present === false ? "false" : "true", note: item.note || "" });
    };

    const saveEdit = async e => {
        e.preventDefault();
        try {
            setSaving(true);
            setErr("");
            await authApis().put(`${attendanceEndpoint}/${editing.id ?? editing.attendanceId}`, {
                present: editForm.present === "true",
                note: editForm.note.trim() || null
            });
            setEditing(null);
            setSuccess("Cập nhật điểm danh thành công!");
            await loadAttendances();
        } catch (ex) {
            setErr(ex.response?.data?.message || "Cập nhật điểm danh thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const changePage = next => {
        const params = Object.fromEntries(q);
        params.page = String(next);
        setQ(params);
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý điểm danh</h2>
                    <p className="text-muted mb-0">Tổng cộng <strong>{totalRecords}</strong> lượt điểm danh</p>
                </div>
                <Button onClick={() => setShowBulk(true)}>+ Điểm danh</Button>
            </div>

            {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}
            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={3}>
                                <Form.Control value={kw} onChange={e => setKw(e.target.value)} placeholder="Tìm học viên..." />
                            </Col>
                            <Col lg={2}>
                                <Form.Select value={draftFilters.courseId} onChange={e => changeFilter("courseId", e.target.value)}>
                                    <option value="">Tất cả khóa học</option>
                                    {courses.map(item => {
                                        const id = item.id ?? item.courseId;
                                        return <option key={id} value={id}>{item.name ?? item.courseName}</option>;
                                    })}
                                </Form.Select>
                            </Col>
                            <Col lg={2}>
                                <DependentClassSelect courseId={draftFilters.courseId} value={draftFilters.classId}
                                    onChange={value => changeFilter("classId", value)} endpoint={classOptionsEndpoint}
                                    emptyLabel="Tất cả lớp học" />
                            </Col>
                            <Col lg={2}>
                                <DependentSessionSelect classId={draftFilters.classId} value={draftFilters.sessionId}
                                    onChange={value => changeFilter("sessionId", value)}
                                    endpoint={sessionOptionsEndpoint} emptyLabel="Tất cả buổi học" />
                            </Col>
                            <Col lg={1}>
                                <Form.Select value={draftFilters.present} onChange={e => changeFilter("present", e.target.value)}>
                                    <option value="">Tất cả</option>
                                    <option value="true">Có mặt</option>
                                    <option value="false">Vắng mặt</option>
                                </Form.Select>
                            </Col>
                            <Col lg={2} className="d-flex gap-2">
                                <Button type="submit">Tìm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? <div className="text-center p-5"><MySpinner /></div> : attendances.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">Không tìm thấy dữ liệu điểm danh.</Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th><th>Học viên</th><th>Lớp</th><th>Buổi học</th>
                                        <th>Điểm danh</th><th>Ghi chú</th><th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {attendances.map(item => (
                                        <tr key={item.id ?? item.attendanceId}>
                                            <td>{item.id ?? item.attendanceId}</td>
                                            <td>
                                                <div className="fw-semibold">{item.studentName ?? item.studentFullName ?? "-"}</div>
                                                {item.username && <small className="text-muted">@{item.username}</small>}
                                            </td>
                                            <td>{item.className ?? item.courseClassName ?? "-"}</td>
                                            <td>{item.sessionTitle ?? item.onlineSessionTitle ?? "-"}</td>
                                            <td>{item.present
                                                ? <Badge bg="success">Có mặt</Badge>
                                                : <Badge bg="danger">Vắng mặt</Badge>}</td>
                                            <td>{item.note || "-"}</td>
                                            <td><Button size="sm" variant="outline-primary" onClick={() => openEdit(item)}>Sửa</Button></td>
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
                        <Pagination.Prev disabled={page === 1} onClick={() => changePage(page - 1)} />
                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                            <Pagination.Item key={number} active={number === page} onClick={() => changePage(number)}>
                                {number}
                            </Pagination.Item>
                        ))}
                        <Pagination.Next disabled={page === totalPages} onClick={() => changePage(page + 1)} />
                    </Pagination>
                </div>
            )}

            <AttendanceBulkModal show={showBulk} onHide={() => setShowBulk(false)} courses={courses}
                classOptionsEndpoint={classOptionsEndpoint} sessionOptionsEndpoint={sessionOptionsEndpoint}
                attendanceEndpoint={attendanceEndpoint} onSaved={async () => {
                    setSuccess("Lưu điểm danh thành công!");
                    await loadAttendances();
                }} />

            <Modal show={!!editing} onHide={() => setEditing(null)} centered>
                <Form onSubmit={saveEdit}>
                    <Modal.Header closeButton><Modal.Title>Cập nhật điểm danh</Modal.Title></Modal.Header>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Trạng thái</Form.Label>
                            <Form.Select value={editForm.present}
                                onChange={e => setEditForm({ ...editForm, present: e.target.value })}>
                                <option value="true">Có mặt</option>
                                <option value="false">Vắng mặt</option>
                            </Form.Select>
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Ghi chú</Form.Label>
                            <Form.Control as="textarea" rows={3} value={editForm.note}
                                onChange={e => setEditForm({ ...editForm, note: e.target.value })} />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={() => setEditing(null)} disabled={saving}>Hủy</Button>
                        <Button type="submit" disabled={saving}>{saving ? "Đang lưu..." : "Lưu"}</Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default AttendanceManagement;
