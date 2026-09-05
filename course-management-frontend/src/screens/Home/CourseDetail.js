import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Modal, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { MyUserContext } from "../../configs/Contexts";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/CourseDetail.css";

const CourseDetail = () => {
    const { courseId } = useParams();
    const [user] = useContext(MyUserContext);
    const nav = useNavigate();

    const [course, setCourse] = useState(null);
    const [classes, setClasses] = useState([]);
    const [selectedClass, setSelectedClass] = useState(null);
    const [loading, setLoading] = useState(true);
    const [enrolling, setEnrolling] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");

    const loadData = async () => {
        try {
            setLoading(true);
            setErr("");

            const [courseRes, classRes] = await Promise.all([
                Apis.get(`${endpoints.publicCourses}/${courseId}`),
                Apis.get(endpoints.publicCourseClasses(courseId))
            ]);

            setCourse(courseRes.data);
            setClasses(Array.isArray(classRes.data) ? classRes.data : []);
        } catch(ex) {
            setErr(ex.response?.data?.message || "Không thể tải khóa học!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadData(); }, [courseId]);

    const register = classItem => {
        if(!user) {
            nav(`/login?next=/courses/${courseId}`);
            return;
        }

        if(user.role !== "STUDENT") {
            setErr("Chỉ tài khoản học viên mới có thể đăng ký khóa học!");
            return;
        }

        setSelectedClass(classItem);
    };

    const confirmEnrollment = async () => {
        try {
            setEnrolling(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(
                endpoints.studentEnrollments,
                { classId: selectedClass.id }
            );

            const enrollmentId =
                res.data?.id ?? res.data?.enrollmentId;

            setSelectedClass(null);
            setSuccess("Đăng ký lớp thành công. Vui lòng tiếp tục thanh toán.");

            if(enrollmentId)
                nav(`/student/payments/${enrollmentId}`);
        } catch(ex) {
            setErr(ex.response?.data?.message || "Đăng ký lớp thất bại!");
        } finally {
            setEnrolling(false);
        }
    };

    const money = value => {
        const amount = Number(value || 0);
        return amount === 0
            ? "Miễn phí"
            : `${amount.toLocaleString("vi-VN")} VNĐ`;
    };

    const classStatus = status =>
        status === "ACTIVE"
            ? <Badge bg="success">Đang diễn ra</Badge>
            : <Badge bg="primary">Sắp khai giảng</Badge>;

    if(loading)
        return <div className="text-center py-5"><MySpinner /></div>;

    if(!course)
        return (
            <div className="cm-section py-5">
                <Alert variant="danger">{err || "Không tìm thấy khóa học."}</Alert>
            </div>
        );

    return (
        <div className="cm-page">
        <section className="cm-section">

            <div className="cm-detail-card">

                <div className="cm-detail-image">
                    <img
                        src={course.imageUrl || "/images/course-default.jpg"}
                        alt={course.name}
                    />
                </div>

                <div className="cm-detail-info">

                    <span className="cm-course-tag">
                        {course.categoryName?.replace("[SEED] ","")}
                    </span>

                    <h1>{course.name}</h1>

                    <p>
                        {course.description || "Thông tin khóa học đang được cập nhật."}
                    </p>

                    <strong className="cm-detail-price">
                        {money(course.tuitionFee)}
                    </strong>

                    <Button onClick={() =>
                        document.getElementById("classes")
                        ?.scrollIntoView({ behavior:"smooth" })
                    }>
                        Xem lớp đang mở
                    </Button>

                </div>

            </div>

            <div id="classes" className="cm-class-section">

                <h2>Lớp đang mở</h2>

                <p className="cm-result-count">
                    Chọn lớp phù hợp với lịch học của bạn
                </p>

                {success && (
                    <Alert variant="success">
                        {success}
                    </Alert>
                )}

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                <Row className="g-4">

                    {classes.length > 0 ? classes.map(c => (

                        <Col key={c.id} xs={12} md={6} lg={4}>

                            <div className="cm-home-card cm-class-card">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <h3>{c.name}</h3>
                                        {classStatus(c.status)}
                                    </div>

                                    <p>
                                        Bắt đầu: <strong>{c.startDate || "Đang cập nhật"}</strong>
                                    </p>

                                    <p>
                                        Kết thúc: <strong>{c.endDate || "Đang cập nhật"}</strong>
                                    </p>

                                    <p>
                                        Học viên:
                                        <strong className={
                                            c.maxStudents - c.currentStudents <= 5
                                                ? "text-danger"
                                                : ""
                                        }>
                                            {c.currentStudents}/{c.maxStudents}
                                        </strong>
                                    </p>

                                    <Button
                                        disabled={c.currentStudents >= c.maxStudents}
                                        onClick={() => register(c)}
                                    >
                                        {c.currentStudents >= c.maxStudents
                                            ? "Lớp đã đầy"
                                            : "Đăng ký lớp này"}
                                    </Button>

                                </div>

                            </div>

                        </Col>

                    )) : (

                        <div className="cm-empty">
                            Chưa có lớp đang mở
                        </div>

                    )}

                </Row>

            </div>

        </section>

            <Modal show={!!selectedClass}
                onHide={() => setSelectedClass(null)}
                centered>
                <Modal.Header closeButton>
                    <Modal.Title>Xác nhận đăng ký</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <p className="mb-2">
                        Bạn đang đăng ký:
                    </p>

                    <h5 className="fw-bold">
                        {course.name}
                    </h5>

                    <div className="cm-confirm-class">
                        <strong>{selectedClass?.name}</strong>
                        <span>
                            {selectedClass?.startDate || "-"}
                            {" → "}
                            {selectedClass?.endDate || "-"}
                        </span>
                    </div>

                    <div className="mt-3">
                        Học phí:
                        <strong className="ms-2 text-danger">
                            {money(course.tuitionFee)}
                        </strong>
                    </div>

                    <small className="text-muted d-block mt-3">
                        Sau khi đăng ký, trạng thái sẽ là chờ thanh toán.
                    </small>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="outline-secondary"
                        onClick={() => setSelectedClass(null)}>
                        Hủy
                    </Button>

                    <Button disabled={enrolling}
                        onClick={confirmEnrollment}>
                        {enrolling ? "Đang đăng ký..." : "Xác nhận đăng ký"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default CourseDetail;