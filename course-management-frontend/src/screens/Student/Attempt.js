import { useEffect, useRef, useState } from "react";
import { Alert, Badge, Button, Card, Form, Modal, ProgressBar } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";
import "../../styles/Attempt.css";

const Attempt = () => {
    const { attemptId } = useParams();
    const nav = useNavigate();

    const [attempt, setAttempt] = useState(null);
    const [attemptStatus, setAttemptStatus] = useState("IN_PROGRESS");
    const [answers, setAnswers] = useState({});
    const [remaining, setRemaining] = useState(0);
    const [loading, setLoading] = useState(true);
    const [saveStatus, setSaveStatus] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [showSubmitModal, setShowSubmitModal] = useState(false);
    const [err, setErr] = useState("");

    const autoSubmitted = useRef(false);
    const essayTimers = useRef({});
    const questionRefs = useRef({});

    const hasTimeLimit = !!attempt?.endTime;
    const timeExpired = hasTimeLimit && remaining <= 0;
    const getQuestionId = q => q.questionId ?? q.id;
    const getOptionId = o => o.answerId ?? o.id;

    const loadAttempt = async () => {
        try {
            setLoading(true);
            setErr("");

            const [detailRes, statusRes] = await Promise.all([
                authApis().get(`${endpoints.studentAssignments}/attempts/${attemptId}`),
                authApis().get(`${endpoints.studentAssignments}/attempts/${attemptId}/result`)
            ]);

            const data = detailRes.data;
            const status = statusRes.data?.status || "IN_PROGRESS";

            if (status !== "IN_PROGRESS") {
                nav(`/student/assignments/result/${attemptId}`, { replace: true });
                return;
            }

            const initial = {};
            (data.questions || []).forEach(q => {
                initial[getQuestionId(q)] = {
                    selectedAnswerId: q.selectedAnswerId ?? "",
                    answerContent: q.answerContent ?? ""
                };
            });

            setAttempt(data);
            setAttemptStatus(status);
            setRemaining(data.remainingSeconds ?? 0);
            setAnswers(initial);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải bài làm!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAttempt();
    }, [attemptId]);

    useEffect(() => {
        return () => Object.values(essayTimers.current).forEach(timer => clearTimeout(timer));
    }, []);

    useEffect(() => {
        if (!attempt?.endTime || attemptStatus !== "IN_PROGRESS") return;

        const timer = setInterval(() => {
            setRemaining(value => Math.max(value - 1, 0));
        }, 1000);

        return () => clearInterval(timer);
    }, [attempt?.endTime, attemptStatus]);

    useEffect(() => {
        if (attemptStatus === "IN_PROGRESS" && attempt?.endTime && remaining === 0 && !autoSubmitted.current) {
            autoSubmitted.current = true;
            submitAttempt(true);
        }
    }, [remaining, attempt?.endTime, attemptStatus]);

    const saveAnswer = async (question, value) => {
        if (attemptStatus !== "IN_PROGRESS") return false;

        const questionId = getQuestionId(question);

        try {
            setSaveStatus(prev => ({ ...prev, [questionId]: "saving" }));

            const body = {
                questionId,
                selectedAnswerId: question.type === "MULTIPLE_CHOICE" && value ? Number(value) : null,
                answerContent: question.type !== "MULTIPLE_CHOICE" ? value : null
            };

            await authApis().put(`${endpoints.studentAssignments}/attempts/${attemptId}/answers`, body);
            setSaveStatus(prev => ({ ...prev, [questionId]: "saved" }));
            return true;
        } catch (ex) {
            setSaveStatus(prev => ({ ...prev, [questionId]: "error" }));
            setErr(ex.response?.data?.message || "Không thể lưu câu trả lời!");
            return false;
        }
    };

    const selectChoice = async (question, value) => {
        const id = getQuestionId(question);

        setAnswers(prev => ({
            ...prev,
            [id]: { ...prev[id], selectedAnswerId: value }
        }));

        await saveAnswer(question, value);
    };

    const changeTextAnswer = (question, value) => {
        const id = getQuestionId(question);

        setAnswers(prev => ({
            ...prev,
            [id]: { ...prev[id], answerContent: value }
        }));

        setSaveStatus(prev => ({ ...prev, [id]: "changed" }));
        clearTimeout(essayTimers.current[id]);

        essayTimers.current[id] = setTimeout(() => {
            saveAnswer(question, value);
        }, 900);
    };

    const saveTextAnswer = question => {
        const id = getQuestionId(question);
        clearTimeout(essayTimers.current[id]);
        return saveAnswer(question, answers[id]?.answerContent || "");
    };

    const isAnswered = question => {
        const answer = answers[getQuestionId(question)];

        return question.type === "MULTIPLE_CHOICE"
            ? !!answer?.selectedAnswerId
            : !!answer?.answerContent?.trim();
    };

    const saveAllAnswers = async () => {
        Object.values(essayTimers.current).forEach(timer => clearTimeout(timer));

        const requests = (attempt?.questions || []).map(question => {
            if (!isAnswered(question)) return null;

            const id = getQuestionId(question);
            const answer = answers[id] || {};

            return question.type === "MULTIPLE_CHOICE"
                ? saveAnswer(question, answer.selectedAnswerId)
                : saveAnswer(question, answer.answerContent);
        }).filter(Boolean);

        const results = await Promise.all(requests);
        return results.every(Boolean);
    };

    const submitAttempt = async auto => {
        if (submitting) return;

        try {
            setSubmitting(true);
            setErr("");

            const savedAll = await saveAllAnswers();

            if (!savedAll && !auto) {
                setErr("Có câu trả lời chưa lưu được. Vui lòng kiểm tra lại trước khi nộp bài.");
                return;
            }

            const res = await authApis().post(`${endpoints.studentAssignments}/attempts/${attemptId}/submit`);

            nav(`/student/assignments/result/${res.data.attemptId}`, {
                state: { submitResult: res.data }
            });
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể nộp bài!");
        } finally {
            setSubmitting(false);
        }
    };

    const confirmSubmit = async () => {
        setShowSubmitModal(false);
        await submitAttempt(false);
    };

    const formatTime = seconds => {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;

        return h > 0
            ? `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`
            : `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    const scrollToQuestion = question => {
        const id = getQuestionId(question);
        questionRefs.current[id]?.scrollIntoView({ behavior: "smooth", block: "center" });
    };

    const getSaveLabel = question => {
        const status = saveStatus[getQuestionId(question)];

        if (status === "saving") return "Đang lưu...";
        if (status === "saved") return "Đã lưu";
        if (status === "error") return "Lưu thất bại";
        if (status === "changed") return "Chưa lưu";
        return "";
    };

    const questions = attempt?.questions || [];
    const answeredCount = questions.filter(isAnswered).length;
    const totalQuestions = questions.length;
    const unansweredCount = totalQuestions - answeredCount;
    const progress = totalQuestions ? answeredCount / totalQuestions * 100 : 0;

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    if (err && !attempt)
        return (
            <div className="cm-portal-page">
                <div className="cm-portal-container">
                    <Alert variant="danger">{err}</Alert>
                    <Button variant="outline-secondary" onClick={() => nav("/student/assignments")}>
                        ← Bài tập của tôi
                    </Button>
                </div>
            </div>
        );

    return (
        <div className="cm-portal-page cm-attempt-page">
            <div className="cm-portal-container cm-attempt-container">
                <Card className="cm-portal-card cm-attempt-header">
                    <Card.Body>
                        <div className="cm-attempt-header-top">
                            <div>
                                <span className="cm-portal-label">BÀI LÀM</span>
                                <h2 className="cm-attempt-title">{attempt.assignmentName}</h2>
                                <div className="cm-attempt-meta">
                                    <span>Lần làm #{attempt.attemptNumber}</span>
                                    <i />
                                    <span>Tối đa {attempt.maximumScore} điểm</span>
                                    <i />
                                    <span>{totalQuestions} câu hỏi</span>
                                </div>
                            </div>

                            <div className={`cm-attempt-timer ${timeExpired ? "is-expired" : ""}`}>
                                <span>{hasTimeLimit ? "Thời gian còn lại" : "Thời gian làm bài"}</span>
                                <strong>{hasTimeLimit ? formatTime(remaining) : "Không giới hạn"}</strong>
                            </div>
                        </div>

                        <div className="cm-attempt-progress-head">
                            <span>Tiến độ làm bài</span>
                            <strong>{answeredCount}/{totalQuestions} câu</strong>
                        </div>
                        <ProgressBar now={progress} className="cm-portal-progress" />

                        <div className="cm-attempt-question-nav">
                            {questions.map((question, index) => {
                                const answered = isAnswered(question);
                                return (
                                    <button key={getQuestionId(question)} type="button"
                                        className={`cm-attempt-question-dot ${answered ? "is-answered" : ""}`}
                                        onClick={() => scrollToQuestion(question)}
                                        title={`Đi đến câu ${question.orderNumber ?? index + 1}`}>
                                        {question.orderNumber ?? index + 1}
                                    </button>
                                );
                            })}
                            <span className="cm-attempt-nav-note">
                                <b>{answeredCount}</b> đã trả lời · <b>{unansweredCount}</b> chưa trả lời
                            </span>
                        </div>
                    </Card.Body>
                </Card>

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                {timeExpired && (
                    <Alert variant="warning">
                        Đã hết thời gian làm bài. Hệ thống đang nộp bài của bạn.
                    </Alert>
                )}

                <div className="cm-attempt-question-list">
                    {questions.map((question, index) => {
                        const id = getQuestionId(question);
                        const answer = answers[id] || {};
                        const answered = isAnswered(question);
                        const saveLabel = getSaveLabel(question);

                        return (
                            <Card className={`cm-portal-card cm-attempt-question ${answered ? "is-answered" : ""}`}
                                key={id} ref={node => questionRefs.current[id] = node}>
                                <Card.Body>
                                    <div className="cm-attempt-question-head">
                                        <div>
                                            <span className="cm-attempt-question-kicker">CÂU {question.orderNumber ?? index + 1}</span>
                                            <h5>{question.content}</h5>
                                        </div>

                                        <div className="cm-attempt-badges">
                                            <Badge bg={question.type === "MULTIPLE_CHOICE" ? "primary"
                                                : question.type === "SHORT_ANSWER" ? "info" : "warning"}
                                                text={question.type === "ESSAY" ? "dark" : undefined}>
                                                {question.type === "MULTIPLE_CHOICE"
                                                    ? "Trắc nghiệm"
                                                    : question.type === "SHORT_ANSWER"
                                                        ? "Trả lời ngắn"
                                                        : "Tự luận"}
                                            </Badge>
                                            <Badge bg="secondary">{question.score} điểm</Badge>
                                        </div>
                                    </div>

                                    {question.type === "MULTIPLE_CHOICE" ? (
                                        <div className="cm-attempt-options">
                                            {(question.options || []).map((option, optionIndex) => {
                                                const optionId = getOptionId(option);
                                                const selected = String(answer.selectedAnswerId) === String(optionId);

                                                return (
                                                    <div key={optionId}
                                                        className={`cm-attempt-option ${selected ? "is-selected" : ""}`}>
                                                        <Form.Check type="radio" name={`question-${id}`}
                                                            id={`answer-${optionId}`}
                                                            label={
                                                                <span>
                                                                    <b>{String.fromCharCode(65 + optionIndex)}</b>
                                                                    {option.content}
                                                                </span>
                                                            }
                                                            value={optionId}
                                                            checked={selected}
                                                            disabled={timeExpired || submitting}
                                                            onChange={e => selectChoice(question, e.target.value)}
                                                        />
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    ) : (
                                        <div className="cm-attempt-text-answer">
                                            <Form.Control as="textarea"
                                                rows={question.type === "SHORT_ANSWER" ? 3 : 6}
                                                value={answer.answerContent || ""}
                                                disabled={timeExpired || submitting}
                                                placeholder={question.type === "SHORT_ANSWER"
                                                    ? "Nhập câu trả lời ngắn..."
                                                    : "Nhập câu trả lời tự luận..."}
                                                onChange={e => changeTextAnswer(question, e.target.value)}
                                                onBlur={() => {
                                                    if (answer.answerContent?.trim() && !timeExpired) saveTextAnswer(question);
                                                }}
                                            />
                                            <div className="cm-attempt-answer-actions">
                                                <span className={`cm-attempt-save-state ${saveStatus[id] === "error" ? "is-error" : ""}`}>
                                                    {saveLabel}
                                                </span>
                                                <Button size="sm" variant="outline-primary"
                                                    disabled={saveStatus[id] === "saving" || submitting || timeExpired}
                                                    onClick={() => saveTextAnswer(question)}>
                                                    {saveStatus[id] === "saving" ? "Đang lưu..." : "Lưu câu trả lời"}
                                                </Button>
                                            </div>
                                        </div>
                                    )}

                                    {question.type === "MULTIPLE_CHOICE" && saveLabel && (
                                        <div className={`cm-attempt-save-state mt-2 ${saveStatus[id] === "error" ? "is-error" : ""}`}>
                                            {saveLabel}
                                        </div>
                                    )}
                                </Card.Body>
                            </Card>
                        );
                    })}
                </div>

                <Card className="cm-portal-card cm-attempt-submit-card">
                    <Card.Body>
                        <div>
                            <span className="cm-portal-label">HOÀN TẤT BÀI LÀM</span>
                            <h5>{unansweredCount === 0 ? "Bạn đã trả lời tất cả câu hỏi" : `Còn ${unansweredCount} câu chưa trả lời`}</h5>
                            <p>Kiểm tra lại đáp án trước khi nộp. Sau khi nộp bạn sẽ không thể chỉnh sửa bài làm này.</p>
                        </div>

                        <Button variant="success" size="lg" disabled={submitting}
                            onClick={() => setShowSubmitModal(true)}>
                            {submitting ? "Đang nộp..." : "Nộp bài"}
                        </Button>
                    </Card.Body>
                </Card>
            </div>

            <Modal show={showSubmitModal} onHide={() => !submitting && setShowSubmitModal(false)} centered>
                <Modal.Header closeButton={!submitting}>
                    <Modal.Title>Xác nhận nộp bài</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="cm-attempt-submit-summary">
                        <div><span>Đã trả lời</span><strong>{answeredCount}/{totalQuestions} câu</strong></div>
                        {hasTimeLimit && <div><span>Thời gian còn lại</span><strong>{formatTime(remaining)}</strong></div>}
                    </div>

                    {unansweredCount > 0 ? (
                        <Alert variant="warning" className="mb-0">
                            Bạn còn <strong>{unansweredCount} câu chưa trả lời</strong>. Bạn vẫn có thể nộp bài nếu muốn.
                        </Alert>
                    ) : (
                        <p className="mb-0">Tất cả câu hỏi đã có câu trả lời. Bạn có chắc muốn nộp bài?</p>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-secondary" disabled={submitting}
                        onClick={() => setShowSubmitModal(false)}>
                        Kiểm tra lại
                    </Button>
                    <Button variant="success" disabled={submitting} onClick={confirmSubmit}>
                        {submitting ? "Đang nộp..." : "Xác nhận nộp"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default Attempt;
