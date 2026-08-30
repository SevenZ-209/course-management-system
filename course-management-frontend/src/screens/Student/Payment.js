import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/StudentPayment.css";

const Payment = () => {
    const { enrollmentId } = useParams();
    const nav = useNavigate();

    const [enrollment, setEnrollment] = useState(null);
    const [course, setCourse] = useState(null);
    const [transaction, setTransaction] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState("BANK_TRANSFER");
    const [transactionCode, setTransactionCode] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");

    const loadData = async () => {
        try {
            setLoading(true);
            setErr("");

            const enrollmentRes = await authApis().get(
                `${endpoints.studentEnrollments}/${enrollmentId}`
            );

            const enrollmentData = enrollmentRes.data;
            setEnrollment(enrollmentData);

            const courseRes = await Apis.get(
                `${endpoints.publicCourses}/${enrollmentData.courseId}`
            );

            setCourse(courseRes.data);

        } catch (ex) {
            console.error("Load payment error:", ex);
            setErr(
                ex.response?.data?.message ||
                "Không thể tải thông tin thanh toán!"
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [enrollmentId]);


    const submitPayment = async e => {
        e.preventDefault();

        if(enrollment?.status !== "PENDING_PAYMENT")
            return setErr("Đăng ký này không còn ở trạng thái chờ thanh toán!");

        try {
            setSaving(true);
            setErr("");

            const res = await authApis().post(
                endpoints.paymentTransactions,
                {
                    enrollmentId: Number(enrollmentId),
                    amount: Number(course.tuitionFee),
                    paymentMethod,
                    transactionCode: transactionCode.trim() || null
                }
            );

            setTransaction(res.data);

        } catch(ex) {
            console.error("Create payment error:", ex);
            setErr(
                ex.response?.data?.message ||
                "Tạo giao dịch thất bại!"
            );
        } finally {
            setSaving(false);
        }
    };


    const money = value =>
        `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;


    if(loading)
        return <div className="text-center py-5"><MySpinner /></div>;


    if(err && !enrollment)
        return (
            <div className="cm-section py-5">
                <Alert variant="danger">{err}</Alert>

                <Button
                    variant="outline-secondary"
                    onClick={() => nav("/student/courses")}
                >
                    Khóa học của tôi
                </Button>
            </div>
        );


    if(transaction)
        return (
            <div className="cm-payment-page">
                <div className="cm-payment-container">

                    <div className="cm-payment-success">

                        <div className="cm-payment-result-icon">
                            ✓
                        </div>

                        <span>
                            THANH TOÁN THÀNH CÔNG
                        </span>

                        <h2>
                            Khóa học đã được kích hoạt
                        </h2>

                        <p>
                            Giao dịch của bạn đã hoàn tất.
                        </p>


                        <div className="cm-transaction-box">

                            <div>
                                <span>Mã giao dịch</span>
                                <strong>
                                    {transaction.transactionCode ||
                                    `#${transaction.id}`}
                                </strong>
                            </div>


                            <div>
                                <span>Số tiền</span>
                                <strong>
                                    {money(transaction.amount)}
                                </strong>
                            </div>

                        </div>


                        <Button onClick={() => nav("/student/courses")}>
                            Khóa học của tôi
                        </Button>

                    </div>

                </div>
            </div>
        );


    return (
        <div className="cm-payment-page">

            <div className="cm-payment-container">

                <Button
                    variant="outline-secondary"
                    size="sm"
                    className="mb-4"
                    onClick={() => nav("/student/courses")}
                >
                    ← Quay lại
                </Button>


                <div className="cm-payment-heading">
                    <span>THANH TOÁN</span>

                    <h1>
                        Hoàn tất đăng ký
                    </h1>

                    <p>
                        Xác nhận thông tin khóa học trước khi thanh toán.
                    </p>
                </div>


                {err && (
                    <Alert
                        variant="danger"
                        dismissible
                        onClose={() => setErr("")}
                    >
                        {err}
                    </Alert>
                )}


                <Row className="g-4">


                    <Col lg={7}>

                        <Card className="cm-payment-card">

                            <Card.Body>

                                <h5>
                                    Thông tin khóa học
                                </h5>


                                <div className="cm-payment-course">

                                    <img
                                        src={
                                            course?.imageUrl ||
                                            "/images/course-default.jpg"
                                        }
                                        alt={course?.name}
                                    />


                                    <div>

                                        <small>
                                            {course?.categoryName}
                                        </small>


                                        <h3>
                                            {enrollment?.courseName}
                                        </h3>


                                        <p>
                                            Lớp:
                                            <strong>
                                                {" "}
                                                {enrollment?.className}
                                            </strong>
                                        </p>

                                    </div>

                                </div>



                                <div className="cm-payment-info">

                                    <div>
                                        <span>
                                            Mã đăng ký
                                        </span>

                                        <strong>
                                            #{enrollment?.enrollmentId}
                                        </strong>
                                    </div>


                                    <div>
                                        <span>
                                            Trạng thái
                                        </span>

                                        <span className="cm-status pending">
                                            Chờ thanh toán
                                        </span>
                                    </div>

                                </div>

                            </Card.Body>

                        </Card>

                    </Col>



                    <Col lg={5}>

                        <Card className="cm-payment-card">

                            <Card.Body>

                                <h5>
                                    Thanh toán
                                </h5>


                                <div className="cm-payment-total">

                                    <span>
                                        Tổng học phí
                                    </span>

                                    <strong>
                                        {money(course?.tuitionFee)}
                                    </strong>

                                </div>


                                <Form
                                    onSubmit={submitPayment}
                                    className="mt-4"
                                >

                                    <Form.Group className="mb-3">

                                        <Form.Label>
                                            Phương thức thanh toán
                                        </Form.Label>


                                        <Form.Select
                                            value={paymentMethod}
                                            onChange={e =>
                                                setPaymentMethod(e.target.value)
                                            }
                                        >

                                            <option value="BANK_TRANSFER">
                                                Chuyển khoản ngân hàng
                                            </option>

                                            <option value="CASH">
                                                Tiền mặt
                                            </option>

                                        </Form.Select>

                                    </Form.Group>



                                    <Form.Group className="mb-4">

                                        <Form.Label>
                                            Mã giao dịch
                                        </Form.Label>


                                        <Form.Control
                                            value={transactionCode}
                                            onChange={e =>
                                                setTransactionCode(e.target.value)
                                            }
                                            placeholder="Có thể để trống"
                                        />


                                    </Form.Group>



                                    <Button
                                        type="submit"
                                        className="w-100"
                                        disabled={saving}
                                    >
                                        {
                                            saving
                                            ? "Đang xử lý..."
                                            : `Thanh toán ${money(course?.tuitionFee)}`
                                        }
                                    </Button>

                                </Form>

                            </Card.Body>

                        </Card>

                    </Col>

                </Row>

            </div>

        </div>
    );
};

export default Payment;