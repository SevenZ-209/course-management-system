import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Answers = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "assignmentId", "questionId", "type", "correct"]);
    const [answers, setAnswers] = useState([]);
    const [courses, setCourses] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [questions, setQuestions] = useState([]);
    const [modalAssignments, setModalAssignments] = useState([]);
    const [modalQuestions, setModalQuestions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");
    const [showModal, setShowModal] = useState(false);
    const [editingAnswer, setEditingAnswer] = useState(null);
    const [form, setForm] = useState({
        courseId: "", assignmentId: "", questionId: "",
        content: "", orderNumber: "", correct: "false"
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const assignmentId = q.get("assignmentId") || "";
    const questionId = q.get("questionId") || "";
    const type = q.get("type") || "";
    const correct = q.get("correct") || "";

    const getAnswerId = x => x.id ?? x.answerId;
    const getCourseId = x => x.id ?? x.courseId;
    const getAssignmentId = x => x.id ?? x.assignmentId;
    const getQuestionId = x => x.id ?? x.questionId;

    const loadAnswers = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (assignmentId) params.assignmentId = assignmentId;
            if (questionId) params.questionId = questionId;
            if (type) params.type = type;
            if (correct !== "") params.correct = correct;

            const res = await authApis().get(endpoints.adminAnswers, { params });
            const data = res.data;

            setAnswers(data.answers || data.items || data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load answers error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách đáp án!");
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

    const loadAssignments = async (selectedCourseId, modal = false) => {
        if (!selectedCourseId) {
            modal ? setModalAssignments([]) : setAssignments([]);
            return;
        }

        try {
            const res = await authApis().get(endpoints.adminAssignmentOptions, {
                params: { courseId: selectedCourseId }
            });

            const data = Array.isArray(res.data) ? res.data : [];
            modal ? setModalAssignments(data) : setAssignments(data);
        } catch (ex) {
            console.error("Load assignments error:", ex);
            modal ? setModalAssignments([]) : setAssignments([]);
        }
    };

    const loadQuestions = async (selectedAssignmentId, modal = false) => {
        if (!selectedAssignmentId) {
            modal ? setModalQuestions([]) : setQuestions([]);
            return;
        }

        try {
            const res = await authApis().get(endpoints.adminQuestionOptions, {
                params: { assignmentId: selectedAssignmentId }
            });

            const data = Array.isArray(res.data) ? res.data : [];
            modal ? setModalQuestions(data) : setQuestions(data);
        } catch (ex) {
            console.error("Load questions error:", ex);
            modal ? setModalQuestions([]) : setQuestions([]);
        }
    };

    useEffect(() => { loadCourses(); }, []);
    useEffect(() => { loadAnswers(); }, [q]);
    useEffect(() => { loadAssignments(draftFilters.courseId); }, [draftFilters.courseId]);
    useEffect(() => { loadQuestions(draftFilters.assignmentId); }, [draftFilters.assignmentId]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.assignmentId) params.assignmentId = draftFilters.assignmentId;
        if (draftFilters.questionId) params.questionId = draftFilters.questionId;
        if (draftFilters.type) params.type = draftFilters.type;
        if (draftFilters.correct !== "") params.correct = draftFilters.correct;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["assignmentId", "questionId"] : name === "assignmentId" ? ["questionId"] : [];
        setDraftFilter(name, value, resetKeys);
    };

    const clearFilters = () => {
        setKw("");
        resetDraftFilters();
        setAssignments([]);
        setQuestions([]);
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const resetForm = () => {
        setForm({
            courseId: "", assignmentId: "", questionId: "",
            content: "", orderNumber: "", correct: "false"
        });
    };

    const openAddModal = () => {
        setEditingAnswer(null);
        setModalAssignments([]);
        setModalQuestions([]);
        resetForm();
        setShowModal(true);
    };

    const openEditModal = async answer => {
        const cId = answer.courseId ?? answer.question?.assignment?.course?.id ?? "";
        const aId = answer.assignmentId ?? answer.question?.assignment?.id ?? "";
        const qId = answer.questionId ?? answer.question?.id ?? "";

        setEditingAnswer(answer);
        setForm({
            courseId: cId,
            assignmentId: aId,
            questionId: qId,
            content: answer.content || "",
            orderNumber: answer.orderNumber ?? "",
            correct: answer.correct === true ? "true" : "false"
        });

        if (cId) await loadAssignments(cId, true);
        if (aId) await loadQuestions(aId, true);

        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingAnswer(null);
        setModalAssignments([]);
        setModalQuestions([]);
        resetForm();
    };

    const changeModalCourse = async value => {
        setForm(prev => ({
            ...prev,
            courseId: value,
            assignmentId: "",
            questionId: ""
        }));

        setModalQuestions([]);
        await loadAssignments(value, true);
    };

    const changeModalAssignment = async value => {
        setForm(prev => ({
            ...prev,
            assignmentId: value,
            questionId: ""
        }));

        await loadQuestions(value, true);
    };

    const questionType = x => x?.questionType || x?.question?.type || "";

    const selectedQuestion = modalQuestions.find(x =>
        String(getQuestionId(x)) === String(form.questionId)
    );

    const selectedQuestionType =
        selectedQuestion?.type ||
        questionType(editingAnswer);

    const saveAnswer = async e => {
        e.preventDefault();

        if (!editingAnswer && !form.courseId)
            return setErr("Vui lòng chọn khóa học!");

        if (!editingAnswer && !form.assignmentId)
            return setErr("Vui lòng chọn bài tập!");

        if (!editingAnswer && !form.questionId)
            return setErr("Vui lòng chọn câu hỏi!");

        if (!form.content.trim())
            return setErr("Nội dung đáp án không được để trống!");

        if (!form.orderNumber || Number(form.orderNumber) < 1)
            return setErr("Thứ tự đáp án phải lớn hơn 0!");

        const currentQuestion = modalQuestions.find(x =>
            String(getQuestionId(x)) === String(form.questionId)
        );

        const currentType =
            currentQuestion?.type ||
            questionType(editingAnswer);

        const answerCorrect =
            currentType === "SHORT_ANSWER"
                ? true
                : currentType === "ESSAY"
                    ? false
                    : form.correct === "true";

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                content: form.content.trim(),
                orderNumber: Number(form.orderNumber),
                correct: answerCorrect
            };

            let res;

            if (editingAnswer) {
                res = await authApis().put(
                    `${endpoints.adminAnswers}/${getAnswerId(editingAnswer)}`,
                    body
                );
            } else {
                res = await authApis().post(endpoints.adminAnswers, {
                    questionId: Number(form.questionId),
                    ...body
                });
            }

            setSuccess(res.data?.message ||
                (editingAnswer ? "Cập nhật đáp án thành công!" : "Thêm đáp án thành công!"));

            closeModal();
            await loadAnswers();
        } catch (ex) {
            console.error("Save answer error:", ex);
            setErr(ex.response?.data?.message || "Lưu đáp án thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const courseName = x => x.courseName || x.question?.assignment?.course?.name || "-";
    const assignmentName = x => x.assignmentName || x.question?.assignment?.name || "-";
    const questionContent = x => x.questionContent || x.question?.content || "-";

    const typeBadge = value => {
        if (value === "MULTIPLE_CHOICE")
            return <Badge bg="primary">Trắc nghiệm</Badge>;

        if (value === "SHORT_ANSWER")
            return <Badge bg="info">Trả lời ngắn</Badge>;

        return <Badge bg="warning" text="dark">Tự luận</Badge>;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý đáp án</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> đáp án
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm đáp án</Button>
            </div>

            {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}
            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={3}>
                                <Form.Control placeholder="Tìm nội dung đáp án..."
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
                                <Form.Select value={draftFilters.assignmentId} disabled={!draftFilters.courseId}
                                    onChange={e => changeFilter("assignmentId", e.target.value)}>
                                    <option value="">Tất cả bài tập</option>
                                    {assignments.map(a => (
                                        <option key={getAssignmentId(a)} value={getAssignmentId(a)}>
                                            {a.name}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.questionId} disabled={!draftFilters.assignmentId}
                                    onChange={e => changeFilter("questionId", e.target.value)}>
                                    <option value="">Tất cả câu hỏi</option>
                                    {questions.map(x => (
                                        <option key={getQuestionId(x)} value={getQuestionId(x)}>
                                            Câu {x.orderNumber}: {x.content}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col lg={1}>
                                <Form.Select value={draftFilters.type}
                                    onChange={e => changeFilter("type", e.target.value)}>
                                    <option value="">Loại</option>
                                    <option value="MULTIPLE_CHOICE">TN</option>
                                    <option value="SHORT_ANSWER">Ngắn</option>
                                    <option value="ESSAY">TL</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.correct}
                                    onChange={e => changeFilter("correct", e.target.value)}>
                                    <option value="">Tất cả đáp án</option>
                                    <option value="true">Đúng</option>
                                    <option value="false">Sai</option>
                                </Form.Select>
                            </Col>

                            <Col xs={12} className="d-flex gap-2 justify-content-end">
                                <Button type="submit">Tìm kiếm</Button>
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
                    ) : answers.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy đáp án.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>Khóa học</th>
                                        <th>Bài tập</th>
                                        <th>Câu hỏi</th>
                                        <th>Loại</th>
                                        <th>Thứ tự</th>
                                        <th>Đáp án</th>
                                        <th>Kết quả</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {answers.map(answer => (
                                        <tr key={getAnswerId(answer)}>
                                            <td>{courseName(answer)}</td>
                                            <td>{assignmentName(answer)}</td>
                                            <td style={{ maxWidth: "250px" }}>
                                                {questionContent(answer)}
                                            </td>
                                            <td>{typeBadge(questionType(answer))}</td>
                                            <td>{answer.orderNumber}</td>
                                            <td style={{ maxWidth: "300px" }}>{answer.content}</td>
                                            <td>
                                                {questionType(answer) === "ESSAY"
                                                    ? <Badge bg="secondary">Tham khảo</Badge>
                                                    : answer.correct
                                                        ? <Badge bg="success">Đúng</Badge>
                                                        : <Badge bg="secondary">Sai</Badge>}
                                            </td>
                                            <td>
                                                <Button size="sm" variant="outline-primary"
                                                    onClick={() => openEditModal(answer)}>
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

            <Modal show={showModal} onHide={closeModal} centered size="lg">
                <Form onSubmit={saveAnswer}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingAnswer ? "Cập nhật đáp án" : "Thêm đáp án"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Khóa học</Form.Label>
                                    <Form.Select value={form.courseId}
                                        onChange={e => changeModalCourse(e.target.value)}
                                        disabled={!!editingAnswer}
                                        required={!editingAnswer}>
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
                                    <Form.Label>Bài tập</Form.Label>
                                    <Form.Select value={form.assignmentId}
                                        onChange={e => changeModalAssignment(e.target.value)}
                                        disabled={!form.courseId || !!editingAnswer}
                                        required={!editingAnswer}>
                                        <option value="">-- Chọn bài tập --</option>
                                        {modalAssignments.map(a => (
                                            <option key={getAssignmentId(a)} value={getAssignmentId(a)}>
                                                {a.name}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group className="mb-3">
                            <Form.Label>Câu hỏi</Form.Label>
                            <Form.Select value={form.questionId}
                                onChange={e => setForm({ ...form, questionId: e.target.value })}
                                disabled={!form.assignmentId || !!editingAnswer}
                                required={!editingAnswer}>
                                <option value="">-- Chọn câu hỏi --</option>

                                {modalQuestions.map(x => (
                                    <option key={getQuestionId(x)} value={getQuestionId(x)}>
                                        Câu {x.orderNumber} - {
                                            x.type === "MULTIPLE_CHOICE"
                                                ? "Trắc nghiệm"
                                                : x.type === "SHORT_ANSWER"
                                                    ? "Trả lời ngắn"
                                                    : "Tự luận"
                                        }: {x.content}
                                    </option>
                                ))}
                            </Form.Select>

                            {editingAnswer && (
                                <Form.Text className="text-muted">
                                    Không thể thay đổi câu hỏi của đáp án sau khi tạo.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>
                                {selectedQuestionType === "ESSAY"
                                    ? "Đáp án tham khảo"
                                    : selectedQuestionType === "SHORT_ANSWER"
                                        ? "Câu trả lời được chấp nhận"
                                        : "Nội dung đáp án"}
                            </Form.Label>

                            <Form.Control as="textarea" rows={3}
                                value={form.content}
                                onChange={e => setForm({ ...form, content: e.target.value })}
                                required />
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Thứ tự đáp án</Form.Label>
                                    <Form.Control type="number" min="1"
                                        value={form.orderNumber}
                                        onChange={e => setForm({ ...form, orderNumber: e.target.value })}
                                        required />
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                {selectedQuestionType === "MULTIPLE_CHOICE" && (
                                    <Form.Group>
                                        <Form.Label>Đáp án đúng?</Form.Label>
                                        <Form.Select value={form.correct}
                                            onChange={e => setForm({ ...form, correct: e.target.value })}>
                                            <option value="false">Sai</option>
                                            <option value="true">Đúng</option>
                                        </Form.Select>
                                    </Form.Group>
                                )}

                                {selectedQuestionType === "SHORT_ANSWER" && (
                                    <Alert variant="info" className="mb-0">
                                        Nội dung này sẽ được xem là câu trả lời đúng được chấp nhận.
                                    </Alert>
                                )}

                                {selectedQuestionType === "ESSAY" && (
                                    <Alert variant="secondary" className="mb-0">
                                        Nội dung này chỉ dùng làm đáp án tham khảo cho giáo viên.
                                    </Alert>
                                )}
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

export default Answers;
