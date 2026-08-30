import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams, useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Assignments = () => {
    const [q, setQ] = useSearchParams();
    const nav = useNavigate();
    const [assignments, setAssignments] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingAssignment, setEditingAssignment] = useState(null);
    const [lessons, setLessons] = useState([]);
    const [form, setForm] = useState({
        name: "",
        courseId: "",
        lessonId: "",
        type: "PRACTICE",
        maximumScore: 100,
        durationMinutes: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const type = q.get("type") || "";
    const status = q.get("status") || "";

    const getAssignmentId = a => a.id ?? a.assignmentId;
    const getCourseId = c => c.id ?? c.courseId;

    const loadLessons = async (courseId) => {
        try {
            const res = await authApis().get(
                `${endpoints.adminLessons}?courseId=${courseId}`
            );
    
            setLessons(
                Array.isArray(res.data)
                    ? res.data
                    : res.data.lessons || []
            );
    
        } catch (ex) {
            console.error(ex);
            setLessons([]);
        }
    };
    const loadAssignments = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (type) params.type = type;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminAssignments, { params });
            const data = res.data;

            setAssignments(data.assignments || data.items || data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load assignments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách bài tập!");
        } finally {
            setLoading(false);
        }
    };

    const loadCourses = async () => {
        try {
            const res = await authApis().get(endpoints.adminCourseOptions);
            setCourses(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load courses error:", ex);
        }
    };

    useEffect(() => {
        loadCourses();
    }, []);

    useEffect(() => {
        loadAssignments();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (courseId) params.courseId = courseId;
        if (type) params.type = type;
        if (status) params.status = status;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const params = Object.fromEntries(q);

        if (value) params[name] = value;
        else delete params[name];

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

    const resetForm = () => {
        setForm({
            name: "",
            courseId: "",
            lessonId: "",
            type: "PRACTICE",
            maximumScore: 100,
            durationMinutes: ""
        });
    
        setLessons([]);
    };

    const openAddModal = () => {
        setEditingAssignment(null);
        resetForm();
        setShowModal(true);
    };

    const openEditModal = assignment => {
        setEditingAssignment(assignment);
        setForm({
            name: assignment.name || "",
            courseId: assignment.courseId ?? assignment.course?.id ?? "",
            lessonId: assignment.lessonId ?? "",
            type: assignment.type || "PRACTICE",
            maximumScore: assignment.maximumScore ?? 100,
            durationMinutes: assignment.durationMinutes ?? ""
        });
        
        if (assignment.courseId)
            loadLessons(assignment.courseId);
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingAssignment(null);
        resetForm();
    };

    const saveAssignment = async e => {
        e.preventDefault();

        if (!form.name.trim()) return setErr("Tên bài tập không được để trống!");
        if (!editingAssignment && !form.courseId)
            return setErr("Vui lòng chọn khóa học!");

        if (!editingAssignment && !form.lessonId)
            return setErr("Vui lòng chọn bài học!");

        if (!form.maximumScore || Number(form.maximumScore) <= 0)
            return setErr("Điểm tối đa phải lớn hơn 0!");

        if (form.type === "TEST" && (!form.durationMinutes || Number(form.durationMinutes) <= 0))
            return setErr("Bài kiểm tra bắt buộc phải có thời gian làm bài!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                name: form.name.trim(),
                type: form.type,
                maximumScore: Number(form.maximumScore),
                durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : null
            };

            let res;

            if (editingAssignment) {
                res = await authApis().put(
                    `${endpoints.adminAssignments}/${getAssignmentId(editingAssignment)}`,
                    body
                );
            } else {
                res = await authApis().post(
                    endpoints.adminAssignments,
                    {
                        courseId:Number(form.courseId),
                        lessonId:Number(form.lessonId),
                        ...body
                    }
                );
            }

            setSuccess(
                res.data?.message ||
                (editingAssignment ? "Cập nhật bài tập thành công!" : "Thêm bài tập thành công!")
            );

            closeModal();
            await loadAssignments();
        } catch (ex) {
            console.error("Save assignment error:", ex);
            setErr(ex.response?.data?.message || "Lưu bài tập thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const updateStatus = async (id, newStatus) => {
        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminAssignments}/${id}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadAssignments();
        } catch (ex) {
            console.error("Update status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadAssignments();
        }
    };

    const courseName = assignment =>
        assignment.courseName ||
        assignment.course?.name ||
        courses.find(c =>
            String(getCourseId(c)) === String(assignment.courseId ?? assignment.course?.id)
        )?.name ||
        "-";

    const typeBadge = value => value === "TEST"
        ? <Badge bg="warning" text="dark">Bài kiểm tra</Badge>
        : <Badge bg="info">Bài luyện tập</Badge>;

    const statusBadge = value => value === "ACTIVE"
        ? <Badge bg="success">Hoạt động</Badge>
        : <Badge bg="secondary">Ngừng hoạt động</Badge>;

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý bài tập</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> bài tập
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm bài tập</Button>
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
                                <Form.Control placeholder="Tìm tên bài tập..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={3}>
                                <Form.Select value={courseId}
                                    onChange={e => changeFilter("courseId", e.target.value)}>
                                    <option value="">Tất cả khóa học</option>

                                    {courses.map(c => (
                                        <option key={getCourseId(c)} value={getCourseId(c)}>
                                            {c.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={type}
                                    onChange={e => changeFilter("type", e.target.value)}>
                                    <option value="">Tất cả loại</option>
                                    <option value="PRACTICE">Bài luyện tập</option>
                                    <option value="TEST">Bài kiểm tra</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE">Hoạt động</option>
                                    <option value="INACTIVE">Ngừng hoạt động</option>
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
                    ) : assignments.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy bài tập.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Bài tập</th>
                                        <th>Khóa học</th>
                                        <th>Loại</th>
                                        <th>Điểm tối đa</th>
                                        <th>Thời gian</th>
                                        <th>Trạng thái</th>
                                        <th style={{ minWidth: 160 }}>Cập nhật</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {assignments.map(assignment => {
                                        const id = getAssignmentId(assignment);

                                        return (
                                            <tr key={id}>
                                                <td>{id}</td>
                                                <td className="fw-semibold">{assignment.name}</td>
                                                <td>{courseName(assignment)}</td>
                                                <td>{typeBadge(assignment.type)}</td>
                                                <td>{assignment.maximumScore}</td>

                                                <td>
                                                    {assignment.durationMinutes
                                                        ? `${assignment.durationMinutes} phút`
                                                        : <span className="text-muted">Không giới hạn</span>}
                                                </td>

                                                <td>{statusBadge(assignment.status)}</td>

                                                <td>
                                                    <Form.Select size="sm" value={assignment.status}
                                                        onChange={e => updateStatus(id, e.target.value)}>
                                                        <option value="ACTIVE">Hoạt động</option>
                                                        <option value="INACTIVE">Ngừng hoạt động</option>
                                                    </Form.Select>
                                                </td>

                                                <td>

                                                    <Button
                                                        size="sm"
                                                        className="me-2"
                                                        onClick={() =>
                                                            nav(`/admin/questions?assignmentId=${id}`)
                                                        }
                                                    >
                                                        Câu hỏi
                                                    </Button>


                                                    <Button
                                                        size="sm"
                                                        variant="outline-primary"
                                                        onClick={() => openEditModal(assignment)}
                                                    >
                                                        Sửa
                                                    </Button>

                                                </td>
                                            </tr>
                                        );
                                    })}
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

                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                            <Pagination.Item key={n} active={n === page}
                                onClick={() => changePage(n)}>
                                {n}
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
                <Form onSubmit={saveAssignment}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingAssignment ? "Cập nhật bài tập" : "Thêm bài tập"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên bài tập</Form.Label>
                            <Form.Control value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Khóa học</Form.Label>

                            <Form.Select
                                value={form.courseId}
                                onChange={e => {
                                    const value = e.target.value;

                                    setForm({
                                        ...form,
                                        courseId: value,
                                        lessonId: ""
                                    });

                                    if (value)
                                        loadLessons(value);
                                    else
                                        setLessons([]);
                                }}
                                disabled={!!editingAssignment}
                                required={!editingAssignment}
                            >
                                <option value="">-- Chọn khóa học --</option>

                                {courses.map(c => (
                                    <option key={getCourseId(c)} value={getCourseId(c)}>
                                        {c.name}
                                    </option>
                                ))}
                            </Form.Select>

                            {editingAssignment && (
                                <Form.Text className="text-muted">
                                    Không thể thay đổi khóa học sau khi tạo bài.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Bài học</Form.Label>

                            <Form.Select
                                value={form.lessonId}
                                onChange={e =>
                                    setForm({
                                        ...form,
                                        lessonId:e.target.value
                                    })
                                }
                                required
                            >
                                <option value="">
                                    -- Chọn bài học --
                                </option>

                                {lessons.map(l => (
                                    <option key={l.id} value={l.id}>
                                        {l.name}
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Loại bài</Form.Label>

                                    <Form.Select value={form.type}
                                        onChange={e => setForm({ ...form, type: e.target.value })}>
                                        <option value="PRACTICE">Bài luyện tập</option>
                                        <option value="TEST">Bài kiểm tra</option>
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Điểm tối đa</Form.Label>

                                    <Form.Control type="number" min="1" step="0.01"
                                        value={form.maximumScore}
                                        onChange={e => setForm({ ...form, maximumScore: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group>
                            <Form.Label>
                                Thời gian làm bài
                                {form.type === "TEST" && <span className="text-danger"> *</span>}
                            </Form.Label>

                            <div className="input-group">
                                <Form.Control type="number" min="1"
                                    value={form.durationMinutes}
                                    onChange={e => setForm({ ...form, durationMinutes: e.target.value })}
                                    required={form.type === "TEST"} />

                                <span className="input-group-text">phút</span>
                            </div>

                            <Form.Text className="text-muted">
                                {form.type === "TEST"
                                    ? "Bài kiểm tra bắt buộc phải có thời gian làm bài."
                                    : "Bài luyện tập có thể để trống nếu không giới hạn thời gian."}
                            </Form.Text>
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

export default Assignments;