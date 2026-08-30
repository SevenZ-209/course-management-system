import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, ProgressBar, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Classes = () => {
    const nav = useNavigate();

    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const loadClasses = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints.teacherClasses);
            setClasses(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load teacher classes error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách lớp học!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadClasses();
    }, []);

    const statusBadge = status => {
        if (status === "ACTIVE") return <Badge bg="success">Đang hoạt động</Badge>;
        if (status === "UPCOMING") return <Badge bg="primary">Sắp bắt đầu</Badge>;
        if (status === "COMPLETED") return <Badge bg="secondary">Đã kết thúc</Badge>;
        return <Badge bg="secondary">{status || "-"}</Badge>;
    };

    const formatDate = value => value
        ? new Date(value).toLocaleDateString("vi-VN")
        : "-";

    const activeCount = classes.filter(item => item.status === "ACTIVE").length;
    const totalStudents = classes.reduce((sum, item) => sum + Number(item.studentCount || 0), 0);

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
                        <span>GIẢNG DẠY</span>
                        <h1>Lớp học của tôi</h1>
                        <p>Quản lý các lớp học và học viên bạn đang phụ trách.</p>
                    </div>

                    <div className="d-flex gap-2">
                        <div className="cm-portal-summary">
                            <span>Lớp đang hoạt động</span>
                            <strong>{activeCount}</strong>
                        </div>

                        <div className="cm-portal-summary">
                            <span>Tổng học viên</span>
                            <strong>{totalStudents}</strong>
                        </div>
                    </div>
                </div>

                {err && <Alert variant="danger">{err}</Alert>}

                {classes.length === 0 ? (
                    <div className="cm-portal-empty">
                        Bạn chưa được phân công lớp học nào.
                    </div>
                ) : (
                    <Row className="g-4">
                        {classes.map(item => {
                            const percent = item.maxStudents
                                ? Math.min((item.studentCount / item.maxStudents) * 100, 100)
                                : 0;

                            return (
                                <Col lg={6} xl={4} key={item.classId}>
                                    <Card className="cm-portal-card cm-portal-hover h-100">
                                        <Card.Body className="p-4 d-flex flex-column">
                                            <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
                                                <div>
                                                    <span className="cm-portal-label">{item.courseName}</span>
                                                    <h5 className="cm-portal-title mt-2 mb-0">{item.className}</h5>
                                                </div>

                                                {statusBadge(item.status)}
                                            </div>

                                            <div className="cm-portal-muted mb-4">
                                                {formatDate(item.startDate)} → {formatDate(item.endDate)}
                                            </div>

                                            <div className="d-flex justify-content-between align-items-center mb-2">
                                                <span className="cm-portal-muted">Học viên</span>
                                                <strong>{item.studentCount}/{item.maxStudents}</strong>
                                            </div>

                                            <ProgressBar now={percent} className="cm-portal-progress mb-4" />

                                            <Button className="mt-auto"
                                                onClick={() => nav(`/teacher/classes/${item.classId}`)}>
                                                Xem lớp học
                                            </Button>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            );
                        })}
                    </Row>
                )}
            </div>
        </div>
    );
};

export default Classes;