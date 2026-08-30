import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, ProgressBar, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/TeacherGrading.css";

const GradingDetail = () => {
    const { attemptId } = useParams();
    const nav = useNavigate();

    const [detail, setDetail] = useState(null);
    const [grades, setGrades] = useState({});
    const [comment, setComment] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState({});
    const [saved, setSaved] = useState({});
    const [finalizing, setFinalizing] = useState(false);
    const [showFinalize, setShowFinalize] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const loadDetail = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(`${endpoints.teacherGrading}/${attemptId}`);
            const data = res.data;
            const initial = {};
            const initialSaved = {};

            (data.answers || []).forEach(answer => {
                if (answer.questionType === "ESSAY") {
                    initial[answer.studentAnswerId] = {
                        score: answer.score != null ? String(answer.score) : "",
                        teacherComment: answer.teacherComment || ""
                    };
                    initialSaved[answer.studentAnswerId] = answer.score != null;
                }
            });

            setDetail(data);
            setGrades(initial);
            setSaved(initialSaved);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải bài cần chấm!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDetail();
    }, [attemptId]);

    const essayAnswers = useMemo(
        () => (detail?.answers || []).filter(answer => answer.questionType === "ESSAY"),
        [detail]
    );

    const currentEssayScore = useMemo(() => essayAnswers.reduce((sum, answer) => {
        const value = grades[answer.studentAnswerId]?.score;
        if (value === "" || value === undefined || Number.isNaN(Number(value))) return sum;
        return sum + Number(value);
    }, 0), [essayAnswers, grades]);

    const gradedEssayCount = useMemo(
        () => essayAnswers.filter(answer => {
            const value = grades[answer.studentAnswerId]?.score;
            return value !== "" && value !== undefined && !Number.isNaN(Number(value));
        }).length,
        [essayAnswers, grades]
    );

    const estimatedTotal = Number(detail?.autoScore || 0) + currentEssayScore;
    const essayProgress = essayAnswers.length ? gradedEssayCount / essayAnswers.length * 100 : 100;

    const updateGrade = (id, field, value) => {
        setGrades(prev => ({ ...prev, [id]: { ...prev[id], [field]: value } }));
        setSaved(prev => ({ ...prev, [id]: false }));
    };

    const validateEssay = answer => {
        const form = grades[answer.studentAnswerId] || {};
        const score = Number(form.score);

        if (form.score === "" || form.score === undefined || Number.isNaN(score))
            return `Vui lòng nhập điểm cho câu ${answer.orderNumber}.`;

        if (score < 0 || score > Number(answer.maximumScore))
            return `Điểm câu ${answer.orderNumber} phải từ 0 đến ${answer.maximumScore}.`;

        return "";
    };

    const saveEssay = async (answer, silent = false) => {
        const validation = validateEssay(answer);

        if (validation) {
            setErr(validation);
            return false;
        }

        const form = grades[answer.studentAnswerId] || {};
        const score = Number(form.score);

        try {
            setSaving(prev => ({ ...prev, [answer.studentAnswerId]: true }));
            setErr("");

            if (!silent) setSuccess("");

            const res = await authApis().post(
                `${endpoints.teacherGrading}/answers/${answer.studentAnswerId}`,
                { score, teacherComment: form.teacherComment?.trim() || null }
            );

            setSaved(prev => ({ ...prev, [answer.studentAnswerId]: true }));

            if (!silent)
                setSuccess(res.data?.message || `Đã lưu điểm câu ${answer.orderNumber}.`);

            return true;
        } catch (ex) {
            setSaved(prev => ({ ...prev, [answer.studentAnswerId]: false }));
            setErr(ex.response?.data?.message || `Không thể lưu điểm câu ${answer.orderNumber}!`);
            return false;
        } finally {
            setSaving(prev => ({ ...prev, [answer.studentAnswerId]: false }));
        }
    };

    const openFinalize = () => {
        const invalidAnswer = essayAnswers.find(answer => validateEssay(answer));

        if (invalidAnswer) {
            setErr(validateEssay(invalidAnswer));
            return;
        }

        setErr("");
        setShowFinalize(true);
    };

    const finalize = async () => {
        try {
            setFinalizing(true);
            setErr("");
            setSuccess("");
            setShowFinalize(false);

            for (const answer of essayAnswers) {
                const ok = await saveEssay(answer, true);

                if (!ok) {
                    setErr(`Không thể hoàn tất vì điểm câu ${answer.orderNumber} chưa được lưu thành công.`);
                    return;
                }
            }

            const res = await authApis().post(
                `${endpoints.teacherGrading}/${attemptId}/finalize`,
                { comment: comment.trim() || null }
            );

            nav("/teacher/grading", {
                state: {
                    message: `Đã hoàn tất chấm bài ${detail.assignmentName} của ${detail.studentName}. Tổng điểm: ${res.data.totalScore}/${detail.maximumScore}.`
                }
            });
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể hoàn tất chấm bài!");
        } finally {
            setFinalizing(false);
        }
    };

    const typeBadge = type => {
        const config = {
            MULTIPLE_CHOICE: ["primary", "Trắc nghiệm"],
            SHORT_ANSWER: ["info", "Trả lời ngắn"],
            ESSAY: ["warning", "Tự luận"]
        }[type] || ["secondary", type];

        return <Badge bg={config[0]} text={type === "ESSAY" ? "dark" : undefined}>{config[1]}</Badge>;
    };

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    if (!detail)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container">
                    <Alert variant="danger">{err || "Không tìm thấy bài cần chấm."}</Alert>
                    <Button variant="outline-secondary" onClick={() => nav("/teacher/grading")}>
                        ← Danh sách chờ chấm
                    </Button>
                </div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container cm-grading-detail-page">
                <Button variant="outline-secondary" size="sm" className="cm-portal-back"
                    onClick={() => nav("/teacher/grading")}>
                    ← Bài chờ chấm
                </Button>

                <div className="cm-portal-heading cm-grading-detail-heading">
                    <div>
                        <span>CHẤM BÀI</span>
                        <h1>{detail.assignmentName}</h1>
                        <p>{detail.studentName} · Lần làm #{detail.attemptNumber} · Bài làm #{detail.attemptId}</p>
                    </div>

                    <div className="cm-grading-score-preview">
                        <span>Điểm dự kiến</span>
                        <strong>{estimatedTotal}<small>/{detail.maximumScore}</small></strong>
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

                <Card className="cm-portal-card cm-grading-overview">
                    <Card.Body>
                        <Row className="g-3">
                            <Col md={4}>
                                <div className="cm-grading-stat">
                                    <span>Điểm tự động</span>
                                    <strong>{detail.autoScore ?? 0}</strong>
                                    <small>Đã được hệ thống chấm</small>
                                </div>
                            </Col>

                            <Col md={4}>
                                <div className="cm-grading-stat">
                                    <span>Điểm tự luận</span>
                                    <strong>{currentEssayScore}</strong>
                                    <small>{gradedEssayCount}/{essayAnswers.length} câu đã nhập điểm</small>
                                </div>
                            </Col>

                            <Col md={4}>
                                <div className="cm-grading-stat">
                                    <span>Điểm tối đa</span>
                                    <strong>{detail.maximumScore}</strong>
                                    <small>Toàn bộ bài tập</small>
                                </div>
                            </Col>
                        </Row>

                        {essayAnswers.length > 0 && (
                            <div className="cm-grading-progress">
                                <div>
                                    <span>Tiến độ chấm tự luận</span>
                                    <strong>{gradedEssayCount}/{essayAnswers.length} câu</strong>
                                </div>
                                <ProgressBar now={essayProgress} />
                            </div>
                        )}
                    </Card.Body>
                </Card>

                <div className="cm-grading-answer-list">
                    {(detail.answers || []).map(answer => {
                        const isEssay = answer.questionType === "ESSAY";
                        const form = grades[answer.studentAnswerId] || {};
                        const isSaved = saved[answer.studentAnswerId];

                        return (
                            <Card className={`cm-portal-card cm-grading-answer ${isEssay ? "is-essay" : ""}`}
                                key={answer.studentAnswerId}>
                                <Card.Body>
                                    <div className="cm-grading-answer-head">
                                        <div>
                                            <span className="cm-grading-question-label">CÂU {answer.orderNumber}</span>
                                            <h5>{answer.questionContent}</h5>
                                        </div>

                                        <div className="cm-grading-answer-badges">
                                            {typeBadge(answer.questionType)}
                                            <Badge bg="secondary">{answer.maximumScore} điểm</Badge>
                                        </div>
                                    </div>

                                    <div className="cm-grading-student-answer">
                                        <span>CÂU TRẢ LỜI CỦA HỌC VIÊN</span>
                                        <div>
                                            {answer.questionType === "MULTIPLE_CHOICE"
                                                ? answer.selectedAnswer || "Không có câu trả lời"
                                                : answer.answerContent || "Không có câu trả lời"}
                                        </div>
                                    </div>

                                    {!isEssay && (
                                        <div className="cm-grading-auto-result">
                                            <span>Điểm tự động</span>
                                            <strong>{answer.score ?? 0}/{answer.maximumScore}</strong>
                                            <Badge bg={Number(answer.score) > 0 ? "success" : "secondary"}>
                                                {Number(answer.score) > 0 ? "Có điểm" : "0 điểm"}
                                            </Badge>
                                        </div>
                                    )}

                                    {isEssay && (
                                        <>
                                            {(answer.referenceAnswers || []).length > 0 && (
                                                <div className="cm-grading-reference">
                                                    <span>ĐÁP ÁN THAM KHẢO</span>
                                                    {(answer.referenceAnswers || []).map((ref, index) => (
                                                        <p key={index}><b>{index + 1}.</b> {ref}</p>
                                                    ))}
                                                </div>
                                            )}

                                            <div className="cm-grading-form">
                                                <Row className="g-3">
                                                    <Col md={4}>
                                                        <Form.Group>
                                                            <Form.Label>
                                                                Điểm câu này <span>/ {answer.maximumScore}</span>
                                                            </Form.Label>
                                                            <Form.Control type="number" min="0" max={answer.maximumScore}
                                                                step="0.01"
                                                                value={form.score ?? ""}
                                                                disabled={finalizing}
                                                                onChange={e => updateGrade(answer.studentAnswerId, "score", e.target.value)}
                                                                placeholder={`0 - ${answer.maximumScore}`}
                                                            />
                                                        </Form.Group>
                                                    </Col>

                                                    <Col md={8}>
                                                        <Form.Group>
                                                            <Form.Label>Nhận xét cho câu này</Form.Label>
                                                            <Form.Control value={form.teacherComment ?? ""}
                                                                disabled={finalizing}
                                                                onChange={e => updateGrade(answer.studentAnswerId, "teacherComment", e.target.value)}
                                                                placeholder="Nhận xét ngắn cho học viên..."
                                                            />
                                                        </Form.Group>
                                                    </Col>
                                                </Row>

                                                <div className="cm-grading-form-actions">
                                                    <span className={isSaved ? "is-saved" : ""}>
                                                        {saving[answer.studentAnswerId]
                                                            ? "Đang lưu..."
                                                            : isSaved
                                                                ? "✓ Đã lưu"
                                                                : "Chưa lưu thay đổi"}
                                                    </span>

                                                    <Button size="sm" variant="outline-primary"
                                                        disabled={saving[answer.studentAnswerId] || finalizing}
                                                        onClick={() => saveEssay(answer)}>
                                                        {saving[answer.studentAnswerId] ? "Đang lưu..." : "Lưu điểm câu này"}
                                                    </Button>
                                                </div>
                                            </div>
                                        </>
                                    )}
                                </Card.Body>
                            </Card>
                        );
                    })}
                </div>

                <Card className="cm-portal-card cm-grading-final-card">
                    <Card.Body>
                        <div>
                            <span className="cm-portal-label">HOÀN TẤT CHẤM BÀI</span>
                            <h5>Nhận xét chung</h5>
                            <p>Nhận xét này sẽ được lưu cùng kết quả bài làm của học viên.</p>
                        </div>

                        <Form.Control as="textarea" rows={4} value={comment}
                            disabled={finalizing}
                            onChange={e => setComment(e.target.value)}
                            placeholder="Nhập nhận xét chung cho bài làm..."
                        />

                        <div className="cm-grading-final-actions">
                            <div>
                                <span>Tổng điểm dự kiến</span>
                                <strong>{estimatedTotal}/{detail.maximumScore}</strong>
                            </div>

                            <Button variant="success" size="lg" disabled={finalizing} onClick={openFinalize}>
                                {finalizing ? "Đang hoàn tất..." : "Hoàn tất chấm bài"}
                            </Button>
                        </div>
                    </Card.Body>
                </Card>
            </div>

            <Modal show={showFinalize} onHide={() => !finalizing && setShowFinalize(false)} centered>
                <Modal.Header closeButton={!finalizing}>
                    <Modal.Title>Xác nhận hoàn tất chấm bài</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <div className="cm-grading-confirm-summary">
                        <div>
                            <span>Học viên</span>
                            <strong>{detail.studentName}</strong>
                        </div>
                        <div>
                            <span>Bài tập</span>
                            <strong>{detail.assignmentName}</strong>
                        </div>
                        <div>
                            <span>Điểm tự động</span>
                            <strong>{detail.autoScore ?? 0}</strong>
                        </div>
                        <div>
                            <span>Điểm tự luận</span>
                            <strong>{currentEssayScore}</strong>
                        </div>
                        <div className="is-total">
                            <span>Tổng điểm dự kiến</span>
                            <strong>{estimatedTotal}/{detail.maximumScore}</strong>
                        </div>
                    </div>

                    <Alert variant="warning" className="mb-0">
                        Sau khi hoàn tất, bài làm sẽ chuyển sang trạng thái <strong>đã chấm</strong>.
                        Backend sẽ tự xác định đạt/chưa đạt và cập nhật lộ trình học tập.
                    </Alert>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="outline-secondary" disabled={finalizing}
                        onClick={() => setShowFinalize(false)}>
                        Kiểm tra lại
                    </Button>
                    <Button variant="success" disabled={finalizing} onClick={finalize}>
                        {finalizing ? "Đang hoàn tất..." : "Xác nhận hoàn tất"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default GradingDetail;
