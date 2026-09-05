import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const LearningPaths = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "status"]);

    const [paths, setPaths] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingPath, setEditingPath] = useState(null);
    const [form, setForm] = useState({
        name: "", courseId: "", assignmentsPerDay: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const status = q.get("status") || "";

    const getPathId = p => p.id ?? p.learningPathId;
    const getCourseId = c => c.id ?? c.courseId;

    const loadPaths = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminLearningPaths, { params });
            const data = res.data;

            setPaths(data.learningPaths || data.paths || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load learning paths error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách lộ trình!");
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

    useEffect(() => {
        loadCourses();
    }, []);

    useEffect(() => {
        loadPaths();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.status) params.status = draftFilters.status;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        setDraftFilter(name, value);
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
        setEditingPath(null);
        setForm({ name: "", courseId: "", assignmentsPerDay: "" });
        setShowModal(true);
    };

    const openEditModal = path => {
        setEditingPath(path);
        setForm({
            name: path.name || "",
            courseId: path.courseId ?? path.course?.id ?? "",
            assignmentsPerDay: path.assignmentsPerDay ?? ""
        });
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingPath(null);
        setForm({ name: "", courseId: "", assignmentsPerDay: "" });
    };

    const savePath = async e => {
        e.preventDefault();

        if (!form.name.trim()) return setErr("Tên lộ trình không được để trống!");
        if (!editingPath && !form.courseId) return setErr("Vui lòng chọn khóa học!");
        if (!form.assignmentsPerDay || Number(form.assignmentsPerDay) < 1)
            return setErr("Số bài tập mỗi ngày phải lớn hơn 0!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            let res;

            if (editingPath) {
                res = await authApis().put(
                    `${endpoints.adminLearningPaths}/${getPathId(editingPath)}`,
                    {
                        name: form.name.trim(),
                        assignmentsPerDay: Number(form.assignmentsPerDay)
                    }
                );
            } else {
                res = await authApis().post(endpoints.adminLearningPaths, {
                    name: form.name.trim(),
                    courseId: Number(form.courseId),
                    assignmentsPerDay: Number(form.assignmentsPerDay)
                });
            }

            setSuccess(
                res.data?.message ||
                (editingPath ? "Cập nhật lộ trình thành công!" : "Thêm lộ trình thành công!")
            );

            closeModal();
            await loadPaths();
        } catch (ex) {
            console.error("Save learning path error:", ex);
            setErr(ex.response?.data?.message || "Lưu lộ trình thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const updateStatus = async (id, newStatus) => {
        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminLearningPaths}/${id}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadPaths();
        } catch (ex) {
            console.error("Update learning path status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadPaths();
        }
    };

    const courseName = path =>
        path.courseName ||
        path.course?.name ||
        courses.find(c =>
            String(getCourseId(c)) === String(path.courseId ?? path.course?.id)
        )?.name ||
        "-";

    const statusBadge = value => value === "ACTIVE"
        ? <Badge bg="success">Hoạt động</Badge>
        : <Badge bg="secondary">Không hoạt động</Badge>;

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý lộ trình học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> lộ trình
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm lộ trình</Button>
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
                            <Col lg={5}>
                                <Form.Control placeholder="Tìm theo tên lộ trình..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.courseId}
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
                                <Form.Select value={draftFilters.status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE">Hoạt động</option>
                                    <option value="INACTIVE">Không hoạt động</option>
                                </Form.Select>
                            </Col>

                            <Col lg={3} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
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
                    ) : paths.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy lộ trình.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Tên lộ trình</th>
                                        <th>Khóa học</th>
                                        <th>Bài tập / ngày</th>
                                        <th>Trạng thái</th>
                                        <th style={{ minWidth: 150 }}>Cập nhật</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {paths.map(path => {
                                        const id = getPathId(path);

                                        return (
                                            <tr key={id}>
                                                <td>{id}</td>
                                                <td className="fw-semibold">{path.name}</td>
                                                <td>{courseName(path)}</td>
                                                <td>{path.assignmentsPerDay}</td>
                                                <td>{statusBadge(path.status)}</td>

                                                <td>
                                                    <Form.Select size="sm" value={path.status}
                                                        onChange={e => updateStatus(id, e.target.value)}>
                                                        <option value="ACTIVE">Hoạt động</option>
                                                        <option value="INACTIVE">Không hoạt động</option>
                                                    </Form.Select>
                                                </td>

                                                <td>
                                                    <Button size="sm" variant="outline-primary"
                                                        onClick={() => openEditModal(path)}>
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

            <Modal show={showModal} onHide={closeModal} centered>
                <Form onSubmit={savePath}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingPath ? "Cập nhật lộ trình" : "Thêm lộ trình"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên lộ trình</Form.Label>
                            <Form.Control value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Khóa học</Form.Label>

                            <Form.Select value={form.courseId}
                                onChange={e => setForm({ ...form, courseId: e.target.value })}
                                disabled={!!editingPath}
                                required={!editingPath}>
                                <option value="">-- Chọn khóa học --</option>

                                {courses.map(c => (
                                    <option key={getCourseId(c)} value={getCourseId(c)}>
                                        {c.name}
                                    </option>
                                ))}
                            </Form.Select>

                            {editingPath && (
                                <Form.Text className="text-muted">
                                    Không thể thay đổi khóa học của lộ trình.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Số bài tập mỗi ngày</Form.Label>
                            <Form.Control type="number" min="1"
                                value={form.assignmentsPerDay}
                                onChange={e => setForm({ ...form, assignmentsPerDay: e.target.value })}
                                required />
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

export default LearningPaths;
