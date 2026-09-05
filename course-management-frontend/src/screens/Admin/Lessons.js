import { useEffect, useRef, useState } from "react";
import {
    Alert, Button, Card, Col, Form, Modal,
    Pagination, Row, Table
} from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Lessons = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "moduleId"]);

    const [lessons, setLessons] = useState([]);
    const [courses, setCourses] = useState([]);
    const [filterModules, setFilterModules] = useState([]);
    const [formModules, setFormModules] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingLesson, setEditingLesson] = useState(null);
    const [form, setForm] = useState({ name: "", courseId: "", moduleId: "", orderNumber: "" });
    const fileRef = useRef();

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const moduleId = q.get("moduleId") || "";

    const getLessonId = l => l.id ?? l.lessonId;
    const getCourseId = c => c.id ?? c.courseId;
    const getModuleId = m => m.id ?? m.moduleId;
    const getModuleCourseId = m => m.courseId ?? m.course?.id;

    const loadLessons = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (moduleId) params.moduleId = moduleId;

            const res = await authApis().get(endpoints.adminLessons, { params });
            const data = res.data;

            setLessons(data.lessons || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load lessons error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách bài học!");
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

    const loadModules = async (selectedCourseId, target = "filter") => {
        if (!selectedCourseId) {
            target === "filter" ? setFilterModules([]) : setFormModules([]);
            return;
        }

        try {
            const res = await authApis().get(endpoints.adminCourseModuleOptions, { params: { courseId: selectedCourseId } });
            const data = Array.isArray(res.data) ? res.data : [];
            target === "filter" ? setFilterModules(data) : setFormModules(data);
        } catch (ex) {
            target === "filter" ? setFilterModules([]) : setFormModules([]);
        }
    };

    useEffect(() => { loadCourses(); }, []);
    useEffect(() => { loadModules(draftFilters.courseId, "filter"); }, [draftFilters.courseId]);

    useEffect(() => {
        loadLessons();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.moduleId) params.moduleId = draftFilters.moduleId;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["moduleId"] : [];
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
        setEditingLesson(null);
        setForm({ name: "", courseId: "", moduleId: "", orderNumber: "" });
        setFormModules([]);
        setShowModal(true);
    };

    const openEditModal = lesson => {
        const selectedCourseId = lesson.courseId ?? lesson.courseModule?.course?.id ?? "";
        setEditingLesson(lesson);
        setForm({
            name: lesson.name || "",
            courseId: selectedCourseId,
            moduleId: lesson.moduleId ?? lesson.courseModule?.id ?? "",
            orderNumber: lesson.orderNumber ?? ""
        });
        loadModules(selectedCourseId, "form");
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingLesson(null);
    };

    const saveLesson = async e => {
        e.preventDefault();

        if (!form.name.trim()) return setErr("Tên bài học không được để trống!");
        if (!form.moduleId) return setErr("Vui lòng chọn module!");
        if (form.orderNumber === "" || Number(form.orderNumber) < 1)
            return setErr("Thứ tự bài học phải lớn hơn 0!");

        const file = fileRef.current?.files?.[0];

        if (!editingLesson && !file)
            return setErr("Vui lòng chọn file PDF!");

        if (file && file.type !== "application/pdf")
            return setErr("Chỉ chấp nhận file PDF!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const data = new FormData();
            data.append("name", form.name.trim());
            data.append("moduleId", form.moduleId);
            data.append("orderNumber", form.orderNumber);
            if (file) data.append("file", file);

            let res;

            if (editingLesson) {
                const id = getLessonId(editingLesson);
                res = await authApis().put(`${endpoints.adminLessons}/${id}`, data);
            } else {
                res = await authApis().post(endpoints.adminLessons, data);
            }

            setSuccess(
                res.data?.message ||
                (editingLesson ? "Cập nhật bài học thành công!" : "Thêm bài học thành công!")
            );

            closeModal();
            await loadLessons();
        } catch (ex) {
            console.error("Save lesson error:", ex);
            setErr(ex.response?.data?.message || "Lưu bài học thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const getModuleName = lesson => lesson.moduleName || lesson.courseModule?.name || "-";
    const getCourseName = lesson => lesson.courseName || lesson.courseModule?.course?.name || "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý bài học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> bài học
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm bài học</Button>
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
                                <Form.Control
                                    placeholder="Tìm theo tên bài học..."
                                    value={kw}
                                    onChange={e => setKw(e.target.value)}
                                />
                            </Col>

                            <Col lg={2}>
                                <Form.Select
                                    value={draftFilters.courseId}
                                    onChange={e => changeFilter("courseId", e.target.value)}
                                >
                                    <option value="">Tất cả khóa học</option>
                                    {courses.map(c => (
                                        <option key={getCourseId(c)} value={getCourseId(c)}>
                                            {c.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={3}>
                                <Form.Select
                                    value={draftFilters.moduleId}
                                    disabled={!draftFilters.courseId}
                                    onChange={e => changeFilter("moduleId", e.target.value)}
                                >
                                    <option value="">{courseId ? "Tất cả module" : "Chọn khóa học trước"}</option>
                                    {filterModules.map(m => (
                                        <option key={getModuleId(m)} value={getModuleId(m)}>
                                            {m.name}
                                        </option>
                                    ))}
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
                    ) : lessons.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy bài học.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Bài học</th>
                                        <th>Khóa học</th>
                                        <th>Module</th>
                                        <th>Thứ tự</th>
                                        <th>File PDF</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {lessons.map(l => (
                                        <tr key={getLessonId(l)}>
                                            <td>{getLessonId(l)}</td>
                                            <td className="fw-semibold">{l.name}</td>
                                            <td>{getCourseName(l)}</td>
                                            <td>{getModuleName(l)}</td>
                                            <td>{l.orderNumber}</td>
                                            <td>
                                                {l.fileUrl ? (
                                                    <a href={l.fileUrl} target="_blank" rel="noreferrer">
                                                        {l.fileName || "Xem PDF"}
                                                    </a>
                                                ) : "-"}
                                            </td>
                                            <td>
                                                <Button
                                                    size="sm"
                                                    variant="outline-primary"
                                                    onClick={() => openEditModal(l)}
                                                >
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
                        <Pagination.Prev
                            disabled={page === 1}
                            onClick={() => changePage(Math.max(page - 1, 1))}
                        />

                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                            <Pagination.Item
                                key={number}
                                active={number === page}
                                onClick={() => changePage(number)}
                            >
                                {number}
                            </Pagination.Item>
                        ))}

                        <Pagination.Next
                            disabled={page === totalPages}
                            onClick={() => changePage(Math.min(page + 1, totalPages))}
                        />
                        <Pagination.Last
                            disabled={page === totalPages}
                            onClick={() => changePage(totalPages)}
                        />
                    </Pagination>
                </div>
            )}

            <Modal show={showModal} onHide={closeModal} centered>
                <Form onSubmit={saveLesson}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingLesson ? "Cập nhật bài học" : "Thêm bài học"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên bài học</Form.Label>
                            <Form.Control
                                value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Khóa học</Form.Label>
                            <Form.Select value={form.courseId} onChange={e => {
                                const value = e.target.value;
                                setForm({ ...form, courseId: value, moduleId: "" });
                                loadModules(value, "form");
                            }} required>
                                <option value="">-- Chọn khóa học --</option>
                                {courses.map(c => (
                                    <option key={getCourseId(c)} value={getCourseId(c)}>{c.name}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Module</Form.Label>
                            <Form.Select value={form.moduleId} disabled={!form.courseId}
                                onChange={e => setForm({ ...form, moduleId: e.target.value })} required>
                                <option value="">{form.courseId ? "-- Chọn module --" : "-- Chọn khóa học trước --"}</option>
                                {formModules.map(m => (
                                    <option key={getModuleId(m)} value={getModuleId(m)}>{m.name}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Thứ tự bài học</Form.Label>
                            <Form.Control
                                type="number"
                                min="1"
                                value={form.orderNumber}
                                onChange={e => setForm({ ...form, orderNumber: e.target.value })}
                                required
                            />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>
                                File PDF {!editingLesson && <span className="text-danger">*</span>}
                            </Form.Label>

                            <Form.Control
                                ref={fileRef}
                                type="file"
                                accept="application/pdf,.pdf"
                                required={!editingLesson}
                            />

                            {editingLesson && (
                                <Form.Text className="text-muted">
                                    Không chọn file mới nếu muốn giữ PDF hiện tại.
                                </Form.Text>
                            )}
                        </Form.Group>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal}>Hủy</Button>
                        <Button type="submit" disabled={saving}>
                            {saving ? "Đang lưu..." : "Lưu"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default Lessons;
