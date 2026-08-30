import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Courses = () => {
    const nav = useNavigate();

    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const loadCourses = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.studentCourses);
            setCourses(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load student courses error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải khóa học!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadCourses();
    }, []);

    const enrollmentId = c => c.enrollmentId ?? c.id;
    const courseName = c => c.courseName ?? c.course?.name ?? "Khóa học";
    const className = c => c.className ?? c.courseClassName ?? c.courseClass?.name ?? "-";
    const formatDate = value => value ? new Date(value).toLocaleDateString("vi-VN") : "-";

    const classBadge = status => {
        const config = {
            UPCOMING: ["info", "Sắp diễn ra"],
            ACTIVE: ["success", "Đang học"],
            COMPLETED: ["secondary", "Đã kết thúc"],
            CANCELED: ["danger", "Đã hủy"]
        }[status] || ["secondary", status || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    if (loading)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <div className="cm-portal-heading">
                    <div>
                        <span>KHÓA HỌC</span>
                        <h1>Khóa học của tôi</h1>
                        <p>Bạn đang tham gia {courses.length} khóa học.</p>
                    </div>

                    <div className="cm-portal-summary">
                        <span>Tổng khóa học</span>
                        <strong>{courses.length}</strong>
                    </div>
                </div>

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                {courses.length === 0 ? (
                    <div className="cm-portal-empty">
                        Bạn chưa có khóa học nào đang hoạt động.
                    </div>
                ) : (
                    <Row className="g-4">
                        {courses.map(c => (
                            <Col xl={4} md={6} key={enrollmentId(c)}>
                                <Card className="cm-portal-card cm-portal-hover h-100">
                                    <Card.Body className="p-4 d-flex flex-column">
                                        <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
                                            <div>
                                                <span className="cm-portal-label">KHÓA HỌC</span>
                                                <h5 className="cm-portal-title mt-2 mb-0">{courseName(c)}</h5>
                                            </div>

                                            {classBadge(c.classStatus ?? c.status)}
                                        </div>

                                        <div className="mb-2">
                                            <span className="cm-portal-muted">Lớp: </span>
                                            <strong>{className(c)}</strong>
                                        </div>

                                        <div className="cm-portal-muted mb-4">
                                            {formatDate(c.startDate)} → {formatDate(c.endDate)}
                                        </div>

                                        <Button className="mt-auto"
                                            onClick={() => nav(`/student/courses/${enrollmentId(c)}`)}>
                                            Vào khóa học
                                        </Button>
                                    </Card.Body>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )}
            </div>
        </div>
    );
};

export default Courses;