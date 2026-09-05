import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Payments = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["status", "date"]);
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const page = Number(q.get("page")) || 1;
    const status = q.get("status") || "";
    const date = q.get("date") || "";

    const loadPayments = async () => {
        try {
            setLoading(true);
            setErr("");
            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (status) params.status = status;
            if (date) params.date = date;

            const res = await authApis().get(endpoints.managerPayments, { params });
            setPayments(Array.isArray(res.data?.transactions) ? res.data.transactions : []);
            setTotalPages(Number(res.data?.totalPages || 1));
            setTotalRecords(Number(res.data?.totalRecords || 0));
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không thể tải danh sách giao dịch!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadPayments(); }, [q]);

    const search = e => {
        e.preventDefault();
        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.status) params.status = draftFilters.status;
        if (draftFilters.date) params.date = draftFilters.date;
        setQ(params);
    };

    const changeFilter = (name, value) => {
        setDraftFilter(name, value);
    };

    const clearFilters = () => { setKw("");
        resetDraftFilters(); setQ({ page: "1" }); };
    const changePage = value => {
        const params = Object.fromEntries(q);
        params.page = String(value);
        setQ(params);
    };

    const updateStatus = async (transactionId, newStatus) => {
        if (!window.confirm(`Xác nhận chuyển giao dịch sang ${newStatus}?`)) return;
        try {
            setErr("");
            setSuccess("");
            const res = await authApis().patch(`${endpoints.managerPayments}/${transactionId}/status`, { status: newStatus });
            setSuccess(res.data?.message || "Cập nhật giao dịch thành công!");
            await loadPayments();
        } catch (ex) {
            setErr(ex.response?.data?.message || "Cập nhật giao dịch thất bại!");
            await loadPayments();
        }
    };

    const statusBadge = value => {
        const config = {
            PENDING: ["warning", "Chờ xử lý"],
            SUCCESS: ["success", "Thành công"],
            FAILED: ["danger", "Thất bại"]
        }[value] || ["secondary", value || "-"];
        return <Badge bg={config[0]} text={value === "PENDING" ? "dark" : undefined}>{config[1]}</Badge>;
    };

    const money = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;
    const dateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Xử lý thanh toán</h2>
                    <p className="text-muted mb-0">Tổng cộng <strong>{totalRecords}</strong> giao dịch</p>
                </div>
            </div>

            {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}
            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={5}>
                                <Form.Control placeholder="Tìm học viên, lớp, khóa học, mã giao dịch..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>
                            <Col lg={2}>
                                <Form.Select value={draftFilters.status} onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="PENDING">Chờ xử lý</option>
                                    <option value="SUCCESS">Thành công</option>
                                    <option value="FAILED">Thất bại</option>
                                </Form.Select>
                            </Col>
                            <Col lg={2}>
                                <Form.Control type="date" value={draftFilters.date} onChange={e => changeFilter("date", e.target.value)} />
                            </Col>
                            <Col lg={3} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
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
                        <Alert variant="info" className="m-4 text-center">Không tìm thấy giao dịch.</Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th><th>Học viên</th><th>Khóa học / Lớp</th><th>Số tiền</th>
                                        <th>Phương thức</th><th>Mã giao dịch</th><th>Trạng thái</th><th>Ngày tạo</th><th>Xử lý</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {payments.map(p => (
                                        <tr key={p.id}>
                                            <td>{p.id}</td>
                                            <td><strong>{p.studentName || p.username || "-"}</strong></td>
                                            <td><div>{p.courseName || "-"}</div><small className="text-muted">{p.className || "-"}</small></td>
                                            <td className="fw-semibold">{money(p.amount)}</td>
                                            <td>{p.paymentMethod || "-"}</td>
                                            <td>{p.transactionCode || "-"}</td>
                                            <td>{statusBadge(p.status)}</td>
                                            <td>{dateTime(p.createdAt)}</td>
                                            <td style={{ minWidth: 145 }}>
                                                {p.status === "PENDING" ? (
                                                    <Form.Select size="sm" defaultValue="" onChange={e => {
                                                        if (e.target.value) updateStatus(p.id, e.target.value);
                                                    }}>
                                                        <option value="">-- Chọn --</option>
                                                        <option value="SUCCESS">Thành công</option>
                                                        <option value="FAILED">Thất bại</option>
                                                    </Form.Select>
                                                ) : <span className="text-muted">Đã xử lý</span>}
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
                        <Pagination.Prev disabled={page === 1} onClick={() => changePage(Math.max(page - 1, 1))} />
                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                            <Pagination.Item key={n} active={n === page} onClick={() => changePage(n)}>{n}</Pagination.Item>
                        ))}
                        <Pagination.Next disabled={page === totalPages} onClick={() => changePage(Math.min(page + 1, totalPages))} />
                    </Pagination>
                </div>
            )}
        </>
    );
};

export default Payments;
