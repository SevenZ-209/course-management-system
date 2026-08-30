import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Payments = () => {
    const [q, setQ] = useSearchParams();

    const [payments, setPayments] = useState([]);
    const [courses, setCourses] = useState([]);
    const [enrollments, setEnrollments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({
        enrollmentId: "", amount: "", paymentMethod: "",
        transactionCode: "", status: "PENDING"
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const status = q.get("status") || "";
    const date = q.get("date") || "";

    const getPaymentId = p => p.id ?? p.transactionId;
    const getCourseId = c => c.id ?? c.courseId;
    const getEnrollmentId = e => e.id ?? e.enrollmentId;

    const loadPayments = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (courseId) params.courseId = courseId;
            if (status) params.status = status;
            if (date) params.date = date;

            const res = await authApis().get(endpoints.adminPayments, { params });
            const data = res.data;

            setPayments(data.transactions || data.payments || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load payments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách giao dịch!");
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const [courseRes, enrollmentRes] = await Promise.all([
                authApis().get(endpoints.adminCourseOptions),
                authApis().get(endpoints.adminPendingEnrollmentOptions)
            ]);

            setCourses(Array.isArray(courseRes.data) ? courseRes.data : []);
            setEnrollments(Array.isArray(enrollmentRes.data) ? enrollmentRes.data : []);
        } catch (ex) {
            console.error("Load payment options error:", ex);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        loadPayments();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (courseId) params.courseId = courseId;
        if (status) params.status = status;
        if (date) params.date = date;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        const params = Object.fromEntries(q);

        if (value) params[name] = value;
        else delete params[name];

        params.page = "1";
        setQ(params);
    };

    const clearFilters = () => {
        setKw("");
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const resetForm = () => {
        setForm({
            enrollmentId: "", amount: "", paymentMethod: "",
            transactionCode: "", status: "PENDING"
        });
    };

    const openAddModal = async () => {
        resetForm();

        try {
            const res = await authApis().get(endpoints.adminPendingEnrollmentOptions);
            setEnrollments(Array.isArray(res.data) ? res.data : []);
        } catch (ex) {
            console.error("Load pending enrollment options error:", ex);
        }

        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        resetForm();
    };

    const selectEnrollment = value => {
        const enrollment = enrollments.find(e =>
            String(getEnrollmentId(e)) === String(value)
        );

        const tuitionFee =
            enrollment?.tuitionFee ??
            enrollment?.courseTuitionFee ??
            enrollment?.amount ??
            "";

        setForm(prev => ({
            ...prev,
            enrollmentId: value,
            amount: tuitionFee
        }));
    };

    const savePayment = async e => {
        e.preventDefault();

        if (!form.enrollmentId) return setErr("Vui lòng chọn đăng ký!");
        if (!form.amount || Number(form.amount) <= 0)
            return setErr("Số tiền thanh toán phải lớn hơn 0!");
        if (!form.paymentMethod.trim())
            return setErr("Vui lòng nhập phương thức thanh toán!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const res = await authApis().post(endpoints.adminPayments, {
                enrollmentId: Number(form.enrollmentId),
                amount: Number(form.amount),
                paymentMethod: form.paymentMethod.trim(),
                transactionCode: form.transactionCode.trim() || null,
                status: form.status
            });

            setSuccess(res.data?.message || "Thêm giao dịch thành công!");
            closeModal();
            await Promise.all([loadPayments(), loadOptions()]);
        } catch (ex) {
            console.error("Add payment error:", ex);
            setErr(ex.response?.data?.message || "Thêm giao dịch thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const updateStatus = async (transactionId, newStatus) => {
        if (!window.confirm(`Xác nhận chuyển giao dịch sang ${newStatus}?`)) return;

        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminPayments}/${transactionId}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật giao dịch thành công!");
            await Promise.all([loadPayments(), loadOptions()]);
        } catch (ex) {
            console.error("Update payment error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật giao dịch thất bại!");
            await loadPayments();
        }
    };

    const studentName = p =>
        p.studentName ||
        p.studentFullName ||
        p.enrollment?.student?.fullName ||
        "-";

    const courseName = p =>
        p.courseName ||
        p.enrollment?.courseClass?.course?.name ||
        "-";

    const className = p =>
        p.className ||
        p.courseClassName ||
        p.enrollment?.courseClass?.name ||
        "-";

    const enrollmentLabel = e => {
        const student = e.studentName || e.studentFullName || e.student?.fullName || "Học viên";
        const course = e.courseName || e.course?.name || e.courseClass?.course?.name || "";
        const classLabel = e.className || e.courseClassName || e.courseClass?.name || "";

        return `${student}${course ? ` - ${course}` : ""}${classLabel ? ` (${classLabel})` : ""}`;
    };

    const statusBadge = value => {
        const config = {
            PENDING: ["warning", "Chờ xử lý"],
            SUCCESS: ["success", "Thành công"],
            FAILED: ["danger", "Thất bại"]
        }[value] || ["secondary", value || "-"];

        return (
            <Badge bg={config[0]} text={value === "PENDING" ? "dark" : undefined}>
                {config[1]}
            </Badge>
        );
    };

    const formatMoney = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;
    const formatDateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý giao dịch</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> giao dịch
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm giao dịch</Button>
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
                                <Form.Control placeholder="Tìm học viên, mã giao dịch..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={courseId}
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
                                <Form.Select value={status}
                                    onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="PENDING">Chờ xử lý</option>
                                    <option value="SUCCESS">Thành công</option>
                                    <option value="FAILED">Thất bại</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2}>
                                <Form.Control type="date" value={date}
                                    onChange={e => changeFilter("date", e.target.value)} />
                            </Col>

                            <Col lg={3} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
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
                    ) : payments.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy giao dịch.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Học viên</th>
                                        <th>Khóa học / Lớp</th>
                                        <th>Số tiền</th>
                                        <th>Phương thức</th>
                                        <th>Mã giao dịch</th>
                                        <th>Trạng thái</th>
                                        <th>Thanh toán lúc</th>
                                        <th>Cập nhật</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {payments.map(p => (
                                        <tr key={getPaymentId(p)}>
                                            <td>{getPaymentId(p)}</td>
                                            <td className="fw-semibold">{studentName(p)}</td>

                                            <td>
                                                <div>{courseName(p)}</div>
                                                <small className="text-muted">{className(p)}</small>
                                            </td>

                                            <td className="fw-semibold">{formatMoney(p.amount)}</td>
                                            <td>{p.paymentMethod}</td>
                                            <td>{p.transactionCode || "-"}</td>
                                            <td>{statusBadge(p.status)}</td>
                                            <td>{formatDateTime(p.paidAt)}</td>

                                            <td style={{ minWidth: 140 }}>
                                                {p.status === "PENDING" ? (
                                                    <Form.Select size="sm" defaultValue=""
                                                        onChange={e => {
                                                            if (e.target.value)
                                                                updateStatus(getPaymentId(p), e.target.value);
                                                        }}>
                                                        <option value="">-- Chọn --</option>
                                                        <option value="SUCCESS">Thành công</option>
                                                        <option value="FAILED">Thất bại</option>
                                                    </Form.Select>
                                                ) : (
                                                    <span className="text-muted">Đã xử lý</span>
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
                <Form onSubmit={savePayment}>
                    <Modal.Header closeButton>
                        <Modal.Title>Thêm giao dịch</Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Đăng ký lớp học</Form.Label>

                            <Form.Select value={form.enrollmentId}
                                onChange={e => selectEnrollment(e.target.value)}
                                required>
                                <option value="">-- Chọn đăng ký --</option>

                                {enrollments.map(e => (
                                    <option key={getEnrollmentId(e)} value={getEnrollmentId(e)}>
                                        {enrollmentLabel(e)}
                                    </option>
                                ))}
                            </Form.Select>

                            {enrollments.length === 0 && (
                                <Form.Text className="text-muted">
                                    Không có đăng ký đang chờ thanh toán.
                                </Form.Text>
                            )}
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Số tiền</Form.Label>

                            <Form.Control type="number" min="1"
                                value={form.amount}
                                onChange={e => setForm({ ...form, amount: e.target.value })}
                                required />

                            <Form.Text className="text-muted">
                                Số tiền phải bằng đúng học phí khóa học.
                            </Form.Text>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Phương thức thanh toán</Form.Label>

                            <Form.Control placeholder="Ví dụ: BANK_TRANSFER"
                                value={form.paymentMethod}
                                onChange={e => setForm({ ...form, paymentMethod: e.target.value })}
                                required />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Mã giao dịch</Form.Label>

                            <Form.Control placeholder="Để trống để hệ thống tự sinh"
                                value={form.transactionCode}
                                onChange={e => setForm({ ...form, transactionCode: e.target.value })} />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Trạng thái</Form.Label>

                            <Form.Select value={form.status}
                                onChange={e => setForm({ ...form, status: e.target.value })}>
                                <option value="PENDING">Chờ xử lý</option>
                                <option value="SUCCESS">Thành công</option>
                                <option value="FAILED">Thất bại</option>
                            </Form.Select>
                        </Form.Group>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal} disabled={saving}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={saving || !enrollments.length}>
                            {saving ? "Đang lưu..." : "Lưu giao dịch"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default Payments;