import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import AsyncUserSelect from "../../components/AsyncUserSelect";

const Enrollments = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "classId", "status"]);

    const [enrollments, setEnrollments] = useState([]);
    const [courses, setCourses] = useState([]);
    const [filterClasses, setFilterClasses] = useState([]);
    const [modalClasses, setModalClasses] = useState([]);
    const [filterClassesLoading, setFilterClassesLoading] = useState(false);
    const [modalClassesLoading, setModalClassesLoading] = useState(false);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({ studentId: "", courseId: "", classId: "" });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const classId = q.get("classId") || "";
    const status = q.get("status") || "";

    const getEnrollmentId = e => e.id ?? e.enrollmentId;
    const getCourseId = c => c.id ?? c.courseId;
    const getClassId = c => c.id ?? c.classId;

    const loadEnrollments = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (classId) params.classId = classId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.managerEnrollments, { params });
            const data = res.data;

            setEnrollments(data.enrollments || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load enrollments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách đăng ký!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const courseRes = await authApis().get(endpoints.managerCourseOptions);
            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
        } catch (ex) {
            console.error("Load enrollment options error:", ex);
        }
    };

    const loadClassOptions = async (selectedCourseId, target = "filter", availableOnly = false) => {
        const setItems = target === "modal" ? setModalClasses : setFilterClasses;
        const setBusy = target === "modal" ? setModalClassesLoading : setFilterClassesLoading;

        if (!selectedCourseId) {
            setItems([]);
            return;
        }

        try {
            setBusy(true);
            const res = await authApis().get(endpoints.managerClassOptions, {
                params: { courseId: selectedCourseId, availableOnly }
            });
            setItems(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load class options error:", ex);
            setItems([]);
        } finally {
            setBusy(false);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadClassOptions(draftFilters.courseId, "filter", false);
    }, [courseId]);

    useEffect(() => {
        loadEnrollments();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.classId) params.classId = draftFilters.classId;
        if (draftFilters.status) params.status = draftFilters.status;

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
        setForm({ studentId: "", courseId: "", classId: "" });
        setModalClasses([]);
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setForm({ studentId: "", courseId: "", classId: "" });
        setModalClasses([]);
    };

    const changeModalCourse = async selectedCourseId => {
        setForm(current => ({ ...current, courseId: selectedCourseId, classId: "" }));
        await loadClassOptions(selectedCourseId, "modal", true);
    };

    const saveEnrollment = async e => {
        e.preventDefault();

        if (!form.studentId) return setErr("Vui lòng chọn học viên!");
        if (!form.classId) return setErr("Vui lòng chọn lớp học!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(endpoints.managerEnrollments, {
                studentId: Number(form.studentId),
                classId: Number(form.classId)
            });

            setSuccess(res.data?.message || "Đăng ký lớp học thành công!");
            closeModal();
            await loadEnrollments();
        } catch (ex) {
            console.error("Add enrollment error:", ex);
            setErr(ex.response?.data?.message || "Đăng ký lớp học thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const cancelEnrollment = async enrollmentId => {
        if (!window.confirm("Bạn có chắc muốn hủy đăng ký này?")) return;

        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.managerEnrollments}/${enrollmentId}/cancel`
            );

            setSuccess(res.data?.message || "Hủy đăng ký thành công!");
            await loadEnrollments();
        } catch (ex) {
            console.error("Cancel enrollment error:", ex);
            setErr(ex.response?.data?.message || "Hủy đăng ký thất bại!");
            await loadEnrollments();
        }
    };

    const studentName = e =>
        e.studentName ||
        e.studentFullName ||
        e.student?.fullName ||
        "-";

    const studentUsername = e =>
        e.username ||
        e.studentUsername ||
        e.student?.username ||
        "";

    const courseName = e =>
        e.courseName ||
        e.course?.name ||
        e.courseClass?.course?.name ||
        "-";

    const className = e =>
        e.className ||
        e.courseClassName ||
        e.courseClass?.name ||
        "-";

    const statusBadge = value => {
        const config = {
            PENDING_PAYMENT: ["warning", "Chờ thanh toán"],
            ACTIVE: ["success", "Đang hoạt động"],
            CANCELED: ["danger", "Đã hủy"]
        }[value] || ["secondary", value || "-"];

        return <Badge bg={config[0]} text={value === "PENDING_PAYMENT" ? "dark" : undefined}>
            {config[1]}
        </Badge>;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý đăng ký lớp học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> đăng ký · Kích hoạt học viên qua luồng thanh toán
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm đăng ký</Button>
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
                                <Form.Control placeholder="Tìm tên, username hoặc email..."
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
                                <Form.Select value={draftFilters.classId} disabled={!courseId || filterClassesLoading}
                                    onChange={e => changeFilter("classId", e.target.value)}>
                                    <option value="">{courseId ? (filterClassesLoading ? "Đang tải lớp..." : "Tất cả lớp") : "Chọn khóa học trước"}</option>

                                    {filterClasses.map(c => (
                                        <option key={getClassId(c)} value={getClassId(c)}>
                                            {c.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="PENDING_PAYMENT">Chờ thanh toán</option>
                                    <option value="ACTIVE">Đang hoạt động</option>
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
                    ) : enrollments.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy đăng ký.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Học viên</th>
                                        <th>Khóa học</th>
                                        <th>Lớp học</th>
                                        <th>Trạng thái</th>
                                        <th style={{ minWidth: 140 }}>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {enrollments.map(e => (
                                        <tr key={getEnrollmentId(e)}>
                                            <td>{getEnrollmentId(e)}</td>

                                            <td>
                                                <div className="fw-semibold">{studentName(e)}</div>

                                                <small className="text-muted">
                                                    {studentUsername(e) ? `@${studentUsername(e)} · ` : ""}ID ${e.studentId ?? "-"}
                                                </small>
                                            </td>

                                            <td>{courseName(e)}</td>
                                            <td>{className(e)}</td>
                                            <td>{statusBadge(e.status)}</td>

                                            <td>
                                                {e.status !== "CANCELED" ? (
                                                    <Button size="sm" variant="outline-danger"
                                                        onClick={() => cancelEnrollment(getEnrollmentId(e))}>
                                                        Hủy đăng ký
                                                    </Button>
                                                ) : (
                                                    <span className="text-muted">Đã hủy</span>
                                                )}
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
                <Form onSubmit={saveEnrollment}>
                    <Modal.Header closeButton>
                        <Modal.Title>Thêm đăng ký lớp học</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Khóa học</Form.Label>
                            <Form.Select value={form.courseId} onChange={e => changeModalCourse(e.target.value)} required>
                                <option value="">-- Chọn khóa học --</option>
                                {courses.map(c => (
                                    <option key={getCourseId(c)} value={getCourseId(c)}>{c.name}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Lớp học</Form.Label>
                            <Form.Select value={form.classId} disabled={!form.courseId || modalClassesLoading}
                                onChange={e => setForm({ ...form, classId: e.target.value })} required>
                                <option value="">{form.courseId ? (modalClassesLoading ? "Đang tải lớp..." : "-- Chọn lớp học --") : "-- Chọn khóa học trước --"}</option>
                                {modalClasses.map(c => (
                                    <option key={getClassId(c)} value={getClassId(c)}>{c.name}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Học viên</Form.Label>
                            <AsyncUserSelect endpoint={endpoints.managerStudentOptions} value={form.studentId}
                                onChange={student => setForm(current => ({ ...current, studentId: student?.id || "" }))}
                                placeholder="Gõ tên, username hoặc email học viên..." required />
                        </Form.Group>

                        <Form.Text className="text-muted">
                            Đăng ký mới sẽ có trạng thái Chờ thanh toán và tự tạo một giao dịch chờ xử lý.
                        </Form.Text>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving}>
                            {saving ? "Đang lưu..." : "Đăng ký"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default Enrollments;
