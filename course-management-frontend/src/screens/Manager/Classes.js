import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import AsyncUserSelect from "../../components/AsyncUserSelect";

const Classes = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "teacherId", "status"]);

    const [classes, setClasses] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingClass, setEditingClass] = useState(null);
    const [form, setForm] = useState({
        name: "", courseId: "", teacherId: "",
        startDate: "", endDate: "", maxStudents: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const teacherId = q.get("teacherId") || "";
    const status = q.get("status") || "";

    const getClassId = c => c.id ?? c.classId;
    const getCourseId = c => c.id ?? c.courseId;
    const getTeacherId = t => t.id ?? t.userId ?? t.teacherId;
    const getTeacherName = t => t.fullName || t.name || t.username || "-";

    const loadClasses = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (teacherId) params.teacherId = teacherId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.managerClasses, { params });
            const data = res.data;

            setClasses(data.classes || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load classes error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách lớp học!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const courseRes = await authApis().get(endpoints.managerCourseOptions);
            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
        } catch (ex) {
            console.error("Load class options error:", ex);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadClasses();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.teacherId) params.teacherId = draftFilters.teacherId;
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
        setEditingClass(null);
        setForm({
            name: "", courseId: "", teacherId: "",
            startDate: "", endDate: "", maxStudents: ""
        });
        setShowModal(true);
    };

    const openEditModal = item => {
        setEditingClass(item);
        setForm({
            name: item.name || "",
            courseId: item.courseId ?? item.course?.id ?? "",
            teacherId: item.teacherId ?? item.teacher?.id ?? "",
            startDate: item.startDate || "",
            endDate: item.endDate || "",
            maxStudents: item.maxStudents ?? ""
        });
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingClass(null);
    };

    const saveClass = async e => {
        e.preventDefault();

        if (!form.name.trim()) return setErr("Tên lớp không được để trống!");
        if (!form.courseId) return setErr("Vui lòng chọn khóa học!");
        if (!form.startDate || !form.endDate)
            return setErr("Vui lòng chọn ngày bắt đầu và kết thúc!");

        if (form.endDate < form.startDate)
            return setErr("Ngày kết thúc phải sau ngày bắt đầu!");

        if (!form.maxStudents || Number(form.maxStudents) < 1)
            return setErr("Số học viên tối đa phải lớn hơn 0!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                name: form.name.trim(),
                courseId: Number(form.courseId),
                teacherId: form.teacherId ? Number(form.teacherId) : null,
                startDate: form.startDate,
                endDate: form.endDate,
                maxStudents: Number(form.maxStudents)
            };

            let res;

            if (editingClass)
                res = await authApis().put(`${endpoints.managerClasses}/${getClassId(editingClass)}`, body);
            else
                res = await authApis().post(endpoints.managerClasses, body);

            setSuccess(
                res.data?.message ||
                (editingClass ? "Cập nhật lớp học thành công!" : "Thêm lớp học thành công!")
            );

            closeModal();
            await loadClasses();
        } catch (ex) {
            console.error("Save class error:", ex);
            setErr(ex.response?.data?.message || "Lưu lớp học thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const cancelClass = async classId => {
        if (!window.confirm("Bạn chắc chắn muốn hủy lớp học này?")) return;

        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.managerClasses}/${classId}/status`,
                { status: "CANCELED" }
            );

            setSuccess(res.data?.message || "Hủy lớp học thành công!");
            await loadClasses();
        } catch (ex) {
            console.error("Cancel class error:", ex);
            setErr(ex.response?.data?.message || "Hủy lớp học thất bại!");
            await loadClasses();
        }
    };

    const courseName = item =>
        item.courseName ||
        item.course?.name ||
        courses.find(c =>
            String(getCourseId(c)) === String(item.courseId ?? item.course?.id)
        )?.name ||
        "-";

    const teacherName = item => {
        if (item.teacherName) return item.teacherName;
        if (item.teacherFullName) return item.teacherFullName;
        if (item.teacher) return getTeacherName(item.teacher);

        return item.teacherId ? `ID ${item.teacherId}` : "Chưa phân công";
    };

    const statusBadge = value => {
        const config = {
            UPCOMING: ["info", "Sắp diễn ra"],
            ACTIVE: ["success", "Đang học"],
            COMPLETED: ["secondary", "Hoàn thành"],
            CANCELED: ["danger", "Đã hủy"]
        }[value] || ["secondary", value || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý lớp học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> lớp học
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm lớp học</Button>
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
                            <Col lg={4}>
                                <Form.Control placeholder="Tìm theo tên lớp..."
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
                                <AsyncUserSelect endpoint={endpoints.managerTeacherOptions} value={draftFilters.teacherId}
                                    onChange={option => changeFilter("teacherId", option?.id || "")}
                                    placeholder="Tìm giáo viên..." />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="UPCOMING">Sắp diễn ra</option>
                                    <option value="ACTIVE">Đang học</option>
                                    <option value="COMPLETED">Hoàn thành</option>
                                    <option value="CANCELED">Đã hủy</option>
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
                    ) : classes.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy lớp học.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Lớp học</th>
                                        <th>Khóa học</th>
                                        <th>Giáo viên</th>
                                        <th>Thời gian</th>
                                        <th>Sĩ số tối đa</th>
                                        <th>Trạng thái</th>
                                        <th style={{ minWidth: 150 }}>Hủy lớp</th>
                                        <th>Chỉnh sửa</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {classes.map(item => {
                                        const id = getClassId(item);

                                        return (
                                            <tr key={id}>
                                                <td>{id}</td>
                                                <td className="fw-semibold">{item.name}</td>
                                                <td>{courseName(item)}</td>
                                                <td>{teacherName(item)}</td>

                                                <td>
                                                    {item.startDate || "-"}
                                                    <br />
                                                    <small className="text-muted">
                                                        đến {item.endDate || "-"}
                                                    </small>
                                                </td>

                                                <td>{item.maxStudents ?? "-"}</td>
                                                <td>{statusBadge(item.status)}</td>

                                                <td>
                                                    {item.status === "CANCELED" ? (
                                                        <span className="text-muted small">Đã hủy</span>
                                                    ) : (
                                                        <Button size="sm" variant="outline-danger"
                                                            onClick={() => cancelClass(id)}>
                                                            Hủy lớp
                                                        </Button>
                                                    )}
                                                </td>

                                                <td>
                                                    <Button size="sm" variant="outline-primary"
                                                        onClick={() => openEditModal(item)}>
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
                <Form onSubmit={saveClass}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingClass ? "Cập nhật lớp học" : "Thêm lớp học"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên lớp</Form.Label>
                            <Form.Control value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required />
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Khóa học</Form.Label>

                                    <Form.Select value={form.courseId}
                                        onChange={e => setForm({ ...form, courseId: e.target.value })}
                                        required>
                                        <option value="">-- Chọn khóa học --</option>

                                        {courses.map(c => (
                                            <option key={getCourseId(c)} value={getCourseId(c)}>
                                                {c.name}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Giáo viên</Form.Label>

                                    <AsyncUserSelect endpoint={endpoints.managerTeacherOptions} value={form.teacherId}
                                        onChange={option => setForm({ ...form, teacherId: option?.id || "" })}
                                        placeholder="Tìm giáo viên để phân công..." required />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Ngày bắt đầu</Form.Label>
                                    <Form.Control type="date" value={form.startDate}
                                        onChange={e => setForm({ ...form, startDate: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Ngày kết thúc</Form.Label>
                                    <Form.Control type="date" value={form.endDate}
                                        min={form.startDate || undefined}
                                        onChange={e => setForm({ ...form, endDate: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group>
                            <Form.Label>Sĩ số tối đa</Form.Label>
                            <Form.Control type="number" min="1" value={form.maxStudents}
                                onChange={e => setForm({ ...form, maxStudents: e.target.value })}
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

export default Classes;
