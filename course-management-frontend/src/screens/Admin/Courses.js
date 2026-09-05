import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Courses = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["categoryId", "status"]);
    const [courses, setCourses] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingCourse, setEditingCourse] = useState(null);
    const [image, setImage] = useState(null);
    const [preview, setPreview] = useState("");
    const [form, setForm] = useState({
        name: "", description: "", tuitionFee: "", categoryId: ""
    });

    const page = Number(q.get("page")) || 1;
    const categoryId = q.get("categoryId") || "";
    const status = q.get("status") || "";

    const getCategoryId = c => c.id ?? c.categoryId;
    const getCourseId = c => c.id ?? c.courseId;

    const loadCourses = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (categoryId) params.categoryId = categoryId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminCourses, { params });
            const data = res.data;

            setCourses(data.courses || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load courses error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách khóa học!");
        } finally {
            setLoading(false);
        }
    };

    const loadCategories = async () => {
        try {
            const res = await authApis().get(endpoints.adminCategoryOptions);
            setCategories(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load category options error:", ex);
        }
    };

    useEffect(() => {
        loadCategories();
    }, []);

    useEffect(() => {
        loadCourses();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.categoryId) params.categoryId = draftFilters.categoryId;
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

    const clearPreview = () => {
        if (preview?.startsWith("blob:")) URL.revokeObjectURL(preview);
    };

    const openAddModal = () => {
        clearPreview();
        setEditingCourse(null);
        setImage(null);
        setPreview("");
        setForm({ name: "", description: "", tuitionFee: "", categoryId: "" });
        setShowModal(true);
    };

    const openEditModal = course => {
        clearPreview();
        setEditingCourse(course);
        setImage(null);
        setPreview(course.imageUrl || "");
        setForm({
            name: course.name || "",
            description: course.description || "",
            tuitionFee: course.tuitionFee ?? "",
            categoryId: course.categoryId ?? course.category?.id ?? ""
        });
        setShowModal(true);
    };

    const closeModal = () => {
        clearPreview();
        setShowModal(false);
        setEditingCourse(null);
        setImage(null);
        setPreview("");
    };

    const handleImageChange = e => {
        const file = e.target.files?.[0] || null;

        if (!file) {
            setImage(null);
            return;
        }

        if (file.size > 2 * 1024 * 1024) {
            e.target.value = "";
            setErr("Dung lượng ảnh không được vượt quá 2MB!");
            return;
        }

        clearPreview();
        setImage(file);
        setPreview(URL.createObjectURL(file));
    };

    const saveCourse = async e => {
        e.preventDefault();

        if (!form.name.trim()) return setErr("Tên khóa học không được để trống!");
        if (!form.categoryId) return setErr("Vui lòng chọn danh mục!");
        if (form.tuitionFee === "" || Number(form.tuitionFee) < 0)
            return setErr("Học phí không hợp lệ!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const data = new FormData();
            data.append("name", form.name.trim());
            data.append("description", form.description.trim());
            data.append("tuitionFee", form.tuitionFee);
            data.append("categoryId", form.categoryId);
            if (image) data.append("image", image);

            let res;

            if (editingCourse)
                res = await authApis().put(`${endpoints.adminCourses}/${getCourseId(editingCourse)}`, data);
            else
                res = await authApis().post(endpoints.adminCourses, data);

            setSuccess(
                res.data?.message ||
                (editingCourse ? "Cập nhật khóa học thành công!" : "Thêm khóa học thành công!")
            );

            closeModal();
            await loadCourses();
        } catch (ex) {
            console.error("Save course error:", ex);
            setErr(ex.response?.data?.message || "Lưu khóa học thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const updateStatus = async (courseId, newStatus) => {
        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminCourses}/${courseId}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadCourses();
        } catch (ex) {
            console.error("Update course status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadCourses();
        }
    };

    const categoryName = course =>
        course.categoryName ||
        course.category?.name ||
        categories.find(c =>
            String(getCategoryId(c)) === String(course.categoryId ?? course.category?.id)
        )?.name ||
        "-";

    const formatMoney = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;

    const statusBadge = value => {
        const config = {
            ACTIVE: ["success", "Hoạt động"],
            INACTIVE: ["secondary", "Không hoạt động"],
            HIDDEN: ["dark", "Đã ẩn"]
        }[value] || ["secondary", value || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý khóa học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> khóa học
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm khóa học</Button>
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
                                <Form.Control placeholder="Tìm theo tên khóa học..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.categoryId}
                                    onChange={e => changeFilter("categoryId", e.target.value)}>
                                    <option value="">Tất cả danh mục</option>
                                    {categories.map(c => (
                                        <option key={getCategoryId(c)} value={getCategoryId(c)}>
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
                                    <option value="HIDDEN">Đã ẩn</option>
                                </Form.Select>
                            </Col>

                            <Col lg={3} className="d-flex gap-2">
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
                    ) : courses.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy khóa học.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Ảnh</th>
                                        <th>Khóa học</th>
                                        <th>Danh mục</th>
                                        <th>Học phí</th>
                                        <th>Trạng thái</th>
                                        <th style={{ minWidth: 160 }}>Cập nhật</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {courses.map(course => {
                                        const id = getCourseId(course);

                                        return (
                                            <tr key={id}>
                                                <td>{id}</td>

                                                <td>
                                                    {course.imageUrl ? (
                                                        <img src={course.imageUrl} alt={course.name}
                                                            style={{
                                                                width: 90, height: 58,
                                                                objectFit: "cover", borderRadius: 6
                                                            }} />
                                                    ) : (
                                                        <div className="bg-light text-muted d-flex align-items-center justify-content-center"
                                                            style={{
                                                                width: 90, height: 58,
                                                                borderRadius: 6, fontSize: 12
                                                            }}>
                                                            Chưa có ảnh
                                                        </div>
                                                    )}
                                                </td>

                                                <td style={{ maxWidth: 300 }}>
                                                    <div className="fw-semibold">{course.name}</div>
                                                    <small className="text-muted">
                                                        {course.description || "Không có mô tả"}
                                                    </small>
                                                </td>

                                                <td>{categoryName(course)}</td>
                                                <td className="fw-semibold">{formatMoney(course.tuitionFee)}</td>
                                                <td>{statusBadge(course.status)}</td>

                                                <td>
                                                    <Form.Select size="sm" value={course.status}
                                                        onChange={e => updateStatus(id, e.target.value)}>
                                                        <option value="ACTIVE">Hoạt động</option>
                                                        <option value="INACTIVE">Không hoạt động</option>
                                                        <option value="HIDDEN">Ẩn khóa học</option>
                                                    </Form.Select>
                                                </td>

                                                <td>
                                                    <Button size="sm" variant="outline-primary"
                                                        onClick={() => openEditModal(course)}>
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
                <Form onSubmit={saveCourse}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingCourse ? "Cập nhật khóa học" : "Thêm khóa học"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Row>
                            <Col md={7}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Tên khóa học</Form.Label>
                                    <Form.Control value={form.name}
                                        onChange={e => setForm({ ...form, name: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={5}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Danh mục</Form.Label>
                                    <Form.Select value={form.categoryId}
                                        onChange={e => setForm({ ...form, categoryId: e.target.value })}
                                        required>
                                        <option value="">-- Chọn danh mục --</option>

                                        {categories.map(c => (
                                            <option key={getCategoryId(c)} value={getCategoryId(c)}>
                                                {c.name}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group className="mb-3">
                            <Form.Label>Học phí</Form.Label>
                            <Form.Control type="number" min="0" value={form.tuitionFee}
                                onChange={e => setForm({ ...form, tuitionFee: e.target.value })}
                                required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Mô tả</Form.Label>
                            <Form.Control as="textarea" rows={4} value={form.description}
                                onChange={e => setForm({ ...form, description: e.target.value })} />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Ảnh khóa học</Form.Label>
                            <Form.Control type="file" accept=".jpg,.jpeg,.png,.gif,image/*"
                                onChange={handleImageChange} />

                            <Form.Text className="text-muted">
                                JPG, JPEG, PNG hoặc GIF. Tối đa 2MB.
                                {editingCourse && " Không chọn ảnh mới nếu muốn giữ ảnh hiện tại."}
                            </Form.Text>

                            {preview && (
                                <div className="mt-3">
                                    <img src={preview} alt="Preview"
                                        style={{
                                            width: "100%", height: 260,
                                            objectFit: "cover", borderRadius: 8
                                        }} />
                                </div>
                            )}
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

export default Courses;
