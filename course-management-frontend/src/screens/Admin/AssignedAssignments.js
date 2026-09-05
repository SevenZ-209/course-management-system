import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import AsyncUserSelect from "../../components/AsyncUserSelect";

const AssignedAssignments = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "learningPathId", "status", "date"]);

    const [items, setItems] = useState([]);
    const [courses, setCourses] = useState([]);
    const [paths, setPaths] = useState([]);
    const [manualStudent, setManualStudent] = useState(null);
    const [releaseStudent, setReleaseStudent] = useState(null);
    const [progressOptions, setProgressOptions] = useState([]);
    const [availableAssignments, setAvailableAssignments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showManual, setShowManual] = useState(false);
    const [showRelease, setShowRelease] = useState(false);
    const [manualForm, setManualForm] = useState({
        studentId: "", assignmentId: "", availableAt: "", dueAt: ""
    });
    const [releaseForm, setReleaseForm] = useState({
        studentLearningPathId: "", availableAt: "", dueAt: ""
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const learningPathId = q.get("learningPathId") || "";
    const status = q.get("status") || "";
    const date = q.get("date") || "";

    const getId = x => x.id ?? x.assignedAssignmentId;
    const getCourseId = x => x.id ?? x.courseId;
    const getPathId = x => x.id ?? x.learningPathId;
    const getProgressId = x => x.id ?? x.studentLearningPathId;
    const getAssignmentId = x => x.id ?? x.assignmentId;

    const loadItems = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (learningPathId) params.learningPathId = learningPathId;
            if (status) params.status = status;
            if (date) params.date = date;

            const res = await authApis().get(endpoints.adminAssignedAssignments, { params });
            const data = res.data;

            setItems(data.assignedAssignments || data.items || data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load assigned assignments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách bài đã giao!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const courseRes = await authApis().get(endpoints.adminCourseOptions);
            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
        } catch (ex) {
            console.error("Load assigned assignment options error:", ex);
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
        if (draftFilters.date) params.date = draftFilters.date;

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

    const resetManualForm = () => {
        setManualForm({
            studentId: "", assignmentId: "",
            availableAt: "", dueAt: ""
        });
    };

    const resetReleaseForm = () => {
        setReleaseForm({
            studentLearningPathId: "",
            availableAt: "", dueAt: ""
        });
    };

    const openManualModal = () => {
        setAvailableAssignments([]);
        setManualStudent(null);
        resetManualForm();
        setShowManual(true);
    };

    const closeManualModal = () => {
        setShowManual(false);
        setAvailableAssignments([]);
        setManualStudent(null);
        resetManualForm();
    };

    const openReleaseModal = () => {
        resetReleaseForm();
        setReleaseStudent(null);
        setProgressOptions([]);
        setShowRelease(true);
    };

    const closeReleaseModal = () => {
        setShowRelease(false);
        setReleaseStudent(null);
        setProgressOptions([]);
        resetReleaseForm();
    };

    const changeStudent = async student => {
        const studentId = student?.id || "";
        setManualStudent(student || null);
        setManualForm(prev => ({
            ...prev,
            studentId,
            assignmentId: ""
        }));

        setAvailableAssignments([]);

        if (!studentId) return;

        try {
            const res = await authApis().get(endpoints.adminAvailableAssignments, {
                params: { studentId }
            });

            setAvailableAssignments(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load available assignments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải bài tập phù hợp!");
        }
    };

    const changeReleaseStudent = async student => {
        const studentId = student?.id || "";
        setReleaseStudent(student || null);
        setProgressOptions([]);
        setReleaseForm(prev => ({ ...prev, studentLearningPathId: "" }));

        if (!studentId) return;

        try {
            const res = await authApis().get(endpoints.adminInProgressStudentLearningPaths, {
                params: { studentId }
            });
            setProgressOptions(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load progress options error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải tiến độ học!");
        }
    };

    const validateTimes = (availableAt, dueAt) => {
        if (!availableAt || !dueAt) {
            setErr("Vui lòng nhập thời gian mở bài và hạn nộp!");
            return false;
        }

        if (new Date(dueAt) <= new Date(availableAt)) {
            setErr("Hạn nộp phải sau thời gian mở bài!");
            return false;
        }

        return true;
    };

    const assignManual = async e => {
        e.preventDefault();

        if (!manualForm.studentId) return setErr("Vui lòng chọn học viên!");
        if (!manualForm.assignmentId) return setErr("Vui lòng chọn bài tập!");
        if (!validateTimes(manualForm.availableAt, manualForm.dueAt)) return;

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(
                `${endpoints.adminAssignedAssignments}/manual`,
                {
                    studentId: Number(manualForm.studentId),
                    assignmentId: Number(manualForm.assignmentId),
                    availableAt: manualForm.availableAt,
                    dueAt: manualForm.dueAt
                }
            );

            setSuccess(res.data?.message || "Giao bài thủ công thành công!");
            closeManualModal();
            await loadItems();
        } catch (ex) {
            console.error("Manual assignment error:", ex);
            setErr(ex.response?.data?.message || "Giao bài thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const releaseCurrent = async e => {
        e.preventDefault();

        if (!releaseForm.studentLearningPathId)
            return setErr("Vui lòng chọn tiến độ học!");

        if (!validateTimes(releaseForm.availableAt, releaseForm.dueAt)) return;

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(
                `${endpoints.adminAssignedAssignments}/release-current`,
                {
                    studentLearningPathId: Number(releaseForm.studentLearningPathId),
                    availableAt: releaseForm.availableAt,
                    dueAt: releaseForm.dueAt
                }
            );

            setSuccess(res.data?.message || "Phát bài theo lộ trình thành công!");
            closeReleaseModal();
            await loadItems();
        } catch (ex) {
            console.error("Release current error:", ex);
            setErr(ex.response?.data?.message || "Phát bài thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const updateStatus = async (id, newStatus) => {
        if (!newStatus) return;

        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminAssignedAssignments}/${id}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadItems();
        } catch (ex) {
            console.error("Update status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadItems();
        }
    };

    const studentName = x =>
        x.studentName ||
        x.studentFullName ||
        x.student?.fullName ||
        "-";

    const assignmentName = x =>
        x.assignmentName ||
        x.assignment?.name ||
        "-";

    const courseName = x =>
        x.courseName ||
        x.assignment?.course?.name ||
        "-";

    const pathName = x =>
        x.learningPathName ||
        x.learningPathDetail?.learningPath?.name ||
        null;

    const assignedByName = x =>
        x.assignedByName ||
        x.assignedBy?.fullName ||
        "-";

    const statusBadge = value => {
        const config = {
            LOCKED: ["secondary", "Đã khóa"],
            AVAILABLE: ["success", "Có thể làm"],
            COMPLETED: ["primary", "Hoàn thành"]
        }[value] || ["secondary", value || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    const formatDateTime = value =>
        value ? new Date(value).toLocaleString("vi-VN") : "-";

    const progressLabel = x => {
        const student =
            x.studentName ||
            x.studentFullName ||
            x.student?.fullName ||
            "Học viên";

        const path =
            x.learningPathName ||
            x.learningPath?.name ||
            "Lộ trình";

        const current =
            x.currentAssignmentName ||
            x.currentDetail?.assignment?.name ||
            "";

        return `${student} - ${path}${current ? ` - ${current}` : ""}`;
    };

    const assignmentLabel = x => {
        const course = x.courseName || x.course?.name || "";
        const assignment = x.name || x.assignmentName || "Bài tập";
        return `${course}${course ? " - " : ""}${assignment}`;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý bài tập đã giao</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> bài đã giao
                    </p>
                </div>

                <div className="d-flex gap-2">
                    <Button variant="outline-primary" onClick={openReleaseModal}>
                        Phát bài theo lộ trình
                    </Button>

                    <Button onClick={openManualModal}>
                        + Giao bài thủ công
                    </Button>
                </div>
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
                                <Form.Control placeholder="Tìm học viên, bài tập..."
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
                                    <option value="LOCKED">Đã khóa</option>
                                    <option value="AVAILABLE">Có thể làm</option>
                                    <option value="COMPLETED">Hoàn thành</option>
                                </Form.Select>
                            </Col>

                            <Col lg={1}>
                                <Form.Control type="date" value={draftFilters.date}
                                    onChange={e => changeFilter("date", e.target.value)} />
                            </Col>

                            <Col lg={2} className="d-flex gap-2">
                                <Button type="submit">Tìm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>
                                    Xóa
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
                            Không tìm thấy bài tập đã giao.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Học viên</th>
                                        <th>Khóa học</th>
                                        <th>Bài tập</th>
                                        <th>Nguồn</th>
                                        <th>Mở bài</th>
                                        <th>Hạn nộp</th>
                                        <th>Trạng thái</th>
                                        <th>Người giao</th>
                                        <th>Cập nhật</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {items.map(x => (
                                        <tr key={getId(x)}>
                                            <td>{getId(x)}</td>
                                            <td className="fw-semibold">{studentName(x)}</td>
                                            <td>{courseName(x)}</td>
                                            <td>{assignmentName(x)}</td>

                                            <td>
                                                {pathName(x)
                                                    ? <Badge bg="info">{pathName(x)}</Badge>
                                                    : <Badge bg="dark">Thủ công</Badge>}
                                            </td>

                                            <td>{formatDateTime(x.availableAt)}</td>
                                            <td>{formatDateTime(x.dueAt)}</td>
                                            <td>{statusBadge(x.status)}</td>
                                            <td>{assignedByName(x)}</td>

                                            <td style={{ minWidth: "145px" }}>
                                                {x.status === "COMPLETED" ? (
                                                    <span className="text-muted">Đã hoàn thành</span>
                                                ) : (
                                                    <Form.Select size="sm" value={x.status}
                                                        onChange={e => updateStatus(getId(x), e.target.value)}>
                                                        <option value="LOCKED">Đã khóa</option>
                                                        <option value="AVAILABLE">Có thể làm</option>
                                                    </Form.Select>
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

            <Modal show={showManual} onHide={closeManualModal} centered size="lg">
                <Form onSubmit={assignManual}>
                    <Modal.Header closeButton>
                        <Modal.Title>Giao bài thủ công</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Học viên</Form.Label>

                            <AsyncUserSelect endpoint={endpoints.adminStudentOptions}
                                value={manualStudent}
                                onChange={changeStudent}
                                placeholder="Tìm tên, username hoặc email..." required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Bài tập có thể giao</Form.Label>

                            <Form.Select value={manualForm.assignmentId}
                                onChange={e => setManualForm(prev => ({
                                    ...prev,
                                    assignmentId: e.target.value
                                }))}
                                disabled={!manualForm.studentId}
                                required>
                                <option value="">-- Chọn bài tập --</option>

                                {availableAssignments.map(a => (
                                    <option key={getAssignmentId(a)} value={getAssignmentId(a)}>
                                        {assignmentLabel(a)}
                                    </option>
                                ))}
                            </Form.Select>

                            {manualForm.studentId && availableAssignments.length === 0 && (
                                <Form.Text className="text-muted">
                                    Học viên không có bài tập phù hợp để giao.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Thời gian mở bài</Form.Label>

                                    <Form.Control type="datetime-local"
                                        value={manualForm.availableAt}
                                        onChange={e => setManualForm(prev => ({
                                            ...prev,
                                            availableAt: e.target.value
                                        }))}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Hạn nộp</Form.Label>

                                    <Form.Control type="datetime-local"
                                        min={manualForm.availableAt || undefined}
                                        value={manualForm.dueAt}
                                        onChange={e => setManualForm(prev => ({
                                            ...prev,
                                            dueAt: e.target.value
                                        }))}
                                        required />
                                </Form.Group>
                            </Col>
                        </Row>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeManualModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit"
                            disabled={saving || !manualForm.studentId || !availableAssignments.length}>
                            {saving ? "Đang giao..." : "Giao bài"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showRelease} onHide={closeReleaseModal} centered size="lg">
                <Form onSubmit={releaseCurrent}>
                    <Modal.Header closeButton>
                        <Modal.Title>Phát bài hiện tại theo lộ trình</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Học viên</Form.Label>
                            <AsyncUserSelect endpoint={endpoints.adminStudentOptions}
                                value={releaseStudent}
                                onChange={changeReleaseStudent}
                                placeholder="Tìm tên, username hoặc email..." required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Tiến độ học</Form.Label>

                            <Form.Select value={releaseForm.studentLearningPathId}
                                disabled={!releaseStudent}
                                onChange={e => setReleaseForm(prev => ({
                                    ...prev,
                                    studentLearningPathId: e.target.value
                                }))}
                                required>
                                <option value="">-- Chọn tiến độ học --</option>

                                {progressOptions.map(x => (
                                    <option key={getProgressId(x)} value={getProgressId(x)}>
                                        {progressLabel(x)}
                                    </option>
                                ))}
                            </Form.Select>

                            {releaseStudent && progressOptions.length === 0 && (
                                <Form.Text className="text-muted">
                                    Học viên không có lộ trình đang IN_PROGRESS.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Thời gian mở bài</Form.Label>

                                    <Form.Control type="datetime-local"
                                        value={releaseForm.availableAt}
                                        onChange={e => setReleaseForm(prev => ({
                                            ...prev,
                                            availableAt: e.target.value
                                        }))}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Hạn nộp</Form.Label>

                                    <Form.Control type="datetime-local"
                                        min={releaseForm.availableAt || undefined}
                                        value={releaseForm.dueAt}
                                        onChange={e => setReleaseForm(prev => ({
                                            ...prev,
                                            dueAt: e.target.value
                                        }))}
                                        required />
                                </Form.Group>
                            </Col>
                        </Row>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeReleaseModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving || !progressOptions.length}>
                            {saving ? "Đang phát..." : "Phát bài"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default AssignedAssignments;
