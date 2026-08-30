import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const LearningPathDetails = () => {
    const [q, setQ] = useSearchParams();

    const [details, setDetails] = useState([]);
    const [courses, setCourses] = useState([]);
    const [paths, setPaths] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingDetail, setEditingDetail] = useState(null);
    const [form, setForm] = useState({
        learningPathId: "", assignmentId: "",
        orderNumber: "", minimumScore: "", maxAttempts: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const learningPathId = q.get("learningPathId") || "";

    const getDetailId = d => d.id ?? d.detailId;
    const getCourseId = c => c.id ?? c.courseId;
    const getPathId = p => p.id ?? p.learningPathId;
    const getPathCourseId = p => p.courseId ?? p.course?.id;
    const getAssignmentId = a => a.id ?? a.assignmentId;

    const loadDetails = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (learningPathId) params.learningPathId = learningPathId;

            const res = await authApis().get(endpoints.adminLearningPathDetails, { params });
            const data = res.data;

            setDetails(data.details || data.learningPathDetails || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load details error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải chi tiết lộ trình!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const [courseRes, pathRes] = await Promise.all([
                authApis().get(endpoints.adminCourseOptions),
                authApis().get(endpoints.adminLearningPathOptions)
            ]);

            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
            setPaths(Array.isArray(pathRes.data) ? pathRes.data : []);
        } catch (ex) {
            console.error("Load options error:", ex);
        }
    };

    const loadAssignments = async pathId => {
        if (!pathId) {
            setAssignments([]);
            return;
        }

        try {
            const res = await authApis().get(
                endpoints.adminLearningPathDetailAssignments,
                { params: { learningPathId: pathId } }
            );

            setAssignments(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load assignments error:", ex);
            setAssignments([]);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadDetails();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (courseId) params.courseId = courseId;
        if (learningPathId) params.learningPathId = learningPathId;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const params = Object.fromEntries(q);

        if (value) params[name] = value;
        else delete params[name];

        if (name === "courseId") delete params.learningPathId;

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
            learningPathId: "", assignmentId: "",
            orderNumber: "", minimumScore: "", maxAttempts: ""
        });
    };

    const openAddModal = () => {
        setEditingDetail(null);
        setAssignments([]);
        resetForm();
        setShowModal(true);
    };

    const openEditModal = async detail => {
        const pathId = detail.learningPathId ?? detail.learningPath?.id ?? "";

        setEditingDetail(detail);
        setForm({
            learningPathId: pathId,
            assignmentId: detail.assignmentId ?? detail.assignment?.id ?? "",
            orderNumber: detail.orderNumber ?? "",
            minimumScore: detail.minimumScore ?? "",
            maxAttempts: detail.maxAttempts ?? ""
        });

        await loadAssignments(pathId);
        setShowModal(true);
    };

    const changePath = async value => {
        setForm(prev => ({
            ...prev,
            learningPathId: value,
            assignmentId: ""
        }));

        await loadAssignments(value);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingDetail(null);
        setAssignments([]);
        resetForm();
    };

    const saveDetail = async e => {
        e.preventDefault();

        if (!editingDetail && !form.learningPathId)
            return setErr("Vui lòng chọn lộ trình!");

        if (!form.assignmentId)
            return setErr("Vui lòng chọn bài tập!");

        if (!form.orderNumber || Number(form.orderNumber) < 1)
            return setErr("Thứ tự phải lớn hơn 0!");

        if (form.minimumScore === "" || Number(form.minimumScore) < 0)
            return setErr("Điểm tối thiểu không hợp lệ!");

        if (!form.maxAttempts || Number(form.maxAttempts) < 1)
            return setErr("Số lần làm tối đa phải lớn hơn 0!");

        const assignment = assignments.find(a =>
            String(getAssignmentId(a)) === String(form.assignmentId)
        );

        if (
            assignment?.maximumScore != null &&
            Number(form.minimumScore) > Number(assignment.maximumScore)
        ) {
            return setErr(
                `Điểm tối thiểu không được vượt quá ${assignment.maximumScore} điểm!`
            );
        }

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                assignmentId: Number(form.assignmentId),
                orderNumber: Number(form.orderNumber),
                minimumScore: Number(form.minimumScore),
                maxAttempts: Number(form.maxAttempts)
            };

            let res;

            if (editingDetail) {
                res = await authApis().put(
                    `${endpoints.adminLearningPathDetails}/${getDetailId(editingDetail)}`,
                    body
                );
            } else {
                res = await authApis().post(endpoints.adminLearningPathDetails, {
                    learningPathId: Number(form.learningPathId),
                    ...body
                });
            }

            setSuccess(
                res.data?.message ||
                (editingDetail
                    ? "Cập nhật chi tiết lộ trình thành công!"
                    : "Thêm bài vào lộ trình thành công!")
            );

            closeModal();
            await loadDetails();
        } catch (ex) {
            console.error("Save detail error:", ex);
            setErr(ex.response?.data?.message || "Lưu chi tiết lộ trình thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const courseName = d =>
        d.courseName ||
        d.learningPath?.course?.name ||
        "-";

    const pathName = d =>
        d.learningPathName ||
        d.learningPath?.name ||
        paths.find(p =>
            String(getPathId(p)) === String(d.learningPathId ?? d.learningPath?.id)
        )?.name ||
        "-";

    const assignmentName = d =>
        d.assignmentName ||
        d.assignment?.name ||
        "-";

    const filteredPaths = courseId
        ? paths.filter(p => String(getPathCourseId(p)) === String(courseId))
        : paths;

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Chi tiết lộ trình học</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> bài trong lộ trình
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm bài vào lộ trình</Button>
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
                                <Form.Control placeholder="Tìm bài tập..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
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

                            <Col lg={3}>
                                <Form.Select value={learningPathId}
                                    onChange={e => changeFilter("learningPathId", e.target.value)}>
                                    <option value="">Tất cả lộ trình</option>

                                    {filteredPaths.map(p => (
                                        <option key={getPathId(p)} value={getPathId(p)}>
                                            {p.name}
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
                    ) : details.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy chi tiết lộ trình.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Khóa học</th>
                                        <th>Lộ trình</th>
                                        <th>Bài tập</th>
                                        <th>Thứ tự</th>
                                        <th>Điểm đạt</th>
                                        <th>Số lần làm</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {details.map(d => (
                                        <tr key={getDetailId(d)}>
                                            <td>{getDetailId(d)}</td>
                                            <td>{courseName(d)}</td>
                                            <td className="fw-semibold">{pathName(d)}</td>
                                            <td>{assignmentName(d)}</td>
                                            <td>{d.orderNumber}</td>
                                            <td>{d.minimumScore}</td>
                                            <td>{d.maxAttempts}</td>

                                            <td>
                                                <Button size="sm" variant="outline-primary"
                                                    onClick={() => openEditModal(d)}>
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
                <Form onSubmit={saveDetail}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingDetail
                                ? "Cập nhật chi tiết lộ trình"
                                : "Thêm bài vào lộ trình"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Lộ trình</Form.Label>

                            <Form.Select value={form.learningPathId}
                                onChange={e => changePath(e.target.value)}
                                disabled={!!editingDetail}
                                required>
                                <option value="">-- Chọn lộ trình --</option>

                                {paths.map(p => (
                                    <option key={getPathId(p)} value={getPathId(p)}>
                                        {p.name}
                                    </option>
                                ))}
                            </Form.Select>

                            {editingDetail && (
                                <Form.Text className="text-muted">
                                    Không thể thay đổi lộ trình khi chỉnh sửa.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Bài tập</Form.Label>

                            <Form.Select value={form.assignmentId}
                                onChange={e => setForm({ ...form, assignmentId: e.target.value })}
                                disabled={!form.learningPathId}
                                required>
                                <option value="">-- Chọn bài tập --</option>

                                {assignments.map(a => (
                                    <option key={getAssignmentId(a)} value={getAssignmentId(a)}>
                                        {a.name} - Tối đa {a.maximumScore} điểm
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Row>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Thứ tự</Form.Label>
                                    <Form.Control type="number" min="1"
                                        value={form.orderNumber}
                                        onChange={e => setForm({ ...form, orderNumber: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Điểm tối thiểu</Form.Label>
                                    <Form.Control type="number" min="0" step="0.01"
                                        value={form.minimumScore}
                                        onChange={e => setForm({ ...form, minimumScore: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>Số lần làm tối đa</Form.Label>
                                    <Form.Control type="number" min="1"
                                        value={form.maxAttempts}
                                        onChange={e => setForm({ ...form, maxAttempts: e.target.value })}
                                        required />
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

export default LearningPathDetails;