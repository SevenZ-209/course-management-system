import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import AsyncUserSelect from "../../components/AsyncUserSelect";

const StudentLearningPaths = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "learningPathId", "status"]);

    const [items, setItems] = useState([]);
    const [courses, setCourses] = useState([]);
    const [paths, setPaths] = useState([]);
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [availablePaths, setAvailablePaths] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({ studentId: "", learningPathId: "" });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const learningPathId = q.get("learningPathId") || "";
    const status = q.get("status") || "";

    const getId = x => x.id ?? x.studentLearningPathId;
    const getCourseId = x => x.id ?? x.courseId;
    const getPathId = x => x.id ?? x.learningPathId;

    const loadItems = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (learningPathId) params.learningPathId = learningPathId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminStudentLearningPaths, { params });
            const data = res.data;

            setItems(data.studentLearningPaths || data.items || data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load student learning paths error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải lộ trình học viên!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const courseRes = await authApis().get(endpoints.adminCourseOptions);
            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
        } catch (ex) {
            console.error("Load options error:", ex);
        }
    };

    const loadFilterPaths = async selectedCourseId => {
        setPaths([]);
        if (!selectedCourseId) return;

        try {
            const res = await authApis().get(endpoints.adminLearningPathOptions, {
                params: { courseId: selectedCourseId }
            });
            setPaths(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load learning path options error:", ex);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadFilterPaths(draftFilters.courseId);
    }, [draftFilters.courseId]);

    useEffect(() => {
        loadItems();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.learningPathId) params.learningPathId = draftFilters.learningPathId;
        if (draftFilters.status) params.status = draftFilters.status;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["learningPathId"] : [];
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

    const openAssignModal = () => {
        setForm({ studentId: "", learningPathId: "" });
        setSelectedStudent(null);
        setAvailablePaths([]);
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setForm({ studentId: "", learningPathId: "" });
        setSelectedStudent(null);
        setAvailablePaths([]);
    };

    const changeStudent = async student => {
        const studentId = student?.id || "";
        setSelectedStudent(student || null);
        setForm({ studentId, learningPathId: "" });
        setAvailablePaths([]);

        if (!studentId) return;

        try {
            const res = await authApis().get(endpoints.adminAvailableLearningPaths, {
                params: { studentId }
            });

            setAvailablePaths(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load available paths error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải lộ trình phù hợp!");
        }
    };

    const assignPath = async e => {
        e.preventDefault();

        if (!form.studentId) return setErr("Vui lòng chọn học viên!");
        if (!form.learningPathId) return setErr("Vui lòng chọn lộ trình!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(
                `${endpoints.adminStudentLearningPaths}/assign`,
                {
                    studentId: Number(form.studentId),
                    learningPathId: Number(form.learningPathId)
                }
            );

            setSuccess(res.data?.message || "Gán lộ trình thành công!");
            closeModal();
            await loadItems();
        } catch (ex) {
            console.error("Assign learning path error:", ex);
            setErr(ex.response?.data?.message || "Gán lộ trình thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const changeProgress = async (id, action) => {
        const label = action === "pause" ? "tạm dừng" : "tiếp tục";

        if (!window.confirm(`Bạn có chắc muốn ${label} lộ trình này?`)) return;

        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminStudentLearningPaths}/${id}/${action}`
            );

            setSuccess(res.data?.message || `Đã ${label} lộ trình!`);
            await loadItems();
        } catch (ex) {
            console.error("Change progress error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật lộ trình thất bại!");
        }
    };

    const studentName = x =>
        x.studentName ||
        x.studentFullName ||
        x.student?.fullName ||
        "-";

    const studentUsername = x =>
        x.studentUsername ||
        x.student?.username ||
        "";

    const pathName = x =>
        x.learningPathName ||
        x.learningPath?.name ||
        "-";

    const courseName = x =>
        x.courseName ||
        x.learningPath?.course?.name ||
        "-";

    const currentAssignment = x =>
        x.currentAssignmentName ||
        x.assignmentName ||
        x.currentDetail?.assignment?.name ||
        "-";

    const statusBadge = value => {
        const config = {
            IN_PROGRESS: ["success", "Đang học"],
            PAUSED: ["warning", "Tạm dừng"],
            COMPLETED: ["primary", "Hoàn thành"]
        }[value] || ["secondary", value || "-"];

        return (
            <Badge bg={config[0]} text={value === "PAUSED" ? "dark" : undefined}>
                {config[1]}
            </Badge>
        );
    };

    const formatDateTime = value =>
        value ? new Date(value).toLocaleString("vi-VN") : "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Lộ trình của học viên</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> lộ trình đã gán
                    </p>
                </div>

                <Button onClick={openAssignModal}>+ Gán lộ trình</Button>
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

                            <Col lg={3}>
                                <Form.Select value={draftFilters.learningPathId}
                                    disabled={!draftFilters.courseId}
                                    onChange={e => changeFilter("learningPathId", e.target.value)}>
                                    <option value="">{draftFilters.courseId ? "Tất cả lộ trình" : "Chọn khóa học trước"}</option>

                                    {paths.map(p => (
                                        <option key={getPathId(p)} value={getPathId(p)}>
                                            {p.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="IN_PROGRESS">Đang học</option>
                                    <option value="PAUSED">Tạm dừng</option>
                                    <option value="COMPLETED">Hoàn thành</option>
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
                    ) : items.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy lộ trình học viên.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Học viên</th>
                                        <th>Khóa học</th>
                                        <th>Lộ trình</th>
                                        <th>Bài hiện tại</th>
                                        <th>Trạng thái</th>
                                        <th>Bắt đầu</th>
                                        <th>Hoàn thành</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {items.map(x => (
                                        <tr key={getId(x)}>
                                            <td>{getId(x)}</td>

                                            <td>
                                                <div className="fw-semibold">{studentName(x)}</div>

                                                {studentUsername(x) && (
                                                    <small className="text-muted">
                                                        @{studentUsername(x)}
                                                    </small>
                                                )}
                                            </td>

                                            <td>{courseName(x)}</td>
                                            <td className="fw-semibold">{pathName(x)}</td>
                                            <td>{currentAssignment(x)}</td>
                                            <td>{statusBadge(x.status)}</td>
                                            <td>{formatDateTime(x.startedAt)}</td>
                                            <td>{formatDateTime(x.completedAt)}</td>

                                            <td>
                                                {x.status === "IN_PROGRESS" && (
                                                    <Button size="sm" variant="outline-warning"
                                                        onClick={() => changeProgress(getId(x), "pause")}>
                                                        Tạm dừng
                                                    </Button>
                                                )}

                                                {x.status === "PAUSED" && (
                                                    <Button size="sm" variant="outline-success"
                                                        onClick={() => changeProgress(getId(x), "resume")}>
                                                        Tiếp tục
                                                    </Button>
                                                )}

                                                {x.status === "COMPLETED" && (
                                                    <span className="text-muted">Đã hoàn thành</span>
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
                <Form onSubmit={assignPath}>
                    <Modal.Header closeButton>
                        <Modal.Title>Gán lộ trình cho học viên</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Học viên</Form.Label>

                            <AsyncUserSelect endpoint={endpoints.adminStudentOptions}
                                value={selectedStudent}
                                onChange={changeStudent}
                                placeholder="Tìm tên, username hoặc email..." required />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Lộ trình có thể gán</Form.Label>

                            <Form.Select value={form.learningPathId}
                                onChange={e => setForm(prev => ({
                                    ...prev,
                                    learningPathId: e.target.value
                                }))}
                                disabled={!form.studentId}
                                required>
                                <option value="">-- Chọn lộ trình --</option>

                                {availablePaths.map(p => (
                                    <option key={getPathId(p)} value={getPathId(p)}>
                                        {p.courseName || p.course?.name || "Khóa học"} - {p.name}
                                    </option>
                                ))}
                            </Form.Select>

                            {form.studentId && availablePaths.length === 0 && (
                                <Form.Text className="text-muted">
                                    Học viên không còn lộ trình phù hợp để gán.
                                </Form.Text>
                            )}
                        </Form.Group>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving || !availablePaths.length}>
                            {saving ? "Đang gán..." : "Gán lộ trình"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default StudentLearningPaths;
