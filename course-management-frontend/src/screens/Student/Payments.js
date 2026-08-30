import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/Portal.css";

const Payments = () => {
    const [transactions, setTransactions] = useState([]);
    const [page, setPage] = useState(1);
    const [keyword, setKeyword] = useState("");
    const [status, setStatus] = useState("ALL");
    const [date, setDate] = useState("");
    const [meta, setMeta] = useState({
        currentPage: 1,
        totalPages: 1,
        totalRecords: 0,
        totalTransactions: 0,
        successCount: 0,
        pendingCount: 0,
        failedCount: 0
    });
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const loadPayments = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(`${endpoints.paymentTransactions}/me`, {
                params: {
                    page,
                    ...(keyword.trim() ? { kw: keyword.trim() } : {}),
                    ...(status !== "ALL" ? { status } : {}),
                    ...(date ? { date } : {})
                }
            });

            if (Array.isArray(res.data)) {
                const data = res.data;
                setTransactions(data);
                setMeta({
                    currentPage: 1,
                    totalPages: 1,
                    totalRecords: data.length,
                    totalTransactions: data.length,
                    successCount: data.filter(item => item.status === "SUCCESS").length,
                    pendingCount: data.filter(item => item.status === "PENDING").length,
                    failedCount: data.filter(item => item.status === "FAILED").length
                });
                return;
            }

            setTransactions(Array.isArray(res.data?.transactions) ? res.data.transactions : []);

            const nextMeta = {
                currentPage: Number(res.data?.currentPage || 1),
                totalPages: Number(res.data?.totalPages || 1),
                totalRecords: Number(res.data?.totalRecords || 0),
                totalTransactions: Number(res.data?.totalTransactions || 0),
                successCount: Number(res.data?.successCount || 0),
                pendingCount: Number(res.data?.pendingCount || 0),
                failedCount: Number(res.data?.failedCount || 0)
            };

            setMeta(nextMeta);
            if (nextMeta.currentPage !== page) setPage(nextMeta.currentPage);
        } catch (ex) {
            console.error("Load student payments error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải lịch sử thanh toán!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(loadPayments, 300);
        return () => clearTimeout(timer);
    }, [page, keyword, status, date]);

    const resetFilters = () => {
        setKeyword("");
        setStatus("ALL");
        setDate("");
        setPage(1);
    };

    const buildPages = (totalPages, currentPage) => {
        if (totalPages <= 5)
            return Array.from({ length: totalPages }, (_, index) => index + 1);

        const pages = [1];
        const start = Math.max(2, currentPage - 1);
        const end = Math.min(totalPages - 1, currentPage + 1);

        if (start > 2) pages.push("left-dots");
        for (let item = start; item <= end; item++) pages.push(item);
        if (end < totalPages - 1) pages.push("right-dots");
        pages.push(totalPages);

        return pages;
    };

    const money = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;

    const formatDateTime = value => value
        ? new Date(value).toLocaleString("vi-VN", {
            hour: "2-digit", minute: "2-digit", day: "2-digit",
            month: "2-digit", year: "numeric"
        })
        : "-";

    const statusBadge = value => {
        if (value === "SUCCESS") return <Badge bg="success">Thành công</Badge>;
        if (value === "PENDING") return <Badge bg="warning" text="dark">Chờ xác nhận</Badge>;
        if (value === "FAILED") return <Badge bg="danger">Thất bại</Badge>;
        return <Badge bg="secondary">{value || "-"}</Badge>;
    };

    const paymentMethod = method => {
        if (method === "BANK_TRANSFER") return "Chuyển khoản";
        if (method === "CASH") return "Tiền mặt";
        return method || "-";
    };

    if (loading && meta.totalTransactions === 0)
        return (
            <div className="cm-portal-page">
                <div className="text-center py-5"><MySpinner /></div>
            </div>
        );

    const hasFilter = keyword.trim() || status !== "ALL" || date;

    return (
        <div className="cm-portal-page">
            <div className="cm-portal-container">
                <div className="cm-portal-heading">
                    <div>
                        <span>THANH TOÁN</span>
                        <h1>Lịch sử thanh toán</h1>
                        <p>Theo dõi các giao dịch thanh toán khóa học của bạn.</p>
                    </div>
                </div>

                <Row className="g-3 mb-4">
                    <Col md={3}>
                        <div className="cm-portal-summary h-100">
                            <span>Tổng giao dịch</span>
                            <strong>{meta.totalTransactions}</strong>
                        </div>
                    </Col>

                    <Col md={3}>
                        <div className="cm-portal-summary h-100">
                            <span>Thành công</span>
                            <strong>{meta.successCount}</strong>
                        </div>
                    </Col>

                    <Col md={3}>
                        <div className="cm-portal-summary h-100">
                            <span>Chờ xác nhận</span>
                            <strong>{meta.pendingCount}</strong>
                        </div>
                    </Col>

                    <Col md={3}>
                        <div className="cm-portal-summary h-100">
                            <span>Thất bại</span>
                            <strong>{meta.failedCount}</strong>
                        </div>
                    </Col>
                </Row>

                <Card className="cm-portal-card mb-4">
                    <Card.Body className="p-3">
                        <Row className="g-2 align-items-end">
                            <Col lg={5}>
                                <Form.Label>Tìm kiếm</Form.Label>
                                <Form.Control value={keyword}
                                    onChange={e => {
                                        setKeyword(e.target.value);
                                        setPage(1);
                                    }}
                                    placeholder="Mã giao dịch, khóa học hoặc lớp..."
                                />
                            </Col>

                            <Col lg={3} md={5}>
                                <Form.Label>Trạng thái</Form.Label>
                                <Form.Select value={status}
                                    onChange={e => {
                                        setStatus(e.target.value);
                                        setPage(1);
                                    }}>
                                    <option value="ALL">Tất cả trạng thái</option>
                                    <option value="SUCCESS">Thành công</option>
                                    <option value="PENDING">Chờ xác nhận</option>
                                    <option value="FAILED">Thất bại</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2} md={4}>
                                <Form.Label>Ngày tạo</Form.Label>
                                <Form.Control type="date" value={date}
                                    onChange={e => {
                                        setDate(e.target.value);
                                        setPage(1);
                                    }}
                                />
                            </Col>

                            <Col lg={2} md={3}>
                                <Button variant="outline-secondary" className="w-100"
                                    disabled={!hasFilter} onClick={resetFilters}>
                                    Đặt lại
                                </Button>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                {err && (
                    <Alert variant="danger" dismissible onClose={() => setErr("")}>
                        {err}
                    </Alert>
                )}

                {loading ? (
                    <div className="text-center py-4"><MySpinner /></div>
                ) : transactions.length === 0 ? (
                    <div className="cm-portal-empty">
                        {hasFilter
                            ? "Không tìm thấy giao dịch phù hợp."
                            : "Bạn chưa có giao dịch thanh toán nào."}
                    </div>
                ) : (
                    <Card className="cm-portal-card">
                        <Card.Body className="p-4">
                            <div className="cm-portal-section-heading">
                                <span>GIAO DỊCH</span>
                                <h5>Danh sách thanh toán</h5>
                                <p>Các giao dịch đã được tạo trên tài khoản của bạn.</p>
                            </div>

                            <div className="table-responsive">
                                <Table hover align="middle" className="cm-portal-table">
                                    <thead>
                                        <tr>
                                            <th>Mã giao dịch</th>
                                            <th>Khóa học</th>
                                            <th>Số tiền</th>
                                            <th>Phương thức</th>
                                            <th>Trạng thái</th>
                                            <th>Ngày tạo</th>
                                            <th>Thanh toán lúc</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {transactions.map(item => (
                                            <tr key={item.id}>
                                                <td>
                                                    <strong>{item.transactionCode || `#${item.id}`}</strong>
                                                </td>

                                                <td>
                                                    <strong className="d-block">
                                                        {item.courseName || `Đăng ký #${item.enrollmentId}`}
                                                    </strong>

                                                    {item.className && (
                                                        <small className="cm-portal-muted">
                                                            Lớp: {item.className}
                                                        </small>
                                                    )}
                                                </td>

                                                <td><strong>{money(item.amount)}</strong></td>
                                                <td>{paymentMethod(item.paymentMethod)}</td>
                                                <td>{statusBadge(item.status)}</td>
                                                <td>{formatDateTime(item.createdAt)}</td>
                                                <td>{formatDateTime(item.paidAt)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        </Card.Body>
                    </Card>
                )}

                {!loading && meta.totalRecords > 0 && (
                    <div className="cm-class-pagination mt-3">
                        <span>Tổng <b>{meta.totalRecords}</b> · Trang {meta.currentPage}/{meta.totalPages}</span>

                        {meta.totalPages > 1 && (
                            <div className="cm-class-pagination-buttons">
                                <button type="button" disabled={meta.currentPage === 1}
                                    onClick={() => setPage(meta.currentPage - 1)}>
                                    ‹
                                </button>

                                {buildPages(meta.totalPages, meta.currentPage).map((item, index) =>
                                    typeof item === "number" ? (
                                        <button type="button" key={item}
                                            className={item === meta.currentPage ? "active" : ""}
                                            onClick={() => setPage(item)}>
                                            {item}
                                        </button>
                                    ) : (
                                        <span key={`${item}-${index}`}>...</span>
                                    )
                                )}

                                <button type="button" disabled={meta.currentPage === meta.totalPages}
                                    onClick={() => setPage(meta.currentPage + 1)}>
                                    ›
                                </button>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Payments;
