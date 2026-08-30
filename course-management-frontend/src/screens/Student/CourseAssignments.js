import { useState } from "react";
import { Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import "../../styles/CourseAssignments.css";

const CourseAssignments = ({ assignments = [] }) => {
    const nav = useNavigate();
    const [starting, setStarting] = useState(null);
    const [err, setErr] = useState("");

    const pathAssignments = assignments
        .filter(item => item.orderNumber != null)
        .sort((a, b) => a.orderNumber - b.orderNumber);
    const manualAssignments = assignments.filter(item => item.orderNumber == null);

    const completedCount = pathAssignments.filter(item => item.status === "COMPLETED").length;
    const progress = pathAssignments.length ? Math.round(completedCount / pathAssignments.length * 100) : 0;
    const currentAssignment = pathAssignments.find(item =>
        item.status === "AVAILABLE" && !["SUBMITTED", "PENDING_GRADING"].includes(item.latestAttemptStatus)
    );

    const startAssignment = async item => {
        if (!item.assignedAssignmentId) {
            setErr("Bài tập chưa được giao!");
            return;
        }

        try {
            setStarting(item.assignedAssignmentId);
            setErr("");

            const res = await authApis().post(
                `${endpoints.studentAssignments}/${item.assignedAssignmentId}/start`
            );

            nav(`/student/assignments/attempt/${res.data.attemptId}`);
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể bắt đầu làm bài!");
        } finally {
            setStarting(null);
        }
    };

    const getUiState = item => {
        if (item.status === "COMPLETED")
            return {
                key: "completed",
                label: "Đã hoàn thành",
                description: "Bài đã đạt yêu cầu và tiến độ đã được ghi nhận.",
                marker: "✓"
            };

        if (item.status === "LOCKED")
            return {
                key: "locked",
                label: "Chưa mở",
                description: "Hoàn thành bài trước trong lộ trình để mở khóa bài này.",
                marker: "🔒"
            };

        if (item.latestAttemptStatus === "IN_PROGRESS")
            return {
                key: "doing",
                label: "Đang làm",
                description: "Bạn đã bắt đầu bài này. Tiếp tục từ phần đang làm dở.",
                marker: "▶"
            };

        if (["SUBMITTED", "PENDING_GRADING"].includes(item.latestAttemptStatus))
            return {
                key: "pending",
                label: "Chờ chấm",
                description: "Bài đã được nộp và đang chờ hệ thống hoặc giáo viên hoàn tất chấm điểm.",
                marker: "…"
            };

        if (item.latestAttemptStatus === "GRADED" && item.canStart)
            return {
                key: "retry",
                label: "Có thể làm lại",
                description: "Lần làm gần nhất chưa đạt yêu cầu. Bạn có thể thực hiện lần tiếp theo.",
                marker: "↻"
            };

        if (item.status === "AVAILABLE")
            return {
                key: "available",
                label: "Sẵn sàng",
                description: "Bài đã được mở. Bạn có thể bắt đầu làm ngay.",
                marker: "▶"
            };

        return {
            key: "disabled",
            label: "Chưa thể làm",
            description: "Bài tập hiện chưa thể thực hiện.",
            marker: "•"
        };
    };

    const renderAction = item => {
        const state = getUiState(item);

        if (state.key === "doing" && item.latestAttemptId)
            return (
                <button className="cm-assignment-action primary"
                    onClick={() => nav(`/student/assignments/attempt/${item.latestAttemptId}`)}>
                    Tiếp tục làm
                </button>
            );

        if (state.key === "pending" && item.latestAttemptId)
            return (
                <button className="cm-assignment-action secondary"
                    onClick={() => nav(`/student/assignments/result/${item.latestAttemptId}`)}>
                    Xem trạng thái
                </button>
            );

        if (state.key === "completed" && (item.latestAttemptId || item.attemptId))
            return (
                <button className="cm-assignment-action secondary"
                    onClick={() => nav(`/student/assignments/result/${item.latestAttemptId || item.attemptId}`)}>
                    Xem kết quả
                </button>
            );

        if (["available", "retry"].includes(state.key) && item.status === "AVAILABLE")
            return (
                <button className="cm-assignment-action primary"
                    disabled={starting === item.assignedAssignmentId || item.canStart === false}
                    onClick={() => startAssignment(item)}>
                    {starting === item.assignedAssignmentId
                        ? "Đang bắt đầu..."
                        : state.key === "retry" ? "Làm lại bài" : "Bắt đầu làm"}
                </button>
            );

        return null;
    };

    const renderAssignment = (item, index, isPathAssignment = true) => {
        const state = getUiState(item);
        const attemptText = item.latestAttemptNumber
            ? `Lần làm gần nhất #${item.latestAttemptNumber}`
            : null;

        return (
            <div className={`cm-assignment-row ${state.key}`} key={item.assignedAssignmentId ?? item.assignmentId}>
                <div className="cm-assignment-step-wrap">
                    <div className={`cm-assignment-step ${state.key}`}>
                        {isPathAssignment ? String(item.orderNumber ?? index + 1).padStart(2, "0") : "BT"}
                    </div>
                    {isPathAssignment && index < pathAssignments.length - 1 && <div className="cm-assignment-line" />}
                </div>

                <div className="cm-assignment-content">
                    <div className="cm-assignment-topline">
                        <div>
                            <div className="cm-assignment-kicker">
                                {isPathAssignment ? `Bài ${item.orderNumber}` : "Giáo viên giao thêm"}
                            </div>
                            <h5>{item.assignmentName}</h5>
                        </div>

                        <span className={`cm-assignment-state ${state.key}`}>
                            <span>{state.marker}</span>{state.label}
                        </span>
                    </div>

                    <p className="cm-assignment-description">{state.description}</p>

                    <div className="cm-assignment-footer">
                        <div className="cm-assignment-meta">
                            {item.assignmentType && (
                                <span>{item.assignmentType === "TEST" ? "Kiểm tra" : "Luyện tập"}</span>
                            )}
                            {item.durationMinutes != null && (
                                <span>{item.durationMinutes > 0 ? `${item.durationMinutes} phút` : "Không giới hạn thời gian"}</span>
                            )}
                            {attemptText && <span>{attemptText}</span>}
                        </div>

                        {renderAction(item)}
                    </div>
                </div>
            </div>
        );
    };

    if (!assignments.length)
        return (
            <div className="course-assignments">
                <div className="cm-portal-empty">Khóa học chưa có bài tập dành cho bạn.</div>
            </div>
        );

    return (
        <div className="course-assignments">
            <div className="cm-assignment-overview">
                <div>
                    <span className="cm-portal-label">BÀI TẬP</span>
                    <h3>Bài tập trong khóa học</h3>
                    <p>
                        Hoàn thành lần lượt các bài trong lộ trình. Bài tiếp theo sẽ tự mở khi bạn đạt yêu cầu.
                    </p>
                </div>

                {pathAssignments.length > 0 && (
                    <div className="cm-assignment-progress-box">
                        <div className="cm-assignment-progress-head">
                            <span>Tiến độ bài tập</span>
                            <strong>{completedCount}/{pathAssignments.length}</strong>
                        </div>
                        <div className="cm-assignment-progress-track">
                            <div style={{ width: `${progress}%` }} />
                        </div>
                        <small>
                            {currentAssignment
                                ? `Tiếp theo: ${currentAssignment.assignmentName}`
                                : completedCount === pathAssignments.length
                                    ? "Bạn đã hoàn thành toàn bộ bài trong lộ trình."
                                    : "Tiếp tục theo lộ trình học tập."}
                        </small>
                    </div>
                )}
            </div>

            {err && (
                <Alert variant="danger" dismissible onClose={() => setErr("")}>
                    {err}
                </Alert>
            )}

            {pathAssignments.length > 0 && (
                <div className="cm-assignment-list">
                    {pathAssignments.map((item, index) => renderAssignment(item, index, true))}
                </div>
            )}

            {manualAssignments.length > 0 && (
                <div className="cm-manual-assignments">
                    <div className="cm-manual-heading">
                        <span className="cm-portal-label">BÀI GIAO THÊM</span>
                        <h5>Bài tập từ giáo viên</h5>
                        <p>Các bài này độc lập với thứ tự mở khóa của lộ trình chính.</p>
                    </div>

                    <div className="cm-assignment-list manual">
                        {manualAssignments.map((item, index) => renderAssignment(item, index, false))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseAssignments;
